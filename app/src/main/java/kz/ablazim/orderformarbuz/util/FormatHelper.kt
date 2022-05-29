package kz.ablazim.orderformarbuz.util

import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "dd.MM.yyyy"

object FormatHelper {
    private val simpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)

    fun getDate(date: Date): String = simpleDateFormat.format(date)
}