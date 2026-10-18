package com.iiot.connector.contract

import com.iiot.connector.contract.serialization.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class DataEvent(
    val timestamp:
        @Serializable(with = InstantSerializer::class)
        Instant,
    val sourceId: SourceId,
    val tagName: TagName,
    val value: DataValue,
    val quality: DataQuality,
    val metadata: Map<String, String> = emptyMap(),
)
