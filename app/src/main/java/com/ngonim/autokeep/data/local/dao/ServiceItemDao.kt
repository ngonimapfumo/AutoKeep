package com.ngonim.autokeep.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ngonim.autokeep.data.local.entity.ServiceItemEntity
import com.ngonim.autokeep.data.local.relation.ServiceItemWithVisits
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceItemDao {
    @Query(
        """
        SELECT * FROM service_items
        WHERE vehicle_id = :vehicleId
        ORDER BY sort_order ASC, name ASC
        """,
    )
    fun observeForVehicle(vehicleId: Long): Flow<List<ServiceItemEntity>>

    @Query(
        """
        SELECT * FROM service_items
        WHERE vehicle_id = :vehicleId
        ORDER BY sort_order ASC, name ASC
        """,
    )
    suspend fun getForVehicle(vehicleId: Long): List<ServiceItemEntity>

    @Query("SELECT * FROM service_items WHERE id = :id")
    suspend fun getById(id: Long): ServiceItemEntity?

    @Query("SELECT * FROM service_items WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<ServiceItemEntity>

    @Transaction
    @Query("SELECT * FROM service_items WHERE id = :id")
    fun observeWithVisits(id: Long): Flow<ServiceItemWithVisits?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: ServiceItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<ServiceItemEntity>): List<Long>

    @Update
    suspend fun update(item: ServiceItemEntity)

    @Query(
        """
        UPDATE service_items
        SET last_done_mileage = :mileage, last_done_at_epoch_day = :performedAtEpochDay
        WHERE id = :id
        """,
    )
    suspend fun updateLastDone(id: Long, mileage: Int, performedAtEpochDay: Int)

    @Delete
    suspend fun delete(item: ServiceItemEntity)
}
