package com.diary.petcare.domain.model

/** Kinds of pets. Stored by name in the database, so never rename an entry. */
enum class PetType { CAT, DOG, BIRD, PARROT, HEN, RABBIT, FISH, HAMSTER, TURTLE, GOAT, COW, HORSE, OTHER }

enum class Gender { MALE, FEMALE, UNKNOWN }

enum class MedicineKind { MEDICINE, DEWORMING, FLEA_TICK, SUPPLEMENT }

enum class GroomingType { BATH, NAIL_CUT, HAIRCUT, BRUSHING, TEETH, EAR_CLEAN, OTHER }

enum class ExpenseCategory { FOOD, VET, MEDICINE, TOYS, GROOMING, ACCESSORIES, OTHER }

enum class ReminderType { FEEDING, MEDICINE, VACCINE, VET, GROOMING, CUSTOM }
