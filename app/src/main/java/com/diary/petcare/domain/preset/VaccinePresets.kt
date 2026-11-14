package com.diary.petcare.domain.preset

import com.diary.petcare.domain.model.PetType

/**
 * A ready-made vaccine entry.
 * [repeatMonths] is the usual gap until the next dose or booster.
 */
data class VaccinePreset(val name: String, val repeatMonths: Int)

/** Common vaccines per pet type. Users can edit the list before adding it to a pet. */
object VaccinePresets {
    private val dog = listOf(
        VaccinePreset("Rabies", 12),
        VaccinePreset("DHPP (Distemper, Hepatitis, Parvo, Parainfluenza)", 12),
        VaccinePreset("Leptospirosis", 12),
        VaccinePreset("Bordetella (Kennel cough)", 12),
        VaccinePreset("Canine influenza", 12),
        VaccinePreset("Lyme disease", 12),
    )
    private val cat = listOf(
        VaccinePreset("Rabies", 12),
        VaccinePreset("FVRCP (Feline distemper)", 12),
        VaccinePreset("FeLV (Feline leukemia)", 12),
        VaccinePreset("Chlamydia", 12),
    )
    private val rabbit = listOf(
        VaccinePreset("RHDV1 / RHDV2", 12),
        VaccinePreset("Myxomatosis", 12),
    )
    private val bird = listOf(
        VaccinePreset("Polyomavirus", 12),
    )
    private val hen = listOf(
        VaccinePreset("Marek's disease", 0),
        VaccinePreset("Newcastle disease", 3),
        VaccinePreset("Infectious bronchitis", 3),
        VaccinePreset("Gumboro (IBD)", 0),
        VaccinePreset("Fowl pox", 12),
    )
    private val goat = listOf(
        VaccinePreset("CDT (Clostridium + Tetanus)", 12),
        VaccinePreset("PPR", 36),
        VaccinePreset("Rabies", 12),
    )
    private val cow = listOf(
        VaccinePreset("FMD (Foot and mouth)", 6),
        VaccinePreset("HS (Hemorrhagic septicemia)", 12),
        VaccinePreset("Black quarter", 12),
        VaccinePreset("Lumpy skin disease", 12),
        VaccinePreset("Brucellosis", 0),
    )
    private val horse = listOf(
        VaccinePreset("Tetanus", 12),
        VaccinePreset("Rabies", 12),
        VaccinePreset("Equine influenza", 6),
        VaccinePreset("EEE / WEE", 12),
        VaccinePreset("West Nile virus", 12),
    )

    fun forType(type: PetType): List<VaccinePreset> = when (type) {
        PetType.DOG -> dog
        PetType.CAT -> cat
        PetType.RABBIT -> rabbit
        PetType.BIRD, PetType.PARROT -> bird
        PetType.HEN -> hen
        PetType.GOAT -> goat
        PetType.COW -> cow
        PetType.HORSE -> horse
        PetType.FISH, PetType.HAMSTER, PetType.TURTLE, PetType.OTHER -> emptyList()
    }
}
