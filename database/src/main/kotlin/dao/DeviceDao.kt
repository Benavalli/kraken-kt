package com.benavalli.kraken.dao

import com.benavalli.kraken.mapper.DeviceMapper
import com.benavalli.kraken.table.DeviceTable
import com.benavalli.kraken.table.DeviceTypeTable
import model.Device
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll

class DeviceDao(
    private val deviceMapper: DeviceMapper
) : BaseDao<Device>(DeviceTable, DeviceTable.id) {

    suspend fun getAllDevices(): List<Device> = getAll(
        rowMapper = deviceMapper::fromResultRowToModel
    )

    override suspend fun getAll(rowMapper: (ResultRow) -> Device): List<Device> {
        return dbQuery {
            (DeviceTable innerJoin DeviceTypeTable)
                .selectAll()
                .map { rowMapper(it) }
        }
    }

    suspend fun getDeviceById(id: Int) = getById(
        id = id,
        rowMapper = deviceMapper::fromResultRowToModel
    )

    override suspend fun getById(id: Int, rowMapper: (ResultRow) -> Device?): Device? {
        return dbQuery {
            (DeviceTable innerJoin DeviceTypeTable)
                .selectAll()
                .where(predicate = DeviceTable.id eq id)
                .map { rowMapper(it) }
                .singleOrNull()
        }
    }

    suspend fun insertDevice(device: Device, typeId: Int) =
        insert(device) { deviceMapper.fromModelToInsertStatement(device, typeId) }

    suspend fun updateDeviceStatus(device: Device) =
        update(device.id) { deviceMapper.fromModelToUpdateStatement(device) }
}
