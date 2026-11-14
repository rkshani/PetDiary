package com.diary.petcare.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.diary.petcare.core.Constants
import com.diary.petcare.domain.model.AppLanguage
import com.diary.petcare.domain.model.AppSettings
import com.diary.petcare.domain.model.Currency
import com.diary.petcare.domain.model.ThemeMode
import com.diary.petcare.domain.model.WeightUnit
import com.diary.petcare.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private val store get() = context.dataStore

    override val settings: Flow<AppSettings> = store.data.map { p ->
        AppSettings(
            themeMode = p[THEME].toEnum(ThemeMode.SYSTEM),
            language = p[LANGUAGE].toEnum(AppLanguage.SYSTEM),
            weightUnit = p[WEIGHT_UNIT].toEnum(WeightUnit.KG),
            currency = p[CURRENCY].toEnum(Currency.PKR),
            onboardingDone = p[ONBOARDING_DONE] ?: false,
            lockEnabled = (p[LOCK_ENABLED] ?: false) && p[PIN_HASH] != null,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) = put(THEME, mode.name)

    override suspend fun setLanguage(language: AppLanguage) {
        put(LANGUAGE, language.name)
        LocaleStore.save(context, language)
    }

    override suspend fun setWeightUnit(unit: WeightUnit) = put(WEIGHT_UNIT, unit.name)
    override suspend fun setCurrency(currency: Currency) = put(CURRENCY, currency.name)

    override suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[ONBOARDING_DONE] = done }
    }

    override suspend fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toHex()
        store.edit {
            it[PIN_SALT] = salt
            it[PIN_HASH] = hash(pin, salt)
            it[LOCK_ENABLED] = true
        }
    }

    override suspend fun checkPin(pin: String): Boolean {
        val p = store.data.first()
        val salt = p[PIN_SALT] ?: return false
        val expected = p[PIN_HASH] ?: return false
        return MessageDigest.isEqual(hash(pin, salt).toByteArray(), expected.toByteArray())
    }

    override suspend fun disableLock() {
        store.edit {
            it[LOCK_ENABLED] = false
            it.remove(PIN_HASH)
            it.remove(PIN_SALT)
        }
    }

    private suspend fun put(key: Preferences.Key<String>, value: String) {
        store.edit { it[key] = value }
    }

    private fun hash(pin: String, salt: String): String {
        var bytes = (salt + pin).toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        repeat(HASH_ROUNDS) { bytes = md.digest(bytes) }
        return bytes.toHex()
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

    private companion object {
        const val HASH_ROUNDS = 10_000
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val CURRENCY = stringPreferencesKey("currency")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
    }
}
