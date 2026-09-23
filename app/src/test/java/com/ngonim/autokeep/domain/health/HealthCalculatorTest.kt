package com.ngonim.autokeep.domain.health

import com.ngonim.autokeep.domain.model.HealthStatus
import com.ngonim.autokeep.domain.model.MileageUnit
import com.ngonim.autokeep.domain.model.Powertrain
import com.ngonim.autokeep.domain.model.ServiceCategory
import com.ngonim.autokeep.domain.model.ServiceItem
import com.ngonim.autokeep.domain.model.Transmission
import com.ngonim.autokeep.domain.model.Vehicle
import com.ngonim.autokeep.domain.time.Dates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCalculatorTest {
    private val today = Dates.epochDay(2026, 8, 29)

    @Test
    fun recordedOil_nextServiceIsIntervalAway() {
        val oil = item(
            name = "Engine Oil",
            intervalDistance = 8_000,
            intervalMonths = 12,
            lastDoneMileage = 63_420,
            lastDoneAtEpochDay = today,
        )

        val forecast = HealthCalculator.forecast(oil, currentMileage = 63_420, todayEpochDay = today)

        assertEquals(71_420, forecast.nextMileage)
        assertEquals(Dates.plusMonths(today, 12), forecast.nextEpochDay)
        assertEquals(8_000, forecast.remainingDistance)
        assertEquals(HealthStatus.GOOD, forecast.status)
    }

    @Test
    fun hondaGraceDashboardCounts() {
        val vehicle = Vehicle(
            id = 1,
            nickname = null,
            make = "Honda",
            model = "Grace",
            year = 2015,
            engineSize = "1.5L",
            powertrain = Powertrain.HYBRID,
            transmission = Transmission.CVT,
            currentMileage = 63_420,
            mileageUnit = MileageUnit.KILOMETERS,
            photoUri = null,
            currencyCode = "USD",
        )
        val items = listOf(
            item(name = "CVT Fluid", intervalDistance = 40_000, lastDoneMileage = 21_020),
            item(name = "Engine Oil", intervalDistance = 8_000, lastDoneMileage = 57_000),
            item(name = "Air Filter", intervalDistance = 20_000, lastDoneMileage = 44_520),
            item(name = "Oil Filter", intervalDistance = 8_000, lastDoneMileage = 58_000),
            item(name = "Cabin Filter", intervalDistance = 15_000, lastDoneMileage = 55_000),
            item(name = "Coolant", intervalDistance = 40_000, lastDoneMileage = 40_000),
            item(name = "Brake Fluid", intervalDistance = 40_000, lastDoneMileage = 40_000),
            item(name = "Spark Plugs", intervalDistance = 40_000, lastDoneMileage = 40_000),
            item(name = "Tyres", intervalDistance = 40_000, lastDoneMileage = 40_000),
            item(name = "Battery", intervalDistance = 50_000, lastDoneMileage = 30_000),
            item(name = "Drive Belt", intervalDistance = 80_000, lastDoneMileage = 40_000),
        )

        val dashboard = HealthCalculator.dashboard(vehicle, items, today)

        assertEquals("Honda Grace", dashboard.vehicle.displayName)
        assertEquals(1, dashboard.overdueCount)
        assertEquals(2, dashboard.dueSoonCount)
        assertEquals(8, dashboard.goodCount)
        assertEquals(0, dashboard.unknownCount)
        assertEquals("CVT Fluid", dashboard.needsAttention.first().item.name)
        assertEquals(-2_400, dashboard.needsAttention.first().remainingDistance)
        val oilReminder = dashboard.reminders.first { it.itemName == "Engine Oil" }
        assertEquals(1_580, oilReminder.remainingDistance)
    }

    @Test
    fun whicheverComesFirst_dateCanMakeItemOverdue() {
        val lastDone = Dates.epochDay(2025, 7, 1)
        val oil = item(
            intervalDistance = 8_000,
            intervalMonths = 12,
            lastDoneMileage = 63_000,
            lastDoneAtEpochDay = lastDone,
        )

        val forecast = HealthCalculator.forecast(oil, currentMileage = 63_420, todayEpochDay = today)

        assertEquals(HealthStatus.OVERDUE, forecast.status)
        assertTrue(forecast.remainingDays != null && forecast.remainingDays < 0)
        assertTrue(forecast.remainingDistance != null && forecast.remainingDistance > 0)
    }

    @Test
    fun unknownWhenNeverServiced() {
        val forecast = HealthCalculator.forecast(
            item = item(lastDoneMileage = null, lastDoneAtEpochDay = null),
            currentMileage = 63_420,
            todayEpochDay = today,
        )
        assertEquals(HealthStatus.UNKNOWN, forecast.status)
    }

    private fun item(
        name: String = "Engine Oil",
        intervalDistance: Int = 8_000,
        intervalMonths: Int? = 12,
        lastDoneMileage: Int? = 63_420,
        lastDoneAtEpochDay: Int? = null,
    ) = ServiceItem(
        id = name.hashCode().toLong(),
        vehicleId = 1,
        name = name,
        category = ServiceCategory.ENGINE,
        catalogKey = name,
        intervalDistance = intervalDistance,
        intervalMonths = intervalMonths,
        lastDoneMileage = lastDoneMileage,
        lastDoneAtEpochDay = lastDoneAtEpochDay,
        sortOrder = 0,
    )
}
