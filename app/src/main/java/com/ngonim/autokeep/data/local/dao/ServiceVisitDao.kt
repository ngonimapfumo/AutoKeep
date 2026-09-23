package com.ngonim.autokeep.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ngonim.autokeep.data.local.entity.ServiceVisitEntity
import com.ngonim.autokeep.data.local.entity.ServiceVisitItemEntity
import com.ngonim.autokeep.data.local.relation.ServiceVisitWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceVisitDao {
    @Transaction
    @Query(
        """
        SELECT * FROM service_visits
        WHERE vehicle_id = :vehicleId
        ORDER BY performed_at_epoch_day DESC, created_at_epoch_ms DESC
        """,
    )
    fun observeForVehicle(vehicleId: Long): Flow<List<ServiceVisitWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM service_visits
        WHERE vehicle_id = :vehicleId
          AND performed_at_epoch_day >= :fromEpochDay
          AND performed_at_epoch_day <= :toEpochDay
        ORDER BY performed_at_epoch_day DESC, created_at_epoch_ms DESC
        """,
    )
    fun observeInRange(
        vehicleId: Long,
        fromEpochDay: Int,
        toEpochDay: Int,
    ): Flow<List<ServiceVisitWithItems>>

    @Query("SELECT * FROM service_visits WHERE id = :id")
    suspend fun getById(id: Long): ServiceVisitEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(visit: ServiceVisitEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<ServiceVisitItemEntity>)

    @Update
    suspend fun update(visit: ServiceVisitEntity)

    @Delete
    suspend fun delete(visit: ServiceVisitEntity)

    @Query(
        """
        SELECT COALESCE(SUM(cost_minor), 0) FROM service_visits
        WHERE vehicle_id = :vehicleId
          AND cost_minor IS NOT NULL
        """,
    )
    fun observeTotalCostMinor(vehicleId: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(cost_minor), 0) FROM service_visits
        WHERE vehicle_id = :vehicleId
          AND cost_minor IS NOT NULL
          AND performed_at_epoch_day >= :fromEpochDay
          AND performed_at_epoch_day <= :toEpochDay
        """,
    )
    fun observeCostMinorInRange(
        vehicleId: Long,
        fromEpochDay: Int,
        toEpochDay: Int,
    ): Flow<Long>
}
