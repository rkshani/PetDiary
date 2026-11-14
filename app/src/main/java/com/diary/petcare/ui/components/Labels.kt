package com.diary.petcare.ui.components

import androidx.annotation.StringRes
import com.diary.petcare.R
import com.diary.petcare.domain.model.AppLanguage
import com.diary.petcare.domain.model.Currency
import com.diary.petcare.domain.model.ExpenseCategory
import com.diary.petcare.domain.model.Gender
import com.diary.petcare.domain.model.GroomingType
import com.diary.petcare.domain.model.MedicineKind
import com.diary.petcare.domain.model.PetType
import com.diary.petcare.domain.model.ReminderType
import com.diary.petcare.domain.model.ThemeMode
import com.diary.petcare.domain.model.WeightUnit

/** String resources and emoji for enum values shown in the UI. */

@get:StringRes
val PetType.label: Int
    get() = when (this) {
        PetType.CAT -> R.string.pet_type_cat
        PetType.DOG -> R.string.pet_type_dog
        PetType.BIRD -> R.string.pet_type_bird
        PetType.PARROT -> R.string.pet_type_parrot
        PetType.HEN -> R.string.pet_type_hen
        PetType.RABBIT -> R.string.pet_type_rabbit
        PetType.FISH -> R.string.pet_type_fish
        PetType.HAMSTER -> R.string.pet_type_hamster
        PetType.TURTLE -> R.string.pet_type_turtle
        PetType.GOAT -> R.string.pet_type_goat
        PetType.COW -> R.string.pet_type_cow
        PetType.HORSE -> R.string.pet_type_horse
        PetType.OTHER -> R.string.pet_type_other
    }

val PetType.emoji: String
    get() = when (this) {
        PetType.CAT -> "🐱"
        PetType.DOG -> "🐶"
        PetType.BIRD -> "🐦"
        PetType.PARROT -> "🦜"
        PetType.HEN -> "🐔"
        PetType.RABBIT -> "🐰"
        PetType.FISH -> "🐟"
        PetType.HAMSTER -> "🐹"
        PetType.TURTLE -> "🐢"
        PetType.GOAT -> "🐐"
        PetType.COW -> "🐄"
        PetType.HORSE -> "🐴"
        PetType.OTHER -> "🐾"
    }

@get:StringRes
val Gender.label: Int
    get() = when (this) {
        Gender.MALE -> R.string.gender_male
        Gender.FEMALE -> R.string.gender_female
        Gender.UNKNOWN -> R.string.gender_unknown
    }

@get:StringRes
val MedicineKind.label: Int
    get() = when (this) {
        MedicineKind.MEDICINE -> R.string.medicine_kind_medicine
        MedicineKind.DEWORMING -> R.string.medicine_kind_deworming
        MedicineKind.FLEA_TICK -> R.string.medicine_kind_flea_tick
        MedicineKind.SUPPLEMENT -> R.string.medicine_kind_supplement
    }

@get:StringRes
val GroomingType.label: Int
    get() = when (this) {
        GroomingType.BATH -> R.string.grooming_bath
        GroomingType.NAIL_CUT -> R.string.grooming_nail_cut
        GroomingType.HAIRCUT -> R.string.grooming_haircut
        GroomingType.BRUSHING -> R.string.grooming_brushing
        GroomingType.TEETH -> R.string.grooming_teeth
        GroomingType.EAR_CLEAN -> R.string.grooming_ear_clean
        GroomingType.OTHER -> R.string.grooming_other
    }

val GroomingType.emoji: String
    get() = when (this) {
        GroomingType.BATH -> "🛁"
        GroomingType.NAIL_CUT -> "💅"
        GroomingType.HAIRCUT -> "✂️"
        GroomingType.BRUSHING -> "🪮"
        GroomingType.TEETH -> "🦷"
        GroomingType.EAR_CLEAN -> "👂"
        GroomingType.OTHER -> "✨"
    }

@get:StringRes
val ExpenseCategory.label: Int
    get() = when (this) {
        ExpenseCategory.FOOD -> R.string.expense_food
        ExpenseCategory.VET -> R.string.expense_vet
        ExpenseCategory.MEDICINE -> R.string.expense_medicine
        ExpenseCategory.TOYS -> R.string.expense_toys
        ExpenseCategory.GROOMING -> R.string.expense_grooming
        ExpenseCategory.ACCESSORIES -> R.string.expense_accessories
        ExpenseCategory.OTHER -> R.string.expense_other
    }

val ExpenseCategory.emoji: String
    get() = when (this) {
        ExpenseCategory.FOOD -> "🍖"
        ExpenseCategory.VET -> "🩺"
        ExpenseCategory.MEDICINE -> "💊"
        ExpenseCategory.TOYS -> "🧸"
        ExpenseCategory.GROOMING -> "🛁"
        ExpenseCategory.ACCESSORIES -> "🎀"
        ExpenseCategory.OTHER -> "📦"
    }

@get:StringRes
val ReminderType.label: Int
    get() = when (this) {
        ReminderType.FEEDING -> R.string.reminder_type_feeding
        ReminderType.MEDICINE -> R.string.reminder_type_medicine
        ReminderType.VACCINE -> R.string.reminder_type_vaccine
        ReminderType.VET -> R.string.reminder_type_vet
        ReminderType.GROOMING -> R.string.reminder_type_grooming
        ReminderType.CUSTOM -> R.string.reminder_type_custom
    }

val ReminderType.emoji: String
    get() = when (this) {
        ReminderType.FEEDING -> "🍽️"
        ReminderType.MEDICINE -> "💊"
        ReminderType.VACCINE -> "💉"
        ReminderType.VET -> "🩺"
        ReminderType.GROOMING -> "🛁"
        ReminderType.CUSTOM -> "🔔"
    }

@get:StringRes
val ThemeMode.label: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }

@get:StringRes
val AppLanguage.label: Int
    get() = when (this) {
        AppLanguage.SYSTEM -> R.string.language_system
        AppLanguage.ENGLISH -> R.string.language_english
        AppLanguage.URDU -> R.string.language_urdu
    }

@get:StringRes
val WeightUnit.label: Int
    get() = when (this) {
        WeightUnit.KG -> R.string.unit_kg
        WeightUnit.LB -> R.string.unit_lb
    }

@get:StringRes
val Currency.label: Int
    get() = when (this) {
        Currency.PKR -> R.string.currency_pkr
        Currency.USD -> R.string.currency_usd
        Currency.INR -> R.string.currency_inr
        Currency.AED -> R.string.currency_aed
        Currency.SAR -> R.string.currency_sar
        Currency.GBP -> R.string.currency_gbp
        Currency.EUR -> R.string.currency_eur
    }
