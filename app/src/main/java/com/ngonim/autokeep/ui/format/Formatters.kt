package com.ngonim.autokeep.ui.format

import com.ngonim.autokeep.domain.model.ExpenseCategory
import com.ngonim.autokeep.domain.model.HealthStatus
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.ServiceCategory
import com.ngonim.autokeep.domain.model.ServiceForecast
import com.ngonim.autokeep.domain.model.Transmission
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

fun MileageUnit.suffix(): String = if (this == MileageUnit.KILOMETERS) "km" else "mi"

fun formatDistance(value: Int, unit: MileageUnit): String =
    "%,d %s".format(Locale.getDefault(), value, unit.suffix())

fun formatMileage(value: Int, unit: MileageUnit): String = formatDistance(value, unit)

fun formatEpochDay(epochDay: Int, locale: Locale = Locale.getDefault()): String =
    formatEpochDay(epochDay, "d MMMM yyyy", locale)

fun formatShortDate(epochDay: Int, locale: Locale = Locale.getDefault()): String =
    formatEpochDay(epochDay, "d MMM yyyy", locale)

private fun formatEpochDay(epochDay: Int, pattern: String, locale: Locale): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = epochDay.toLong() * 86_400_000L
    return SimpleDateFormat(pattern, locale).format(calendar.time)
}

fun epochDayToMillis(epochDay: Int): Long = epochDay.toLong() * 86_400_000L

fun millisToEpochDay(millis: Long): Int = (millis / 86_400_000L).toInt()

fun formatMoney(minor: Long?, currencyCode: String?): String {
    if (minor == null) return "—"
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    currencyCode?.let {
        runCatching { format.currency = Currency.getInstance(it) }
    }
    return format.format(minor / 100.0)
}

fun parseMoneyToMinor(text: String): Long? {
    val cleaned = text.trim().replace(",", "")
    if (cleaned.isEmpty()) return null
    val value = cleaned.toDoubleOrNull() ?: return null
    return Math.round(value * 100.0)
}

fun ServiceForecast.remainingLabel(unit: MileageUnit): String {
    return when (status) {
        HealthStatus.UNKNOWN -> "Last service not set"
        HealthStatus.OVERDUE -> overdueLabel(unit)
        HealthStatus.DUE_SOON, HealthStatus.GOOD -> upcomingLabel(unit)
    }
}

fun ServiceForecast.remainingLabelCompact(unit: MileageUnit): String {
    return when {
        remainingDistance != null && remainingDistance < 0 ->
            formatDistance(abs(remainingDistance), unit)
        remainingDistance != null -> formatDistance(remainingDistance, unit)
        remainingDays != null && remainingDays < 0 -> "${abs(remainingDays)} days"
        remainingDays != null -> "$remainingDays days"
        else -> "Not set"
    }
}

fun ServiceForecast.nextServiceLabel(unit: MileageUnit): String {
    val mileage = nextMileage?.let { formatMileage(it, unit) }
    val date = nextEpochDay?.let { formatEpochDay(it) }
    return when {
        mileage != null && date != null -> "$mileage  ·  $date"
        mileage != null -> mileage
        date != null -> date
        else -> "Set last service to calculate the next one"
    }
}

fun Powertrain.label(): String = when (this) {
    Powertrain.GASOLINE -> "Petrol"
    Powertrain.DIESEL -> "Diesel"
    Powertrain.HYBRID -> "Hybrid"
    Powertrain.ELECTRIC -> "Electric"
}

fun Transmission.label(): String = when (this) {
    Transmission.MANUAL -> "Manual"
    Transmission.AUTOMATIC -> "Automatic"
    Transmission.CVT -> "CVT"
}

fun ServiceCategory.label(): String = when (this) {
    ServiceCategory.ENGINE -> "Engine"
    ServiceCategory.TRANSMISSION -> "Transmission"
    ServiceCategory.BRAKES -> "Brakes"
    ServiceCategory.COOLING -> "Cooling"
    ServiceCategory.ELECTRICAL -> "Electrical"
    ServiceCategory.TYRES -> "Tyres"
    ServiceCategory.OTHER -> "Other"
}

fun ExpenseCategory.label(): String = when (this) {
    ExpenseCategory.FUEL -> "Fuel"
    ExpenseCategory.REPAIRS -> "Repairs"
    ExpenseCategory.PARTS -> "Parts"
    ExpenseCategory.OTHER -> "Other"
}

fun HealthStatus.label(): String = when (this) {
    HealthStatus.GOOD -> "Good"
    HealthStatus.DUE_SOON -> "Due soon"
    HealthStatus.OVERDUE -> "Overdue"
    HealthStatus.UNKNOWN -> "Not set"
}

fun vehicleMeta(
    year: Int?,
    powertrain: Powertrain,
    transmission: Transmission,
    engineSize: String? = null,
    nickname: String? = null,
): String = listOfNotNull(
    nickname?.takeIf { it.isNotBlank() },
    year?.toString(),
    engineSize?.takeIf { it.isNotBlank() },
    powertrain.label(),
    transmission.label(),
).joinToString(" • ")

private fun ServiceForecast.overdueLabel(unit: MileageUnit): String {
    val parts = buildList {
        if (remainingDistance != null && remainingDistance < 0) {
            add("${formatDistance(abs(remainingDistance), unit)} overdue")
        }
        if (remainingDays != null && remainingDays < 0) {
            add("${abs(remainingDays)} days overdue")
        }
    }
    return parts.joinToString(" · ").ifEmpty { "Overdue" }
}

private fun ServiceForecast.upcomingLabel(unit: MileageUnit): String {
    val parts = buildList {
        if (remainingDistance != null) {
            add("${formatDistance(remainingDistance, unit)} remaining")
        }
        if (remainingDays != null) {
            add(
                if (remainingDays == 1) "1 day remaining" else "$remainingDays days remaining",
            )
        }
    }
    return parts.joinToString(" · ").ifEmpty { status.label() }
}
