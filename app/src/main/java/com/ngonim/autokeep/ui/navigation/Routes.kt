package com.ngonim.autokeep.ui.navigation

enum class MainTab { HOME, VEHICLES, HISTORY, MORE }

object Routes {
    const val START = "start"
    const val ONBOARDING = "onboarding"
    const val ADD_VEHICLE = "add_vehicle?from={from}"
    const val MAIN = "main/{tab}"
    const val VEHICLE_DETAILS = "vehicle/{vehicleId}"
    const val MAINTENANCE_DETAIL = "item/{vehicleId}/{itemId}"
    const val RECORD = "record/{vehicleId}?itemId={itemId}&mode={mode}"
    const val REMINDERS = "reminders/{vehicleId}"
    const val EXPENSES = "expenses/{vehicleId}"
    const val SETTINGS = "settings"

    const val FROM_ONBOARDING = "onboarding"
    const val FROM_GARAGE = "garage"
    const val MODE_SERVICE = "service"
    const val MODE_EXPENSE = "expense"

    fun addVehicle(from: String) = "add_vehicle?from=$from"
    fun main(tab: MainTab) = "main/${tab.name}"
    fun vehicleDetails(vehicleId: Long) = "vehicle/$vehicleId"
    fun maintenanceDetail(vehicleId: Long, itemId: Long) = "item/$vehicleId/$itemId"
    fun record(vehicleId: Long, itemId: Long = 0L, mode: String = MODE_SERVICE) =
        "record/$vehicleId?itemId=$itemId&mode=$mode"
    fun reminders(vehicleId: Long) = "reminders/$vehicleId"
    fun expenses(vehicleId: Long) = "expenses/$vehicleId"
}
