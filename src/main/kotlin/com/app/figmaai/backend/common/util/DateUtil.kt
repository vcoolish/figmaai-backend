package com.app.figmaai.backend.common.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Suppress("unused")
object DateUtil {
    private const val DEFAULT_DATE_NUM_VALUE = 0
    private const val MAX_HOUR_VALUE = 23
    private const val MAX_MINUTE_VALUE = 59

    @JvmStatic
    val ZONE_UTC: ZoneId = ZoneId.of("UTC")

    @JvmStatic
    val CREATION_DATE: ZonedDateTime = ZonedDateTime.of(2020, 5, 1, 0, 0, 0, 0, ZONE_UTC)

    @JvmStatic
    fun toZonedDateTimeStartDay(date: Date?): ZonedDateTime? =
        toZonedDateTime(date)
            ?.withHour(DEFAULT_DATE_NUM_VALUE)
            ?.withMinute(DEFAULT_DATE_NUM_VALUE)
            ?.withSecond(DEFAULT_DATE_NUM_VALUE)

    @JvmStatic
    fun zonedDateTimeStartDay(): ZonedDateTime =
        ZonedDateTime.now(ZONE_UTC)
            .withHour(DEFAULT_DATE_NUM_VALUE)
            .withMinute(DEFAULT_DATE_NUM_VALUE)
            .withSecond(DEFAULT_DATE_NUM_VALUE)

    @JvmStatic
    fun zonedDateTimeEndDay(): ZonedDateTime =
        ZonedDateTime.now(ZONE_UTC)
            .withHour(MAX_HOUR_VALUE)
            .withMinute(MAX_MINUTE_VALUE)
            .withSecond(MAX_MINUTE_VALUE)

    @JvmStatic
    @JvmName("toNullableZonedDateTime")
    fun toZonedDateTime(date: Date?): ZonedDateTime? =
        date?.let(this::toZonedDateTime)

    @JvmStatic
    fun toZonedDateTime(date: Date): ZonedDateTime =
        date.toInstant().atZone(ZONE_UTC)

    @JvmStatic
    @JvmName("toNullableZonedDateTimeStartDay")
    fun toZonedDateTimeStartDay(date: LocalDate?): ZonedDateTime? = date
        ?.atStartOfDay(ZONE_UTC)
        ?.withHour(DEFAULT_DATE_NUM_VALUE)
        ?.withMinute(DEFAULT_DATE_NUM_VALUE)
        ?.withSecond(DEFAULT_DATE_NUM_VALUE)

    @JvmStatic
    fun toZonedDateTimeStartDay(date: LocalDate): ZonedDateTime = date
        .atStartOfDay(ZONE_UTC)
        .withHour(DEFAULT_DATE_NUM_VALUE)
        .withMinute(DEFAULT_DATE_NUM_VALUE)
        .withSecond(DEFAULT_DATE_NUM_VALUE)

    @JvmStatic
    @JvmName("toNullableZonedDateTimeEndDay")
    fun toZonedDateTimeEndDay(date: LocalDate?): ZonedDateTime? = date
        ?.atStartOfDay(ZONE_UTC)
        ?.withHour(MAX_HOUR_VALUE)
        ?.withMinute(MAX_MINUTE_VALUE)
        ?.withSecond(MAX_MINUTE_VALUE)

    @JvmStatic
    fun toZonedDateTimeEndDay(date: LocalDate): ZonedDateTime = date
        .atStartOfDay(ZONE_UTC)
        .withHour(MAX_HOUR_VALUE)
        .withMinute(MAX_MINUTE_VALUE)
        .withSecond(MAX_MINUTE_VALUE)

    @JvmStatic
    fun localDateNow(): LocalDate =
        zonedDateTimeNow().toLocalDate()

    @JvmStatic
    fun localDateTimeNow(): LocalDateTime =
        zonedDateTimeNow().toLocalDateTime()

    @JvmStatic
    fun zonedDateTimeNow(): ZonedDateTime =
        ZonedDateTime.now(ZONE_UTC)
}
