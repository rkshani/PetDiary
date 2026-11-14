package com.diary.petcare.di

import com.diary.petcare.data.repository.ExpenseRepositoryImpl
import com.diary.petcare.data.repository.FeedingRepositoryImpl
import com.diary.petcare.data.repository.GroomingRepositoryImpl
import com.diary.petcare.data.repository.MedicineRepositoryImpl
import com.diary.petcare.data.repository.NoteRepositoryImpl
import com.diary.petcare.data.repository.PetRepositoryImpl
import com.diary.petcare.data.repository.PhotoRepositoryImpl
import com.diary.petcare.data.repository.ReminderRepositoryImpl
import com.diary.petcare.data.repository.VaccineRepositoryImpl
import com.diary.petcare.data.repository.VetVisitRepositoryImpl
import com.diary.petcare.data.repository.WeightRepositoryImpl
import com.diary.petcare.domain.repository.ExpenseRepository
import com.diary.petcare.domain.repository.FeedingRepository
import com.diary.petcare.domain.repository.GroomingRepository
import com.diary.petcare.domain.repository.MedicineRepository
import com.diary.petcare.domain.repository.NoteRepository
import com.diary.petcare.domain.repository.PetRepository
import com.diary.petcare.domain.repository.PhotoRepository
import com.diary.petcare.domain.repository.ReminderRepository
import com.diary.petcare.domain.repository.VaccineRepository
import com.diary.petcare.domain.repository.VetVisitRepository
import com.diary.petcare.domain.repository.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun pet(impl: PetRepositoryImpl): PetRepository
    @Binds abstract fun feeding(impl: FeedingRepositoryImpl): FeedingRepository
    @Binds abstract fun vaccine(impl: VaccineRepositoryImpl): VaccineRepository
    @Binds abstract fun medicine(impl: MedicineRepositoryImpl): MedicineRepository
    @Binds abstract fun vetVisit(impl: VetVisitRepositoryImpl): VetVisitRepository
    @Binds abstract fun weight(impl: WeightRepositoryImpl): WeightRepository
    @Binds abstract fun grooming(impl: GroomingRepositoryImpl): GroomingRepository
    @Binds abstract fun expense(impl: ExpenseRepositoryImpl): ExpenseRepository
    @Binds abstract fun note(impl: NoteRepositoryImpl): NoteRepository
    @Binds abstract fun photo(impl: PhotoRepositoryImpl): PhotoRepository
    @Binds abstract fun reminder(impl: ReminderRepositoryImpl): ReminderRepository
}
