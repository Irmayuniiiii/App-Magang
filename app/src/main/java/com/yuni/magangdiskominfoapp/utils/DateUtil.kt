package com.yuni.magangdiskominfoapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            dateFormatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun isValidDateRange(startDate: String, endDate: String): Boolean {
        val start = parseDate(startDate)
        val end = parseDate(endDate)

        if (start == null || end == null) return false

        return !start.after(end)
    }
}