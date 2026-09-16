package com.example.services

/**
 * Architectural contract placeholder for Accessibility Service.
 * Per project requirements:
 * "Do NOT implement Accessibility Service yet."
 * "Prepare the architecture for future features such as: ... Accessibility Service"
 */
interface AccessibilityContract {
  fun isServiceEnabled(): Boolean
  fun registerActionListener(onAction: (String) -> Unit)
  fun unregisterActionListener()
}
