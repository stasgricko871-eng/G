package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ControllerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val buttons: List<VirtualButtonConfig>,
    val globalOpacity: Float = 0.85f,
    val isBuiltIn: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        val root = JSONObject()
        root.put("id", id)
        root.put("name", name)
        root.put("globalOpacity", globalOpacity.toDouble())
        root.put("isBuiltIn", isBuiltIn)

        val array = JSONArray()
        for (btn in buttons) {
            array.put(btn.toJsonObject())
        }
        root.put("buttons", array)
        return root
    }

    fun toJsonString(): String {
        return toJsonObject().toString(2)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): ControllerProfile {
            val buttonsList = mutableListOf<VirtualButtonConfig>()
            val array = json.optJSONArray("buttons")
            if (array != null) {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i)
                    if (obj != null) {
                        buttonsList.add(VirtualButtonConfig.fromJsonObject(obj))
                    }
                }
            }

            return ControllerProfile(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Новый профиль"),
                buttons = buttonsList,
                globalOpacity = json.optDouble("globalOpacity", 0.85).toFloat(),
                isBuiltIn = json.optBoolean("isBuiltIn", false)
            )
        }

        fun fromJsonString(jsonStr: String): Result<ControllerProfile> {
            return runCatching {
                val json = JSONObject(jsonStr)
                fromJsonObject(json)
            }
        }

        /**
         * Горизонтальный (Landscape) профиль со стрелочками (D-Pad).
         */
        fun createDefaultRetroRpg(): ControllerProfile {
            return ControllerProfile(
                id = "profile_retro_rpg",
                name = "Retro RPG (Стрелочки)",
                isBuiltIn = true,
                globalOpacity = 0.88f,
                buttons = listOf(
                    // Ретро-крестовина со стрелочками слева под большой палец в горизонтальном режиме
                    VirtualButtonConfig(
                        id = "dpad_main",
                        label = "D-Pad",
                        keyId = "DPAD",
                        xFraction = 0.14f,
                        yFraction = 0.68f,
                        widthDp = 145,
                        heightDp = 145,
                        shape = ButtonShape.DPAD_CROSS,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f,
                        isDPad = true,
                        isJoystick = false
                    ),
                    // Кнопка Z (Действие/Подтверждение) справа
                    VirtualButtonConfig(
                        id = "btn_z",
                        label = "Z",
                        keyId = "Z",
                        xFraction = 0.90f,
                        yFraction = 0.58f,
                        widthDp = 66,
                        heightDp = 66,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f
                    ),
                    // Кнопка X (Отмена/Рывок)
                    VirtualButtonConfig(
                        id = "btn_x",
                        label = "X",
                        keyId = "X",
                        xFraction = 0.81f,
                        yFraction = 0.74f,
                        widthDp = 66,
                        heightDp = 66,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f
                    ),
                    // Кнопка C (Меню/Инвентарь)
                    VirtualButtonConfig(
                        id = "btn_c",
                        label = "C",
                        keyId = "C",
                        xFraction = 0.92f,
                        yFraction = 0.78f,
                        widthDp = 66,
                        heightDp = 66,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f
                    )
                )
            )
        }

        /**
         * Горизонтальный (Landscape) профиль с аналоговым джойстиком.
         */
        fun createDefaultJoystickProfile(): ControllerProfile {
            return ControllerProfile(
                id = "profile_retro_joystick",
                name = "Retro Action (Джойстик)",
                isBuiltIn = true,
                globalOpacity = 0.88f,
                buttons = listOf(
                    // Аналоговый виртуальный джойстик слева
                    VirtualButtonConfig(
                        id = "joystick_main",
                        label = "Joystick",
                        keyId = "DPAD",
                        xFraction = 0.14f,
                        yFraction = 0.68f,
                        widthDp = 145,
                        heightDp = 145,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f,
                        isDPad = false,
                        isJoystick = true
                    ),
                    // Кнопка Z
                    VirtualButtonConfig(
                        id = "btn_z_joy",
                        label = "Z",
                        keyId = "Z",
                        xFraction = 0.90f,
                        yFraction = 0.58f,
                        widthDp = 66,
                        heightDp = 66,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f
                    ),
                    // Кнопка X
                    VirtualButtonConfig(
                        id = "btn_x_joy",
                        label = "X",
                        keyId = "X",
                        xFraction = 0.81f,
                        yFraction = 0.74f,
                        widthDp = 66,
                        heightDp = 66,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f
                    ),
                    // Кнопка C
                    VirtualButtonConfig(
                        id = "btn_c_joy",
                        label = "C",
                        keyId = "C",
                        xFraction = 0.92f,
                        yFraction = 0.78f,
                        widthDp = 66,
                        heightDp = 66,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.PIXEL,
                        opacity = 0.90f
                    )
                )
            )
        }

        fun createDefaultCustom(): ControllerProfile {
            return ControllerProfile(
                id = "profile_custom",
                name = "Custom (Свой)",
                isBuiltIn = false,
                globalOpacity = 0.90f,
                buttons = listOf(
                    VirtualButtonConfig(
                        id = "dpad_custom",
                        label = "D-Pad",
                        keyId = "DPAD",
                        xFraction = 0.14f,
                        yFraction = 0.68f,
                        widthDp = 145,
                        heightDp = 145,
                        shape = ButtonShape.DPAD_CROSS,
                        style = ButtonStyle.OUTLINE,
                        opacity = 0.90f,
                        isDPad = true,
                        isJoystick = false
                    ),
                    VirtualButtonConfig(
                        id = "btn_a",
                        label = "A",
                        keyId = "Z",
                        xFraction = 0.88f,
                        yFraction = 0.65f,
                        widthDp = 64,
                        heightDp = 64,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.OUTLINE,
                        opacity = 0.90f
                    ),
                    VirtualButtonConfig(
                        id = "btn_b",
                        label = "B",
                        keyId = "X",
                        xFraction = 0.77f,
                        yFraction = 0.75f,
                        widthDp = 64,
                        heightDp = 64,
                        shape = ButtonShape.CIRCLE,
                        style = ButtonStyle.OUTLINE,
                        opacity = 0.90f
                    )
                )
            )
        }
    }
}
