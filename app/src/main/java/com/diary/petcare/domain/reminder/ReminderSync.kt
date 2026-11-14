package com.diary.petcare.domain.reminder

import com.diary.petcare.core.util.DateTimeUtils
import com.diary.petcare.domain.model.DAY_MILLIS
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.model.Medicine
import com.diary.petcare.domain.model.Reminder
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.domain.model.Vaccine
import com.diary.petcare.domain.model.VetVisit
import com.diary.petcare.domain.repository.ReminderRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the reminders table in step with the records that create reminders.
 * Every save or delete of a feeding schedule, medicine, vaccine, vet visit or grooming
 * calls into this class.
 */
@Singleton
class ReminderSync @Inject constructor(
    private val repo: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend fun syncFeeding(s: FeedingSchedule) {
        if (!s.reminderEnabled) return remove(ReminderType.FEEDING, s.id)
        put(
            Reminder(
                petId = s.petId, type = ReminderType.FEEDING, refId = s.id, title = s.food, message = s.quantity,
                triggerAt = DateTimeUtils.nextOccurrence(s.timeOfDay), repeatIntervalMillis = DAY_MILLIS,
            ),
        )
    }

    suspend fun syncMedicine(m: Medicine) {
        remove(ReminderType.MEDICINE, m.id)
        if (!m.reminderEnabled) return
        val from = maxOf(DateTimeUtils.now(), m.startDate - 1)
        m.timesOfDay.forEachIndexed { slot, minutes ->
            val first = DateTimeUtils.nextOccurrence(minutes, from)
            if (m.endDate == null || first <= DateTimeUtils.endOfDay(m.endDate)) {
                put(
                    Reminder(
                        petId = m.petId, type = ReminderType.MEDICINE, refId = m.id, slot = slot, title = m.name,
                        message = m.dose, triggerAt = first, repeatIntervalMillis = DAY_MILLIS,
                    ),
                )
            }
        }
    }

    suspend fun syncVaccine(v: Vaccine) {
        val due = v.nextDueDate
        if (!v.reminderEnabled || due == null) return remove(ReminderType.VACCINE, v.id)
        putOneShot(v.petId, ReminderType.VACCINE, v.id, v.name, "", DateTimeUtils.atTimeOfDay(due, MORNING))
    }

    suspend fun syncVetVisit(v: VetVisit) {
        val next = v.nextVisitDate
        if (!v.reminderEnabled || next == null) return remove(ReminderType.VET, v.id)
        putOneShot(v.petId, ReminderType.VET, v.id, v.reason, v.clinic, DateTimeUtils.atTimeOfDay(next, MORNING))
    }

    suspend fun syncGrooming(g: Grooming) {
        val next = g.nextDueDate
        if (!g.reminderEnabled || next == null) return remove(ReminderType.GROOMING, g.id)
        putOneShot(g.petId, ReminderType.GROOMING, g.id, g.type.name, g.note, DateTimeUtils.atTimeOfDay(next, MORNING))
    }

    suspend fun remove(type: ReminderType, refId: Long) {
        repo.getByRef(type, refId).forEach {
            scheduler.cancel(it.id)
            repo.delete(it.id)
        }
    }

    /** Puts every enabled reminder back on the timer, e.g. after a restore. */
    suspend fun rescheduleAll() {
        repo.getEnabled().forEach { scheduler.schedule(it) }
    }

    private suspend fun putOneShot(petId: Long, type: ReminderType, refId: Long, title: String, msg: String, at: Long) {
        if (at < DateTimeUtils.now() - DAY_MILLIS) return remove(type, refId)
        put(Reminder(petId = petId, type = type, refId = refId, title = title, message = msg, triggerAt = at))
    }

    private suspend fun put(r: Reminder) {
        val old = repo.getByRefSlot(r.type, r.refId, r.slot)
        val toSave = r.copy(id = old?.id ?: 0, enabled = old?.enabled ?: true, lastDoneAt = old?.lastDoneAt)
        val id = repo.save(toSave)
        val saved = toSave.copy(id = id)
        if (saved.enabled) scheduler.schedule(saved) else scheduler.cancel(id)
    }

    private companion object {
        const val MORNING = 9 * 60
    }
}
