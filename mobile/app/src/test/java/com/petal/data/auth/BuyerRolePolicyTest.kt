package com.petal.data.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuyerRolePolicyTest {
    @Test
    fun buyerRolesCanSeeBuyerAccountSurfaces() {
        assertTrue(BuyerRolePolicy.isBuyer("BUYER"))
        assertTrue(BuyerRolePolicy.isBuyer("ROLE_BUYER"))
    }

    @Test
    fun floristRolesCannotSeeBuyerAccountSurfaces() {
        assertFalse(BuyerRolePolicy.isBuyer("FLORIST"))
        assertFalse(BuyerRolePolicy.isBuyer("ROLE_FLORIST"))
        assertFalse(BuyerRolePolicy.isBuyer("artisan"))
    }
}
