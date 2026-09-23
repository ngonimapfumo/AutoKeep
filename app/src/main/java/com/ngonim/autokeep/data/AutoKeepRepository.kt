package com.ngonim.autokeep.data

import androidx.room.withTransaction
import com.ngonim.autokeep.data.local.AutoKeepDatabase
import com.ngonim.autokeep.data.local.entity.ExpenseEntity
import com.ngonim.autokeep.data.local.entity.ServiceItemEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitItemEntity
import com.ngonim.autokeep.data.local.entity.VehicleEntity
import com.ngonim.autokeep.data.mapper.toDomain
import com.ngonim.autokeep.domain.cost.CostCalculator
import com.ngonim.autokeep.domain.health.HealthCalculator
import com.ngonim.autokeep.domain.model.AddCustomItem
import com.ngonim.autokeep.domain.model.AddExpense
import com.ngonim.autokeep.domain.model.AddVehicle
import com.ngonim.autokeep.domain.model.Dashboard
import com.ngonim.autokeep.domain.model.Expense
import com.ngonim.autokeep.domain.model.ExpenseOverview
import com.ngonim.autokeep.domain.model.RecordService
import com.ngonim.autokeep.domain.model.ServiceForecast
import com.ngonim.autokeep.domain.model.ServiceItem
import com.ngonim.autokeep.domain.model.ServiceVisit
import com.ngonim.autokeep.domain.model.Vehicle
import com.ngonim.autokeep.domain.model.YearlyCosts
import com.ngonim.autokeep.domain.schedule.DefaultMaintenanceSchedule
import com.ngonim.autokeep.domain.time.EpochClock
import com.ngonim.autokeep.domain.time.SystemEpochClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AutoKeepRepository(
    private val db: AutoKeepDatabase,
    private val clock: EpochClock = SystemEpochClock,
    private val schedule: DefaultMaintenanceSchedule = DefaultMaintenanceSchedule,
) {
    private val vehicles = db.vehicleDao()
    private val items = db.serviceItemDao()
    private val visits = db.serviceVisitDao()
    private val expenses = db.expenseDao()

    fun observeVehicles(): Flow<List<Vehicle>> =
        vehicles.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeVehicle(vehicleId: Long): Flow<Vehicle?> =
        vehicles.observeById(vehicleId).map { it?.toDomain() }

    fun observeDashboard(vehicleId: Long): Flow<Dashboard?> =
        combine(
            vehicles.observeById(vehicleId),
            items.observeForVehicle(vehicleId),
        ) { vehicle, itemRows ->
            vehicle?.let {
                HealthCalculator.dashboard(
                    vehicle = it.toDomain(),
                    items = itemRows.map { row -> row.toDomain() },
                    todayEpochDay = clock.todayEpochDay(),
                )
            }
        }

    fun observeHistory(vehicleId: Long): Flow<List<ServiceVisit>> =
        visits.observeForVehicle(vehicleId).map { rows -> rows.map { it.toDomain() } }

    fun observeHistoryForItem(vehicleId: Long, itemId: Long): Flow<List<ServiceVisit>> =
        observeHistory(vehicleId).map { rows -> rows.filter { itemId in it.itemIds } }

    fun observeYearlyCosts(vehicleId: Long, year: Int): Flow<YearlyCosts> =
        combine(
            visits.observeForVehicle(vehicleId),
            items.observeForVehicle(vehicleId),
        ) { visitRows, itemRows ->
            CostCalculator.yearly(
                year = year,
                visits = visitRows.map { it.toDomain() },
                itemsById = itemRows.map { it.toDomain() }.associateBy { it.id },
            )
        }

    fun observeExpenses(vehicleId: Long): Flow<List<Expense>> =
        expenses.observeForVehicle(vehicleId).map { rows -> rows.map { it.toDomain() } }

    fun observeExpenseOverview(vehicleId: Long, year: Int): Flow<ExpenseOverview> =
        combine(
            visits.observeForVehicle(vehicleId),
            items.observeForVehicle(vehicleId),
            expenses.observeForVehicle(vehicleId),
        ) { visitRows, itemRows, expenseRows ->
            CostCalculator.overview(
                year = year,
                visits = visitRows.map { it.toDomain() },
                itemsById = itemRows.map { it.toDomain() }.associateBy { it.id },
                expenses = expenseRows.map { it.toDomain() },
            )
        }

    fun observeTotalSpend(vehicleId: Long): Flow<Long> =
        combine(
            visits.observeTotalCostMinor(vehicleId),
            expenses.observeTotalCostMinor(vehicleId),
        ) { visitTotal, expenseTotal -> visitTotal + expenseTotal }

    suspend fun addVehicle(input: AddVehicle): Long {
        require(input.make.isNotBlank()) { "Make is required" }
        require(input.model.isNotBlank()) { "Model is required" }
        require(input.currentMileage >= 0) { "Mileage cannot be negative" }
        val now = System.currentTimeMillis()
        return db.withTransaction {
            val vehicleId = vehicles.insert(
                VehicleEntity(
                    make = input.make.trim(),
                    model = input.model.trim(),
                    year = input.year,
                    engineSize = input.engineSize?.trim()?.takeIf { it.isNotEmpty() },
                    powertrain = input.powertrain,
                    transmission = input.transmission,
                    currentMileage = input.currentMileage,
                    mileageUnit = input.mileageUnit,
                    nickname = input.nickname?.trim()?.takeIf { it.isNotEmpty() },
                    currencyCode = input.currencyCode,
                    photoUri = input.photoUri,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                ),
            )
            val seed = schedule.itemsFor(input.powertrain, input.transmission, input.mileageUnit)
            items.insertAll(
                seed.map { entry ->
                    ServiceItemEntity(
                        vehicleId = vehicleId,
                        name = entry.name,
                        category = entry.category,
                        catalogKey = entry.catalogKey,
                        intervalDistance = entry.intervalDistance,
                        intervalMonths = entry.intervalMonths,
                        sortOrder = entry.sortOrder,
                        createdAtEpochMs = now,
                    )
                },
            )
            vehicleId
        }
    }

    suspend fun setLastDone(itemId: Long, mileage: Int, performedAtEpochDay: Int) {
        require(mileage >= 0) { "Mileage cannot be negative" }
        items.updateLastDone(itemId, mileage, performedAtEpochDay)
    }

    suspend fun updateMileage(vehicleId: Long, mileage: Int) {
        require(mileage >= 0) { "Mileage cannot be negative" }
        vehicles.updateMileage(vehicleId, mileage, System.currentTimeMillis())
    }

    suspend fun updatePhoto(vehicleId: Long, photoUri: String?) {
        val current = vehicles.getById(vehicleId) ?: return
        if (current.photoUri != photoUri) {
            VehiclePhotoStore.delete(current.photoUri)
        }
        vehicles.updatePhoto(vehicleId, photoUri, System.currentTimeMillis())
    }

    suspend fun deleteVehicle(vehicleId: Long) {
        val current = vehicles.getById(vehicleId) ?: return
        VehiclePhotoStore.delete(current.photoUri)
        vehicles.delete(current)
    }

    suspend fun recordService(input: RecordService): List<ServiceForecast> {
        require(input.serviceItemIds.isNotEmpty()) { "Select at least one service item" }
        require(input.mileage >= 0) { "Mileage cannot be negative" }
        val now = System.currentTimeMillis()
        return db.withTransaction {
            val vehicle = vehicles.getById(input.vehicleId)
                ?: error("Vehicle not found")
            val selected = items.getByIds(input.serviceItemIds.toList())
            require(selected.size == input.serviceItemIds.size) { "Unknown service item" }
            require(selected.all { it.vehicleId == input.vehicleId }) {
                "Service items must belong to this vehicle"
            }

            val visitId = visits.insert(
                ServiceVisitEntity(
                    vehicleId = input.vehicleId,
                    mileage = input.mileage,
                    performedAtEpochDay = input.performedAtEpochDay,
                    costMinor = input.costMinor,
                    workshop = input.workshop?.trim()?.takeIf { it.isNotEmpty() },
                    notes = input.notes?.trim()?.takeIf { it.isNotEmpty() },
                    receiptUri = input.receiptUri,
                    createdAtEpochMs = now,
                ),
            )
            visits.insertItems(
                selected.map { item ->
                    ServiceVisitItemEntity(visitId = visitId, serviceItemId = item.id)
                },
            )
            selected.forEach { item ->
                items.updateLastDone(item.id, input.mileage, input.performedAtEpochDay)
            }
            if (input.mileage > vehicle.currentMileage) {
                vehicles.updateMileage(input.vehicleId, input.mileage, now)
            }

            val updatedItems = items.getByIds(input.serviceItemIds.toList()).map { it.toDomain() }
            val currentMileage = maxOf(vehicle.currentMileage, input.mileage)
            updatedItems
                .sortedWith(compareBy(ServiceItem::sortOrder, ServiceItem::name))
                .map { HealthCalculator.forecast(it, currentMileage, clock.todayEpochDay()) }
        }
    }

    suspend fun addExpense(input: AddExpense): Long {
        require(input.title.isNotBlank()) { "Title is required" }
        require(input.costMinor > 0) { "Enter a cost" }
        return expenses.insert(
            ExpenseEntity(
                vehicleId = input.vehicleId,
                title = input.title.trim(),
                category = input.category,
                costMinor = input.costMinor,
                performedAtEpochDay = input.performedAtEpochDay,
                notes = input.notes?.trim()?.takeIf { it.isNotEmpty() },
                createdAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun addCustomItem(input: AddCustomItem): Long {
        require(input.name.isNotBlank()) { "Name is required" }
        require(input.intervalDistance > 0) { "Interval must be greater than zero" }
        val existing = items.getForVehicle(input.vehicleId)
        return items.insert(
            ServiceItemEntity(
                vehicleId = input.vehicleId,
                name = input.name.trim(),
                category = input.category,
                catalogKey = null,
                intervalDistance = input.intervalDistance,
                intervalMonths = input.intervalMonths,
                sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1,
                createdAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun updateItem(item: ServiceItem) {
        val current = items.getById(item.id) ?: error("Service item not found")
        items.update(
            current.copy(
                name = item.name.trim(),
                category = item.category,
                intervalDistance = item.intervalDistance,
                intervalMonths = item.intervalMonths,
                sortOrder = item.sortOrder,
            ),
        )
    }

    suspend fun deleteItem(itemId: Long) {
        val current = items.getById(itemId) ?: return
        items.delete(current)
    }
}
