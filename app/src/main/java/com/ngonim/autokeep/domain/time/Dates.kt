package com.ngonim.autokeep.domain.time

import java.util.Calendar
import java.util.TimeZone

object Dates {
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    fun todayEpochDay(): Int = localEpochDay(System.currentTimeMillis())

    fun localEpochDay(epochMs: Long): Int {
        val offset = TimeZone.getDefault().getOffset(epochMs)
        return ((epochMs + offset) / MS_PER_DAY).toInt()
    }

    fun epochDay(year: Int, month: Int, dayOfMonth: Int): Int {
        val calendar = Calendar.getInstance(utc)
        calendar.clear()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return (calendar.timeInMillis / MS_PER_DAY).toInt()
    }

    fun plusMonths(epochDay: Int, months: Int): Int {
        val calendar = Calendar.getInstance(utc)
        calendar.timeInMillis = epochDay.toLong() * MS_PER_DAY
        calendar.add(Calendar.MONTH, months)
        return (calendar.timeInMillis / MS_PER_DAY).toInt()
    }

    fun yearStart(year: Int): Int = epochDay(year, 1, 1)

    fun yearEnd(year: Int): Int = epochDay(year, 12, 31)

    fun yearOf(epochDay: Int): Int = calendarOf(epochDay).get(Calendar.YEAR)

    fun monthOf(epochDay: Int): Int = calendarOf(epochDay).get(Calendar.MONTH) + 1

    private fun calendarOf(epochDay: Int): Calendar {
        val calendar = Calendar.getInstance(utc)
        calendar.timeInMillis = epochDay.toLong() * MS_PER_DAY
        return calendar
    }

    private const val MS_PER_DAY = 86_400_000L
}
