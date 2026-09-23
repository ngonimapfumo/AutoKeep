package com.ngonim.autokeep.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ngonim.autokeep.data.local.entity.VehicleEntity
import com.ngonim.autokeep.data.local.relation.VehicleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY updated_at_epoch_ms DESC")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun observeById(id: Long): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: Long): VehicleEntity?

    @Transaction
    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun observeWithItems(id: Long): Flow<VehicleWithItems?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query(
        """
        UPDATE vehicles
        SET current_mileage = :mileage, updated_at_epoch_ms = :updatedAtEpochMs
        WHERE id = :id
        """,
    )
    suspend fun updateMileage(id: Long, mileage: Int, updatedAtEpochMs: Long)

    @Query(
        """
        UPDATE vehicles
        SET photo_uri = :photoUri, updated_at_epoch_ms = :updatedAtEpochMs
        WHERE id = :id
        """,
    )
    suspend fun updatePhoto(id: Long, photoUri: String?, updatedAtEpochMs: Long)

    @Delete
    suspend fun delete(vehicle: VehicleEntity)
}
