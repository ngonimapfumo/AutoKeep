package com.ngonim.autokeep.domain.health

import com.ngonim.autokeep.domain.model.Dashboard
import com.ngonim.autokeep.domain.model.HealthStatus
import com.ngonim.autokeep.domain.model.MaintenanceReminder
import com.ngonim.autokeep.domain.model.ServiceForecast
import com.ngonim.autokeep.domain.model.ServiceItem
import com.ngonim.autokeep.domain.model.Vehicle
import com.ngonim.autokeep.domain.model.VehicleHealthBadge
import com.ngonim.autokeep.domain.time.Dates
import kotlin.math.roundToInt

object HealthCalculator {
    const val DUE_SOON_DISTANCE_FRACTION = 0.20
    const val DUE_SOON_DAYS = 30

    fun forecast(item: ServiceItem, currentMileage: Int, todayEpochDay: Int): ServiceForecast {
        val nextMileage = item.lastDoneMileage?.let { it + item.intervalDistance }
        val nextEpochDay = if (item.lastDoneAtEpochDay != null && item.intervalMonths != null) {
            Dates.plusMonths(item.lastDoneAtEpochDay, item.intervalMonths)
        } else {
            null
        }
        val remainingDistance = nextMileage?.let { it - currentMileage }
        val remainingDays = nextEpochDay?.let { it - todayEpochDay }
        val status = status(
            remainingDistance = remainingDistance,
            remainingDays = remainingDays,
            intervalDistance = item.intervalDistance,
        )
        return ServiceForecast(
            item = item,
            status = status,
            remainingDistance = remainingDistance,
            remainingDays = remainingDays,
            nextMileage = nextMileage,
            nextEpochDay = nextEpochDay,
        )
    }

    fun dashboard(vehicle: Vehicle, items: List<ServiceItem>, todayEpochDay: Int): Dashboard {
        val forecasts = items
            .sortedWith(compareBy(ServiceItem::sortOrder, ServiceItem::name))
            .map { forecast(it, vehicle.currentMileage, todayEpochDay) }
        val needsAttention = forecasts
            .filter { it.status == HealthStatus.OVERDUE || it.status == HealthStatus.DUE_SOON || it.status == HealthStatus.UNKNOWN }
            .sortedWith(
                compareBy<ServiceForecast> { attentionRank(it.status) }
                    .thenBy { it.remainingDistance ?: Int.MIN_VALUE }
                    .thenBy { it.item.name },
            )
        val reminders = forecasts
            .filter { it.status == HealthStatus.DUE_SOON && it.remainingDistance != null }
            .map { row ->
                MaintenanceReminder(
                    vehicleName = vehicle.displayName,
                    itemName = row.item.name,
                    remainingDistance = row.remainingDistance!!,
                    mileageUnit = vehicle.mileageUnit,
                )
            }
        return Dashboard(
            vehicle = vehicle,
            goodCount = forecasts.count { it.status == HealthStatus.GOOD },
            dueSoonCount = forecasts.count { it.status == HealthStatus.DUE_SOON },
            overdueCount = forecasts.count { it.status == HealthStatus.OVERDUE },
            unknownCount = forecasts.count { it.status == HealthStatus.UNKNOWN },
            needsAttention = needsAttention,
            items = forecasts,
            reminders = reminders,
        )
    }

    fun badge(dashboard: Dashboard): VehicleHealthBadge = when {
        dashboard.overdueCount > 0 -> VehicleHealthBadge.OVERDUE
        dashboard.dueSoonCount > 0 -> VehicleHealthBadge.DUE_SOON
        else -> VehicleHealthBadge.ALL_GOOD
    }

    fun nextDue(items: List<ServiceForecast>): ServiceForecast? =
        items
            .filter { it.remainingDistance != null }
            .minByOrNull { it.remainingDistance!! }

    internal fun status(
        remainingDistance: Int?,
        remainingDays: Int?,
        intervalDistance: Int,
    ): HealthStatus {
        if (remainingDistance == null && remainingDays == null) return HealthStatus.UNKNOWN
        if (remainingDistance != null && remainingDistance < 0) return HealthStatus.OVERDUE
        if (remainingDays != null && remainingDays < 0) return HealthStatus.OVERDUE
        val dueSoonDistance = (intervalDistance * DUE_SOON_DISTANCE_FRACTION).roundToInt()
        val distanceSoon = remainingDistance != null && remainingDistance <= dueSoonDistance
        val timeSoon = remainingDays != null && remainingDays <= DUE_SOON_DAYS
        if (distanceSoon || timeSoon) return HealthStatus.DUE_SOON
        return HealthStatus.GOOD
    }

    private fun attentionRank(status: HealthStatus): Int = when (status) {
        HealthStatus.OVERDUE -> 0
        HealthStatus.DUE_SOON -> 1
        HealthStatus.UNKNOWN -> 2
        HealthStatus.GOOD -> 3
    }
}
