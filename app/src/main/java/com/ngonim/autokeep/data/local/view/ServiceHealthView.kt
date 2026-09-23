package com.ngonim.autokeep.data.local.view

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

/**
 * Dashboard row: remaining distance for one service item.
 *
 * `remaining_distance` is null when last-done mileage was never set.
 * Negative remaining means overdue. Health color (green/amber/red) stays in the app layer.
 */
@DatabaseView(
    viewName = "service_health",
    value = """
        SELECT
            i.id AS service_item_id,
            i.vehicle_id AS vehicle_id,
            i.name AS name,
            i.interval_distance AS interval_distance,
            i.last_done_mileage AS last_done_mileage,
            i.last_done_at_epoch_day AS last_done_at_epoch_day,
            i.sort_order AS sort_order,
            v.current_mileage AS current_mileage,
            CASE
                WHEN i.last_done_mileage IS NULL THEN NULL
                ELSE (i.last_done_mileage + i.interval_distance - v.current_mileage)
            END AS remaining_distance
        FROM service_items AS i
        INNER JOIN vehicles AS v ON v.id = i.vehicle_id
        """,
)
data class ServiceHealthView(
    @ColumnInfo(name = "service_item_id")
    val serviceItemId: Long,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    val name: String,
    @ColumnInfo(name = "interval_distance")
    val intervalDistance: Int,
    @ColumnInfo(name = "last_done_mileage")
    val lastDoneMileage: Int?,
    @ColumnInfo(name = "last_done_at_epoch_day")
    val lastDoneAtEpochDay: Int?,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "current_mileage")
    val currentMileage: Int,
    @ColumnInfo(name = "remaining_distance")
    val remainingDistance: Int?,
)
