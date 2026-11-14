package com.diary.petcare.di

import android.content.Context
import androidx.room.Room
import com.diary.petcare.core.Constants
import com.diary.petcare.data.local.PetDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PetDatabase =
        Room.databaseBuilder(context, PetDatabase::class.java, Constants.DATABASE_NAME).build()

    @Provides fun petDao(db: PetDatabase) = db.petDao()
    @Provides fun feedingDao(db: PetDatabase) = db.feedingDao()
    @Provides fun vaccineDao(db: PetDatabase) = db.vaccineDao()
    @Provides fun medicineDao(db: PetDatabase) = db.medicineDao()
    @Provides fun vetVisitDao(db: PetDatabase) = db.vetVisitDao()
    @Provides fun weightDao(db: PetDatabase) = db.weightDao()
    @Provides fun groomingDao(db: PetDatabase) = db.groomingDao()
    @Provides fun expenseDao(db: PetDatabase) = db.expenseDao()
    @Provides fun noteDao(db: PetDatabase) = db.noteDao()
    @Provides fun photoDao(db: PetDatabase) = db.photoDao()
    @Provides fun reminderDao(db: PetDatabase) = db.reminderDao()
}
