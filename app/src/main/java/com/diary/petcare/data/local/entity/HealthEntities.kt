package com.diary.petcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diary.petcare.domain.model.Medicine
import com.diary.petcare.domain.model.MedicineDose
import com.diary.petcare.domain.model.MedicineKind
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.model.VetVisit

@Entity(
    tableName = "vaccines",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId"), Index("nextDueDate")],
)
data class VaccineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val name: String,
    val dateGiven: Long?,
    val nextDueDate: Long?,
    val vetName: String,
    val cardPhotoPath: String?,
    val notes: String,
    val reminderEnabled: Boolean,
)

fun VaccineEntity.toDomain() =
    Vaccine(id, petId, name, dateGiven, nextDueDate, vetName, cardPhotoPath, notes, reminderEnabled)

fun Vaccine.toEntity() =
    VaccineEntity(id, petId, name, dateGiven, nextDueDate, vetName, cardPhotoPath, notes, reminderEnabled)

@Entity(
    tableName = "medicines",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId")],
)
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val name: String,
    val kind: MedicineKind,
    val dose: String,
    /** Times of day as minutes after midnight, comma separated, e.g. "480,1200". */
    val timesOfDay: String,
    val startDate: Long,
    val endDate: Long?,
    val notes: String,
    val reminderEnabled: Boolean,
)

fun MedicineEntity.toDomain() = Medicine(
    id = id,
    petId = petId,
    name = name,
    kind = kind,
    dose = dose,
    timesOfDay = timesOfDay.split(',').mapNotNull { it.trim().toIntOrNull() }.sorted(),
    startDate = startDate,
    endDate = endDate,
    notes = notes,
    reminderEnabled = reminderEnabled,
)

fun Medicine.toEntity() = MedicineEntity(
    id = id,
    petId = petId,
    name = name,
    kind = kind,
    dose = dose,
    timesOfDay = timesOfDay.sorted().joinToString(","),
    startDate = startDate,
    endDate = endDate,
    notes = notes,
    reminderEnabled = reminderEnabled,
)

@Entity(
    tableName = "medicine_doses",
    foreignKeys = [ForeignKey(MedicineEntity::class, ["id"], ["medicineId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["medicineId", "scheduledAt"], unique = true), Index("petId")],
)
data class MedicineDoseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val petId: Long,
    val scheduledAt: Long,
    val givenAt: Long?,
)

fun MedicineDoseEntity.toDomain() = MedicineDose(id, medicineId, petId, scheduledAt, givenAt)
fun MedicineDose.toEntity() = MedicineDoseEntity(id, medicineId, petId, scheduledAt, givenAt)

@Entity(
    tableName = "vet_visits",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId")],
)
data class VetVisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val date: Long,
    val vetName: String,
    val clinic: String,
    val reason: String,
    val diagnosis: String,
    val prescriptionPhotoPath: String?,
    val costPaisa: Long,
    val nextVisitDate: Long?,
    val reminderEnabled: Boolean,
)

fun VetVisitEntity.toDomain() = VetVisit(
    id, petId, date, vetName, clinic, reason, diagnosis, prescriptionPhotoPath, costPaisa, nextVisitDate, reminderEnabled,
)

fun VetVisit.toEntity() = VetVisitEntity(
    id, petId, date, vetName, clinic, reason, diagnosis, prescriptionPhotoPath, costPaisa, nextVisitDate, reminderEnabled,
)

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["type", "refId", "slot"], unique = true), Index("petId"), Index("triggerAt")],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val type: ReminderType,
    val refId: Long,
    val slot: Int,
    val title: String,
    val message: String,
    val triggerAt: Long,
    val repeatIntervalMillis: Long,
    val enabled: Boolean,
    val lastDoneAt: Long?,
)

fun ReminderEntity.toDomain() =
    Reminder(id, petId, type, refId, slot, title, message, triggerAt, repeatIntervalMillis, enabled, lastDoneAt)

fun Reminder.toEntity() =
    ReminderEntity(id, petId, type, refId, slot, title, message, triggerAt, repeatIntervalMillis, enabled, lastDoneAt)
