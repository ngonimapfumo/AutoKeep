package com.ngonim.autokeep.domain.cost

import com.ngonim.autokeep.domain.model.ServiceCategory
import com.ngonim.autokeep.domain.model.ServiceItem
import com.ngonim.autokeep.domain.model.ServiceVisit
import com.ngonim.autokeep.domain.time.Dates
import org.junit.Assert.assertEquals
import org.junit.Test

class CostCalculatorTest {
    @Test
    fun groupsVisitCostBySharedCategory() {
        val oil = item(1, "Engine Oil", ServiceCategory.ENGINE)
        val filter = item(2, "Oil Filter", ServiceCategory.ENGINE)
        val cvt = item(3, "CVT Fluid", ServiceCategory.TRANSMISSION)
        val tyres = item(4, "Tyres", ServiceCategory.TYRES)
        val cabin = item(5, "Cabin Filter", ServiceCategory.OTHER)

        val visits = listOf(
            visit(1, Dates.epochDay(2026, 3, 1), listOf(oil, filter), 6_500),
            visit(2, Dates.epochDay(2026, 5, 1), listOf(cvt), 9_000),
            visit(3, Dates.epochDay(2026, 7, 1), listOf(tyres), 12_000),
            visit(4, Dates.epochDay(2026, 8, 1), listOf(cabin), 3_500),
            visit(5, Dates.epochDay(2025, 12, 1), listOf(oil), 99_000),
        )

        val costs = CostCalculator.yearly(
            year = 2026,
            visits = visits,
            itemsById = listOf(oil, filter, cvt, tyres, cabin).associateBy { it.id },
        )

        assertEquals(31_000L, costs.totalMinor)
        assertEquals(6_500L, costs.byCategory[ServiceCategory.ENGINE])
        assertEquals(9_000L, costs.byCategory[ServiceCategory.TRANSMISSION])
        assertEquals(12_000L, costs.byCategory[ServiceCategory.TYRES])
        assertEquals(3_500L, costs.byCategory[ServiceCategory.OTHER])
    }

    @Test
    fun mixedCategoryVisit_goesToOther() {
        val oil = item(1, "Engine Oil", ServiceCategory.ENGINE)
        val tyres = item(4, "Tyres", ServiceCategory.TYRES)
        val visit = visit(1, Dates.epochDay(2026, 4, 1), listOf(oil, tyres), 8_000)

        val costs = CostCalculator.yearly(
            year = 2026,
            visits = listOf(visit),
            itemsById = listOf(oil, tyres).associateBy { it.id },
        )

        assertEquals(8_000L, costs.byCategory[ServiceCategory.OTHER])
    }

    private fun item(id: Long, name: String, category: ServiceCategory) = ServiceItem(
        id = id,
        vehicleId = 1,
        name = name,
        category = category,
        catalogKey = name,
        intervalDistance = 8_000,
        intervalMonths = 12,
        lastDoneMileage = null,
        lastDoneAtEpochDay = null,
        sortOrder = 0,
    )

    private fun visit(
        id: Long,
        day: Int,
        done: List<ServiceItem>,
        costMinor: Long,
    ) = ServiceVisit(
        id = id,
        vehicleId = 1,
        mileage = 60_000,
        performedAtEpochDay = day,
        itemIds = done.map { it.id },
        itemNames = done.map { it.name },
        costMinor = costMinor,
        workshop = "ABC Motors",
        notes = null,
        receiptUri = null,
    )
}
