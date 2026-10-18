package com.iiot.connector.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DataValue {
    @Serializable
    @SerialName("INT")
    data class IntValue(
        val value: Int,
    ) : DataValue()

    @Serializable
    @SerialName("LONG")
    data class LongValue(
        val value: Long,
    ) : DataValue()

    @Serializable
    @SerialName("DOUBLE")
    data class DoubleValue(
        val value: Double,
    ) : DataValue()

    @Serializable
    @SerialName("BOOLEAN")
    data class BooleanValue(
        val value: Boolean,
    ) : DataValue()

    @Serializable
    @SerialName("STRING")
    data class StringValue(
        val value: String,
    ) : DataValue()

    companion object {
        fun of(raw: Any): DataValue =
            when (raw) {
                is Int -> IntValue(raw)
                is Long -> LongValue(raw)
                is Double -> DoubleValue(raw)
                is Boolean -> BooleanValue(raw)
                is String -> StringValue(raw)
                else -> throw IllegalArgumentException("Unsupported DataValue type: ${raw::class.qualifiedName}")
            }
    }
}
