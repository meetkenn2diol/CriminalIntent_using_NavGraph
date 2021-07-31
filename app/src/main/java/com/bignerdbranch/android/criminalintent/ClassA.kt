package com.bignerdbranch.android.criminalintent

import android.view.accessibility.AccessibilityEvent

/**
 * This class is just a helper class for converting java code to a Kotlin in android studio
 */
internal class CrimeListExecutor {
    private fun aMethod() {
        val str = javaClass.name
        val accessibilityEvent = AccessibilityEvent.obtain()
        accessibilityEvent.text.add("A pictur have been taken")
    }
}