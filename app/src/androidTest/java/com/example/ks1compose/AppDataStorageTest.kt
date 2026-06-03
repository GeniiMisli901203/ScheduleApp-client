package com.example.ks1compose

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ks1compose.data.datasource.remote.TokenManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDataStorageTest {

    private lateinit var appContext: Context

    @Before
    fun setup() {
        appContext = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testAppContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.example.ks1compose", context.packageName)
    }

    @Test
    fun testTokenManagerInitialization() {
        TokenManager.init(appContext)
        assertTrue(TokenManager.isInitialized())
    }

    @Test
    fun testTokenManagerSaveAndClear() {
        TokenManager.init(appContext)

        TokenManager.authToken = "test-token"
        assertEquals("test-token", TokenManager.authToken)

        TokenManager.authToken = null
        assertNull(TokenManager.authToken)
    }

    @Test
    fun testDataStoreSaveAndRead() {
        val prefs = appContext.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("test_key", "test_value")
        editor.apply()

        val savedValue = prefs.getString("test_key", null)
        assertEquals("test_value", savedValue)
    }

    @Test
    fun testDataStoreClear() {
        val prefs = appContext.getSharedPreferences("test_prefs_clear", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("key1", "value1")
        editor.putString("key2", "value2")
        editor.apply()

        editor.clear().apply()

        val value1 = prefs.getString("key1", null)
        val value2 = prefs.getString("key2", null)

        assertNull(value1)
        assertNull(value2)
    }

    @Test
    fun testFirstLaunchFlag() {
        val prefs = appContext.getSharedPreferences("first_launch_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val isFirstBefore = !prefs.contains("first_launch_done")
        assertTrue(isFirstBefore)

        editor.putBoolean("first_launch_done", true)
        editor.apply()

        val isFirstAfter = !prefs.contains("first_launch_done")
        assertFalse(isFirstAfter)
    }

    @Test
    fun testSaveUserRole() {
        val prefs = appContext.getSharedPreferences("user_role_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_role", "student")
        editor.apply()

        val role = prefs.getString("user_role", null)
        assertEquals("student", role)
    }

    @Test
    fun testSaveUserClass() {
        val prefs = appContext.getSharedPreferences("user_class_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_class", "10А")
        editor.apply()

        val userClass = prefs.getString("user_class", null)
        assertEquals("10А", userClass)
    }

    @Test
    fun testSaveUserSchool() {
        val prefs = appContext.getSharedPreferences("user_school_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_school", "Школа №1")
        editor.apply()

        val school = prefs.getString("user_school", null)
        assertEquals("Школа №1", school)
    }

    @Test
    fun testSaveUserEmail() {
        val prefs = appContext.getSharedPreferences("user_email_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_email", "student@school.com")
        editor.apply()

        val email = prefs.getString("user_email", null)
        assertEquals("student@school.com", email)
    }

    @Test
    fun testSaveUserLogin() {
        val prefs = appContext.getSharedPreferences("user_login_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_login", "ivan123")
        editor.apply()

        val login = prefs.getString("user_login", null)
        assertEquals("ivan123", login)
    }

    @Test
    fun testSaveDarkThemePreference() {
        val prefs = appContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putBoolean("dark_theme", true)
        editor.apply()

        val isDark = prefs.getBoolean("dark_theme", false)
        assertTrue(isDark)
    }

    @Test
    fun testSaveAndUpdateUserData() {
        val prefs = appContext.getSharedPreferences("update_user_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_name", "Иван")
        editor.putString("user_sname", "Иванов")
        editor.apply()

        assertEquals("Иван", prefs.getString("user_name", null))
        assertEquals("Иванов", prefs.getString("user_sname", null))

        editor.putString("user_name", "Петр")
        editor.apply()

        assertEquals("Петр", prefs.getString("user_name", null))
    }

    @Test
    fun testRemoveUserData() {
        val prefs = appContext.getSharedPreferences("remove_user_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_id", "12345")
        editor.putString("user_token", "token123")
        editor.apply()

        assertNotNull(prefs.getString("user_id", null))
        assertNotNull(prefs.getString("user_token", null))

        editor.remove("user_id")
        editor.remove("user_token")
        editor.apply()

        assertNull(prefs.getString("user_id", null))
        assertNull(prefs.getString("user_token", null))
    }
}