package com.diary.petcare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diary.petcare.data.local.dao.ExpenseDao
import com.diary.petcare.data.local.dao.FeedingDao
import com.diary.petcare.data.local.dao.GroomingDao
import com.diary.petcare.data.local.dao.MedicineDao
import com.diary.petcare.data.local.dao.NoteDao
import com.diary.petcare.data.local.dao.PetDao
import com.diary.petcare.data.local.dao.PhotoDao
import com.diary.petcare.data.local.dao.ReminderDao
import com.diary.petcare.data.local.dao.VaccineDao
import com.diary.petcare.data.local.dao.VetVisitDao
import com.diary.petcare.data.local.dao.WeightDao
import com.diary.petcare.data.local.entity.ExpenseEntity
import com.diary.petcare.data.local.entity.FeedingLogEntity
import com.diary.petcare.data.local.entity.FeedingScheduleEntity
import com.diary.petcare.data.local.entity.GroomingEntity
import com.diary.petcare.data.local.entity.MedicineDoseEntity
import com.diary.petcare.data.local.entity.MedicineEntity
import com.diary.petcare.data.local.entity.NoteEntity
import com.diary.petcare.data.local.entity.PetEntity
import com.diary.petcare.data.local.entity.PhotoEntity
import com.diary.petcare.data.local.entity.ReminderEntity
import com.diary.petcare.data.local.entity.VaccineEntity
import com.diary.petcare.data.local.entity.VetVisitEntity
import com.diary.petcare.data.local.entity.WeightLogEntity

@Database(
    entities = [
        PetEntity::class,
        FeedingScheduleEntity::class,
        FeedingLogEntity::class,
        VaccineEntity::class,
        MedicineEntity::class,
        MedicineDoseEntity::class,
        VetVisitEntity::class,
        WeightLogEntity::class,
        GroomingEntity::class,
        ExpenseEntity::class,
        NoteEntity::class,
        PhotoEntity::class,
        ReminderEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun feedingDao(): FeedingDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun medicineDao(): MedicineDao
    abstract fun vetVisitDao(): VetVisitDao
    abstract fun weightDao(): WeightDao
    abstract fun groomingDao(): GroomingDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun noteDao(): NoteDao
    abstract fun photoDao(): PhotoDao
    abstract fun reminderDao(): ReminderDao
}
