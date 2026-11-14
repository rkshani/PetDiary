package com.diary.petcare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diary.petcare.domain.model.Gender
import com.diary.petcare.domain.model.Pet
import com.diary.petcare.domain.model.PetType

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PetType,
    val breed: String,
    val gender: Gender,
    val birthDate: Long?,
    val color: String,
    val microchip: String,
    val notes: String,
    val photoPath: String?,
    val createdAt: Long,
)

fun PetEntity.toDomain() = Pet(id, name, type, breed, gender, birthDate, color, microchip, notes, photoPath, createdAt)

fun Pet.toEntity() = PetEntity(id, name, type, breed, gender, birthDate, color, microchip, notes, photoPath, createdAt)
