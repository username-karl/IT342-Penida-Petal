package com.petal.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthFormValidatorTest {
    @Test
    fun loginRequiresValidEmailAndPassword() {
        assertEquals("Enter a valid email address.", AuthFormValidator.loginError("bad-email", "secret123!"))
        assertEquals("Enter your password.", AuthFormValidator.loginError("buyer@example.com", ""))
        assertNull(AuthFormValidator.loginError("buyer@example.com", "secret123!"))
    }

    @Test
    fun registerMatchesBackendPasswordRulesAndBuyerRole() {
        assertEquals("Enter your full name.", AuthFormValidator.registerError("", "buyer@example.com", "Secret123!"))
        assertEquals("Use at least 8 characters.", AuthFormValidator.registerError("Petal Buyer", "buyer@example.com", "Short1!"))
        assertEquals("Add at least one number and one special character.", AuthFormValidator.registerError("Petal Buyer", "buyer@example.com", "Password"))
        assertNull(AuthFormValidator.registerError("Petal Buyer", "buyer@example.com", "Secret123!"))
        assertEquals("BUYER", AuthFormValidator.buyerRole)
    }
}
