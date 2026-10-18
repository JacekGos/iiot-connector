package com.iiot.connector.contract.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.format.DateTimeParseException

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Instant {
        val raw = decoder.decodeString()
        return try {
            Instant.parse(raw)
        } catch (e: DateTimeParseException) {
            throw SerializationException("Invalid Instant format: '$raw'", e)
        }
    }
}
