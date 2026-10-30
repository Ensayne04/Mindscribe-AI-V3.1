package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.AppSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppSecurityManagerTest {

    private lateinit var context: Context
    private lateinit var securityManager: AppSecurityManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences(AppSecurityManager.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        securityManager = AppSecurityManager(context)
    }

    @Test
    fun `default pin is 1234 and verifies successfully`() {
        assertTrue(securityManager.isUsingDefaultPin())
        assertTrue(securityManager.verifyPin("1234"))
        assertFalse(securityManager.verifyPin("0000"))
        assertFalse(securityManager.verifyPin("9999"))
    }

    @Test
    fun `sha256 hash produces unique deterministic hash`() {
        val hash1 = AppSecurityManager.hashPin("1234")
        val hash2 = AppSecurityManager.hashPin("1234")
        val hash3 = AppSecurityManager.hashPin("4321")

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hash3)
        assertEquals(64, hash1.length) // SHA-256 hex string length
    }

    @Test
    fun `changePin updates pin when old pin is correct`() {
        val success = securityManager.changePin("1234", "5678")
        assertTrue(success)
        assertFalse(securityManager.isUsingDefaultPin())
        assertTrue(securityManager.verifyPin("5678"))
        assertFalse(securityManager.verifyPin("1234"))
    }

    @Test
    fun `changePin fails when old pin is incorrect`() {
        val success = securityManager.changePin("9999", "5678")
        assertFalse(success)
        assertTrue(securityManager.verifyPin("1234"))
        assertFalse(securityManager.verifyPin("5678"))
    }

    @Test
    fun `resetPinToDefault restores 1234`() {
        securityManager.changePin("1234", "9876")
        assertFalse(securityManager.isUsingDefaultPin())

        securityManager.resetPinToDefault()
        assertTrue(securityManager.isUsingDefaultPin())
        assertTrue(securityManager.verifyPin("1234"))
    }

    @Test
    fun `app lock and biometric settings can be toggled`() {
        assertTrue(securityManager.isAppLockEnabled())
        securityManager.setAppLockEnabled(false)
        assertFalse(securityManager.isAppLockEnabled())

        assertTrue(securityManager.isBiometricEnabled())
        securityManager.setBiometricEnabled(false)
        assertFalse(securityManager.isBiometricEnabled())
    }
}
