package com.petal.core.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface SessionStore {
    fun saveSession(token: String, name: String?, email: String?, role: String?)
    fun token(): String?
    fun userName(): String?
    fun role(): String?
    fun clear()
}

class EncryptedSessionStore(context: Context) : SessionStore {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveSession(token: String, name: String?, email: String?, role: String?) {
        preferences.edit()
            .putString(TOKEN_KEY, token)
            .putString(NAME_KEY, name)
            .putString(EMAIL_KEY, email)
            .putString(ROLE_KEY, role)
            .apply()
    }

    override fun token(): String? = preferences.getString(TOKEN_KEY, null)

    override fun userName(): String? = preferences.getString(NAME_KEY, null)

    override fun role(): String? = preferences.getString(ROLE_KEY, null)

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "petal_secure_prefs"
        const val TOKEN_KEY = "jwt_token"
        const val NAME_KEY = "user_name"
        const val EMAIL_KEY = "user_email"
        const val ROLE_KEY = "user_role"
    }
}
