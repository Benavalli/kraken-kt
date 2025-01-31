package model

data class Device(
    val id: Int,
    val pin: Int,
    val type: DeviceType,
    val state: DeviceState
)

enum class DeviceType {
    LIGHT,
    LIGHT_2,
    EXHAUST,
    INLINE_FAN,
    FAN,
    HUMIDIFIER,
    VALVE,
    PUMP,
    AIR_PUMP,
    UNKNOWN
}

enum class DeviceState(val value: Boolean) {
    ENABLED(true),
    DISABLED(false);
}
