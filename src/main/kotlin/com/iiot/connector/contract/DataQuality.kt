package com.iiot.connector.contract

import com.iiot.connector.contract.serialization.DataQualitySerializer
import kotlinx.serialization.Serializable

@Serializable(with = DataQualitySerializer::class)
sealed class DataQuality(
    val code: String,
) {
    data object Good : DataQuality("GOOD")

    data object Bad : DataQuality("BAD")

    data object Uncertain : DataQuality("UNCERTAIN")

    data object Unknown : DataQuality("UNKNOWN")

    companion object {
        fun fromCode(code: String): DataQuality? =
            when (code) {
                Good.code -> Good
                Bad.code -> Bad
                Uncertain.code -> Uncertain
                Unknown.code -> Unknown
                else -> null
            }
    }
}
