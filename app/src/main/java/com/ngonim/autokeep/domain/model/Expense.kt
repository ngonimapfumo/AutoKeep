package com.ngonim.autokeep.domain.model

data class Expense(
    val id: Long,
    val vehicleId: Long,
    val title: String,
    val category: ExpenseCategory,
    val costMinor: Long,
    val performedAtEpochDay: Int,
    val notes: String?,
)

data class AddExpense(
    val vehicleId: Long,
    val title: String,
    val category: ExpenseCategory,
    val costMinor: Long,
    val performedAtEpochDay: Int,
    val notes: String? = null,
)

data class LedgerEntry(
    val id: Long,
    val title: String,
    val category: ExpenseCategory,
    val costMinor: Long,
    val performedAtEpochDay: Int,
    val mileage: Int?,
)

enum class ExpenseFilter {
    ALL,
    FUEL,
    REPAIRS,
    PARTS,
}

data class ExpenseOverview(
    val year: Int,
    val totalMinor: Long,
    val previousYearTotalMinor: Long,
    val monthly: List<Long>,
    val entries: List<LedgerEntry>,
)
