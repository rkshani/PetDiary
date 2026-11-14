package com.diary.petcare.data.report

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.diary.petcare.R
import com.diary.petcare.data.settings.LocaleStore
import com.diary.petcare.di.IoDispatcher
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.domain.repository.MedicineRepository
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.repository.VetVisitRepository
import com.diary.petcare.domain.repository.WeightRepository
import com.diary.petcare.ui.components.label
import com.diary.petcare.ui.format.Formatters
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Which parts go into the report. */
data class ReportOptions(
    val profile: Boolean = true,
    val vaccines: Boolean = true,
    val medicines: Boolean = true,
    val vetVisits: Boolean = true,
    val weight: Boolean = true,
)

/**
 * Builds an A4 health report PDF with Android's built-in PdfDocument (no extra library).
 * Text goes through StaticLayout so Urdu and other right-to-left text shapes correctly.
 */
@Singleton
class HealthReportGenerator @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val pets: PetRepository,
    private val vaccines: VaccineRepository,
    private val medicines: MedicineRepository,
    private val vetVisits: VetVisitRepository,
    private val weights: WeightRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    /** Returns the PDF file in the cache "reports" folder, or null if the pet is missing. */
    suspend fun generate(petId: Long, options: ReportOptions, settings: AppSettings): File? = withContext(io) {
        val pet = pets.getPet(petId) ?: return@withContext null
        val ctx = LocaleStore.wrap(appContext)
        val w = PdfWriter(ctx)

        w.title(ctx.getString(R.string.report_title, pet.name))
        w.small(ctx.getString(R.string.report_generated, Formatters.dateTime(System.currentTimeMillis())))
        w.space(8f)

        if (options.profile) {
            w.heading(ctx.getString(R.string.report_profile))
            pet.photoPath?.let { w.photo(it) }
            w.row(ctx.getString(R.string.pet_name), pet.name)
            w.row(ctx.getString(R.string.pet_type), ctx.getString(pet.type.label))
            w.row(ctx.getString(R.string.pet_breed), pet.breed)
            w.row(ctx.getString(R.string.pet_gender), ctx.getString(pet.gender.label))
            pet.birthDate?.let { w.row(ctx.getString(R.string.pet_birthday), Formatters.date(it)) }
            w.row(ctx.getString(R.string.pet_color), pet.color)
            w.row(ctx.getString(R.string.pet_microchip), pet.microchip)
            w.row(ctx.getString(R.string.notes), pet.notes)
        }

        if (options.vaccines) {
            w.heading(ctx.getString(R.string.vaccines_title))
            val list = vaccines.getForPet(petId)
            if (list.isEmpty()) w.body(ctx.getString(R.string.report_none))
            list.forEach { v ->
                w.bold(v.name)
                w.body(
                    listOfNotNull(
                        v.dateGiven?.let { ctx.getString(R.string.vaccine_given_on, Formatters.date(it)) },
                        v.nextDueDate?.let { ctx.getString(R.string.vaccine_next_on, Formatters.date(it)) },
                        v.vetName.ifBlank { null },
                    ).joinToString("  •  "),
                )
            }
        }

        if (options.medicines) {
            w.heading(ctx.getString(R.string.medicines_title))
            val list = medicines.getForPet(petId)
            if (list.isEmpty()) w.body(ctx.getString(R.string.report_none))
            list.forEach { m ->
                w.bold("${m.name} (${ctx.getString(m.kind.label)})")
                val range = listOfNotNull(Formatters.date(m.startDate), m.endDate?.let { Formatters.date(it) }).joinToString(" → ")
                val times = m.timesOfDay.joinToString(", ") { Formatters.timeOfDay(it) }
                w.body(listOf(m.dose, times, range).filter { it.isNotBlank() }.joinToString("  •  "))
                if (m.notes.isNotBlank()) w.small(m.notes)
            }
        }

        if (options.vetVisits) {
            w.heading(ctx.getString(R.string.vet_title))
            val list = vetVisits.getForPet(petId)
            if (list.isEmpty()) w.body(ctx.getString(R.string.report_none))
            list.forEach { v ->
                w.bold("${Formatters.date(v.date)} — ${v.reason}")
                w.body(
                    listOfNotNull(
                        listOf(v.vetName, v.clinic).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null },
                        v.costPaisa.takeIf { it > 0 }?.let { Formatters.money(it, settings.currency) },
                        v.nextVisitDate?.let { ctx.getString(R.string.vet_next_on, Formatters.date(it)) },
                    ).joinToString("  •  "),
                )
                if (v.diagnosis.isNotBlank()) w.small(v.diagnosis)
            }
        }

        if (options.weight) {
            w.heading(ctx.getString(R.string.weight_title))
            val list = weights.getForPet(petId)
            if (list.isEmpty()) w.body(ctx.getString(R.string.report_none))
            if (list.size >= 2) w.chart(list.map { it.date to Formatters.weightValue(it.weightGrams, settings.weightUnit) })
            list.asReversed().forEach { l ->
                w.row(Formatters.date(l.date), Formatters.weight(l.weightGrams, settings.weightUnit) + if (l.note.isBlank()) "" else "  (${l.note})")
            }
        }

        w.space(16f)
        w.small(ctx.getString(R.string.report_footer))

        val dir = File(appContext.cacheDir, "reports").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val safeName = pet.name.replace(Regex("[^\\p{L}\\p{N}_-]+"), "_").take(40)
        val file = File(dir, "${safeName}_health_report.pdf")
        w.finish(file)
        file
    }
}

/** Tiny top-to-bottom PDF writer with automatic page breaks. */
private class PdfWriter(private val ctx: Context) {
    private val doc = PdfDocument()
    private var pageNo = 0
    private lateinit var page: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var y = 0f

    private val accent = Color.rgb(0xB8, 0x5C, 0x38)
    private val textColor = Color.rgb(0x23, 0x1A, 0x16)
    private val grey = Color.rgb(0x85, 0x73, 0x6B)

    init {
        newPage()
    }

    private fun newPage() {
        if (pageNo > 0) {
            footer()
            doc.finishPage(page)
        }
        pageNo++
        page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create())
        canvas = page.canvas
        y = MARGIN
    }

    private fun footer() {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; color = grey }
        val text = ctx.getString(R.string.report_page, pageNo)
        canvas.drawText(text, PAGE_W - MARGIN - p.measureText(text), PAGE_H - 24f, p)
        canvas.drawText(ctx.getString(R.string.app_name), MARGIN, PAGE_H - 24f, p)
    }

    private fun ensure(height: Float) {
        if (y + height > PAGE_H - BOTTOM) newPage()
    }

    private fun text(s: String, size: Float, color: Int, bold: Boolean = false, indent: Float = 0f) {
        if (s.isBlank()) return
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val width = (PAGE_W - 2 * MARGIN - indent).toInt()
        val layout = StaticLayout.Builder.obtain(s, 0, s.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.1f)
            .build()
        ensure(layout.height.toFloat())
        canvas.save()
        canvas.translate(MARGIN + indent, y)
        layout.draw(canvas)
        canvas.restore()
        y += layout.height + 2f
    }

    fun title(s: String) = text(s, 22f, accent, bold = true)
    fun heading(s: String) {
        space(10f)
        ensure(40f)
        text(s, 15f, accent, bold = true)
        val line = Paint().apply { color = accent; strokeWidth = 1f }
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, line)
        y += 6f
    }

    fun bold(s: String) = text(s, 11f, textColor, bold = true)
    fun body(s: String) = text(s, 10.5f, textColor, indent = 8f)
    fun small(s: String) = text(s, 9f, grey, indent = 8f)
    fun space(h: Float) {
        y += h
    }

    fun row(label: String, value: String) {
        if (value.isBlank()) return
        text("$label:  $value", 10.5f, textColor)
    }

    fun photo(path: String) {
        val bmp = runCatching {
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 })
        }.getOrNull() ?: return
        val size = 90f
        ensure(size + 8f)
        val scale = minOf(size / bmp.width, size / bmp.height)
        val dst = RectF(MARGIN, y, MARGIN + bmp.width * scale, y + bmp.height * scale)
        canvas.drawBitmap(bmp, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
        y += dst.height() + 8f
    }

    /** Small line chart of (date, value) points. */
    fun chart(points: List<Pair<Long, Double>>) {
        val h = 120f
        ensure(h + 16f)
        val left = MARGIN + 30f
        val right = PAGE_W - MARGIN
        val top = y + 4f
        val bottom = y + h
        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }.coerceAtLeast(minX + 1)
        val minY = points.minOf { it.second } * 0.95
        val maxY = (points.maxOf { it.second } * 1.05).coerceAtLeast(minY + 0.1)
        val axis = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = grey; strokeWidth = 0.5f }
        canvas.drawLine(left, bottom, right, bottom, axis)
        canvas.drawLine(left, top, left, bottom, axis)
        val label = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8f; color = grey }
        canvas.drawText("%.1f".format(maxY), MARGIN, top + 8f, label)
        canvas.drawText("%.1f".format(minY), MARGIN, bottom, label)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; strokeWidth = 2f; style = Paint.Style.STROKE }
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
        var px = 0f
        var py = 0f
        points.sortedBy { it.first }.forEachIndexed { i, (d, v) ->
            val x = left + (d - minX).toFloat() / (maxX - minX) * (right - left)
            val yy = (bottom - (v - minY) / (maxY - minY) * (bottom - top)).toFloat()
            if (i > 0) canvas.drawLine(px, py, x, yy, line)
            canvas.drawCircle(x, yy, 2.5f, dot)
            px = x
            py = yy
        }
        y = bottom + 12f
    }

    fun finish(file: File) {
        footer()
        doc.finishPage(page)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    private companion object {
        const val PAGE_W = 595 // A4 at 72 dpi
        const val PAGE_H = 842
        const val MARGIN = 40f
        const val BOTTOM = 50f
    }
}
