package com.example.data.model

import android.view.KeyEvent

/**
 * Supported hardware and virtual key definitions.
 * Extensible for any Android KeyEvent code.
 */
data class VirtualKey(
    val id: String,
    val displayName: String,
    val keyCode: Int,
    val category: KeyCategory = KeyCategory.ACTION
) {
    enum class KeyCategory(val title: String) {
        DPAD("D-Pad"),
        ACTION("Действия"),
        LETTERS("Буквы"),
        NUMBERS("Цифры"),
        SYSTEM("Системные")
    }

    companion object {
        val ALL_KEYS: List<VirtualKey> by lazy {
            val list = mutableListOf(
                // D-Pad
                VirtualKey("DPAD_UP", "↑ Вверх", KeyEvent.KEYCODE_DPAD_UP, KeyCategory.DPAD),
                VirtualKey("DPAD_DOWN", "↓ Вниз", KeyEvent.KEYCODE_DPAD_DOWN, KeyCategory.DPAD),
                VirtualKey("DPAD_LEFT", "← Влево", KeyEvent.KEYCODE_DPAD_LEFT, KeyCategory.DPAD),
                VirtualKey("DPAD_RIGHT", "→ Вправо", KeyEvent.KEYCODE_DPAD_RIGHT, KeyCategory.DPAD),

                // Common RPG Actions
                VirtualKey("Z", "Z (Действие/OK)", KeyEvent.KEYCODE_Z, KeyCategory.ACTION),
                VirtualKey("X", "X (Отмена/Бег)", KeyEvent.KEYCODE_X, KeyCategory.ACTION),
                VirtualKey("C", "C (Меню/Инвентарь)", KeyEvent.KEYCODE_C, KeyCategory.ACTION),

                // System & Modifiers
                VirtualKey("ENTER", "Enter", KeyEvent.KEYCODE_ENTER, KeyCategory.SYSTEM),
                VirtualKey("ESCAPE", "Escape", KeyEvent.KEYCODE_ESCAPE, KeyCategory.SYSTEM),
                VirtualKey("SPACE", "Space (Пробел)", KeyEvent.KEYCODE_SPACE, KeyCategory.SYSTEM),
                VirtualKey("SHIFT", "Shift", KeyEvent.KEYCODE_SHIFT_LEFT, KeyCategory.SYSTEM),
                VirtualKey("CTRL", "Ctrl", KeyEvent.KEYCODE_CTRL_LEFT, KeyCategory.SYSTEM),
                VirtualKey("TAB", "Tab", KeyEvent.KEYCODE_TAB, KeyCategory.SYSTEM),
                VirtualKey("BACKSPACE", "Backspace", KeyEvent.KEYCODE_DEL, KeyCategory.SYSTEM)
            )

            // Letters A-Z (excluding already added Z, X, C to avoid duplication)
            for (char in 'A'..'Y') {
                if (char == 'X') continue
                val keyCode = KeyEvent.KEYCODE_A + (char - 'A')
                list.add(VirtualKey(char.toString(), char.toString(), keyCode, KeyCategory.LETTERS))
            }

            // Numbers 0-9
            for (digit in 0..9) {
                val keyCode = KeyEvent.KEYCODE_0 + digit
                list.add(VirtualKey(digit.toString(), digit.toString(), keyCode, KeyCategory.NUMBERS))
            }

            list
        }

        fun findById(id: String): VirtualKey {
            return ALL_KEYS.find { it.id.equals(id, ignoreCase = true) }
                ?: VirtualKey("Z", "Z", KeyEvent.KEYCODE_Z, KeyCategory.ACTION)
        }
    }
}
