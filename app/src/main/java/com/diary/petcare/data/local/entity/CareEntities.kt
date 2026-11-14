package com.diary.petcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diary.petcare.domain.model.Expense
import com.diary.petcare.domain.model.ExpenseCategory
import com.diary.petcare.domain.model.FeedingLog
import com.diary.petcare.domain.model.FeedingSchedule
import com.diary.petcare.domain.model.Grooming
import com.diary.petcare.domain.model.GroomingType
import com.diary.petcare.domain.model.Note
import com.diary.petcare.domain.model.Photo
import com.diary.petcare.domain.model.WeightLog

@Entity(
    tableName = "feeding_schedules",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId")],
)
data class FeedingScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val timeOfDay: Int,
    val food: String,
    val quantity: String,
    val reminderEnabled: Boolean,
)

fun FeedingScheduleEntity.toDomain() = FeedingSchedule(id, petId, timeOfDay, food, quantity, reminderEnabled)
fun FeedingSchedule.toEntity() = FeedingScheduleEntity(id, petId, timeOfDay, food, quantity, reminderEnabled)

@Entity(
    tableName = "feeding_logs",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId"), Index("fedAt")],
)
data class FeedingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val scheduleId: Long?,
    val fedAt: Long,
    val food: String,
    val quantity: String,
    val note: String,
)

fun FeedingLogEntity.toDomain() = FeedingLog(id, petId, scheduleId, fedAt, food, quantity, note)
fun FeedingLog.toEntity() = FeedingLogEntity(id, petId, scheduleId, fedAt, food, quantity, note)

@Entity(
    tableName = "weight_logs",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId")],
)
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val date: Long,
    val weightGrams: Long,
    val note: String,
)

fun WeightLogEntity.toDomain() = WeightLog(id, petId, date, weightGrams, note)
fun WeightLog.toEntity() = WeightLogEntity(id, petId, date, weightGrams, note)

@Entity(
    tableName = "groomings",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId")],
)
data class GroomingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val type: GroomingType,
    val date: Long,
    val repeatDays: Int,
    val note: String,
    val reminderEnabled: Boolean,
)

fun GroomingEntity.toDomain() = Grooming(id, petId, type, date, repeatDays, note, reminderEnabled)
fun Grooming.toEntity() = GroomingEntity(id, petId, type, date, repeatDays, note, reminderEnabled)

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId"), Index("date")],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val category: ExpenseCategory,
    val amountPaisa: Long,
    val date: Long,
    val note: String,
)

fun ExpenseEntity.toDomain() = Expense(id, petId, category, amountPaisa, date, note)
fun Expense.toEntity() = ExpenseEntity(id, petId, category, amountPaisa, date, note)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId")],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val date: Long,
    val title: String,
    val text: String,
)

fun NoteEntity.toDomain() = Note(id, petId, date, title, text)
fun Note.toEntity() = NoteEntity(id, petId, date, title, text)

@Entity(
    tableName = "photos",
    foreignKeys = [ForeignKey(PetEntity::class, ["id"], ["petId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("petId")],
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val path: String,
    val takenAt: Long,
    val caption: String,
)

fun PhotoEntity.toDomain() = Photo(id, petId, path, takenAt, caption)
fun Photo.toEntity() = PhotoEntity(id, petId, path, takenAt, caption)
