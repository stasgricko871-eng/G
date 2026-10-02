package com.example.data.model

enum class ButtonStyle(val displayName: String) {
    PIXEL("Pixel Art (Ретро)"),
    MINIMAL("Minimal (Минимум)"),
    TRANSPARENT("Transparent (Прозрачный)"),
    OUTLINE("Outline (Только контур)")
}

enum class ButtonShape(val displayName: String) {
    CIRCLE("Круг"),
    SQUARE("Квадрат"),
    ROUNDED_RECT("Скруглённый"),
    DPAD_CROSS("Крестовина D-Pad"),
    DIAMOND("Ромб")
}
