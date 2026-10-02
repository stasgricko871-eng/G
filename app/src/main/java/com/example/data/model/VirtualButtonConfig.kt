package com.example.data.model

import org.json.JSONObject

data class VirtualButtonConfig(
    val id: String,
    val label: String,
    val keyId: String,
    val xFraction: Float,
    val yFraction: Float,
    val widthDp: Int = 64,
    val heightDp: Int = 64,
    val shape: ButtonShape = ButtonShape.CIRCLE,
    val style: ButtonStyle = ButtonStyle.PIXEL,
    val opacity: Float = 0.85f,
    val isDPad: Boolean = false,
    val isJoystick: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("label", label)
            put("keyId", keyId)
            put("xFraction", xFraction.toDouble())
            put("yFraction", yFraction.toDouble())
            put("widthDp", widthDp)
            put("heightDp", heightDp)
            put("shape", shape.name)
            put("style", style.name)
            put("opacity", opacity.toDouble())
            put("isDPad", isDPad)
            put("isJoystick", isJoystick)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): VirtualButtonConfig {
            return VirtualButtonConfig(
                id = json.optString("id", java.util.UUID.randomUUID().toString()),
                label = json.optString("label", "Z"),
                keyId = json.optString("keyId", "Z"),
                xFraction = json.optDouble("xFraction", 0.5).toFloat(),
                yFraction = json.optDouble("yFraction", 0.5).toFloat(),
                widthDp = json.optInt("widthDp", 64),
                heightDp = json.optInt("heightDp", 64),
                shape = runCatching { ButtonShape.valueOf(json.optString("shape", ButtonShape.CIRCLE.name)) }.getOrDefault(ButtonShape.CIRCLE),
                style = runCatching { ButtonStyle.valueOf(json.optString("style", ButtonStyle.PIXEL.name)) }.getOrDefault(ButtonStyle.PIXEL),
                opacity = json.optDouble("opacity", 0.85).toFloat(),
                isDPad = json.optBoolean("isDPad", false),
                isJoystick = json.optBoolean("isJoystick", false)
            )
        }
    }
}
