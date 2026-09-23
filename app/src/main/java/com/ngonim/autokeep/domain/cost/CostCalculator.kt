package com.ngonim.autokeep.domain.cost

import com.ngonim.autokeep.domain.model.Expense
import com.ngonim.autokeep.domain.model.ExpenseCategory
import com.ngonim.autokeep.domain.model.ExpenseOverview
import com.ngonim.autokeep.domain.model.LedgerEntry
import com.ngonim.autokeep.domain.model.ServiceCategory
import com.ngonim.autokeep.domain.model.ServiceItem
import com.ngonim.autokeep.domain.model.ServiceVisit
import com.ngonim.autokeep.domain.model.YearlyCosts
import com.ngonim.autokeep.domain.time.Dates

object CostCalculator {
    fun yearly(year: Int, visits: List<ServiceVisit>, itemsById: Map<Long, ServiceItem>): YearlyCosts {
        val from = Dates.yearStart(year)
        val to = Dates.yearEnd(year)
        val inYear = visits.filter { it.performedAtEpochDay in from..to && it.costMinor != null }
        val byCategory = mutableMapOf<ServiceCategory, Long>()
        var total = 0L
        inYear.forEach { visit ->
            val amount = visit.costMinor ?: return@forEach
            total += amount
            val category = categoryFor(visit.itemIds, itemsById)
            byCategory[category] = (byCategory[category] ?: 0L) + amount
        }
        return YearlyCosts(year = year, totalMinor = total, byCategory = byCategory)
    }

    fun overview(
        year: Int,
        visits: List<ServiceVisit>,
        itemsById: Map<Long, ServiceItem>,
        expenses: List<Expense>,
    ): ExpenseOverview {
        val from = Dates.yearStart(year)
        val to = Dates.yearEnd(year)
        val previousFrom = Dates.yearStart(year - 1)
        val previousTo = Dates.yearEnd(year - 1)
        val visitEntries = visits.mapNotNull { visit ->
            val amount = visit.costMinor ?: return@mapNotNull null
            LedgerEntry(
                id = visit.id,
                title = visit.itemNames.joinToString(" + ").ifBlank { "Service" },
                category = expenseCategoryFor(categoryFor(visit.itemIds, itemsById)),
                costMinor = amount,
                performedAtEpochDay = visit.performedAtEpochDay,
                mileage = visit.mileage,
            )
        }
        val expenseEntries = expenses.map { expense ->
            LedgerEntry(
                id = expense.id + 1_000_000,
                title = expense.title,
                category = expense.category,
                costMinor = expense.costMinor,
                performedAtEpochDay = expense.performedAtEpochDay,
                mileage = null,
            )
        }
        val all = (visitEntries + expenseEntries).sortedByDescending { it.performedAtEpochDay }
        val inYear = all.filter { it.performedAtEpochDay in from..to }
        val previous = all.filter { it.performedAtEpochDay in previousFrom..previousTo }
        val monthly = LongArray(12)
        inYear.forEach { entry ->
            val month = Dates.monthOf(entry.performedAtEpochDay)
            monthly[month - 1] += entry.costMinor
        }
        return ExpenseOverview(
            year = year,
            totalMinor = inYear.sumOf { it.costMinor },
            previousYearTotalMinor = previous.sumOf { it.costMinor },
            monthly = monthly.toList(),
            entries = inYear,
        )
    }

    private fun expenseCategoryFor(category: ServiceCategory): ExpenseCategory = when (category) {
        ServiceCategory.TYRES, ServiceCategory.ELECTRICAL, ServiceCategory.OTHER -> ExpenseCategory.PARTS
        else -> ExpenseCategory.REPAIRS
    }

    internal fun categoryFor(itemIds: List<Long>, itemsById: Map<Long, ServiceItem>): ServiceCategory {
        val categories = itemIds.mapNotNull { itemsById[it]?.category }.toSet()
        return categories.singleOrNull() ?: ServiceCategory.OTHER
    }
}
