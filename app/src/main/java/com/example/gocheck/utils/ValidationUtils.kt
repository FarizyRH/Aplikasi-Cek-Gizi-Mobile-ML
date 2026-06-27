// ValidationUtils.kt
package com.example.gocheck.utils

object ValidationUtils {

    fun isValidNumber(input: String): Boolean {
        return try {
            input.isNotEmpty() && input.toFloat() >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
    }
}
