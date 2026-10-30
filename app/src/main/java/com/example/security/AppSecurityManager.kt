package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class AppSecurityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "offline_workspace_security_prefs"
        const val KEY_APP_LOCK_ENABLED = "key_app_lock_enabled"
        const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        const val KEY_PIN_HASH = "key_pin_hash"
        const val DEFAULT_PIN = "1234"

        fun hashPin(pin: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    init {
        // Initialize default configuration if first time launching
        if (!prefs.contains(KEY_PIN_HASH)) {
            prefs.edit().putString(KEY_PIN_HASH, hashPin(DEFAULT_PIN)).apply()
        }
        if (!prefs.contains(KEY_APP_LOCK_ENABLED)) {
            prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, true).apply()
        }
        if (!prefs.contains(KEY_BIOMETRIC_ENABLED)) {
            prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, true).apply()
        }
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK_ENABLED, true)

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun verifyPin(inputPin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, hashPin(DEFAULT_PIN)) ?: hashPin(DEFAULT_PIN)
        return hashPin(inputPin) == storedHash
    }

    fun setPin(newPin: String): Boolean {
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) return false
        prefs.edit().putString(KEY_PIN_HASH, hashPin(newPin)).apply()
        return true
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) return false
        return setPin(newPin)
    }

    fun resetPinToDefault() {
        prefs.edit().putString(KEY_PIN_HASH, hashPin(DEFAULT_PIN)).apply()
    }

    fun isUsingDefaultPin(): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, "")
        return storedHash == hashPin(DEFAULT_PIN)
    }
}
