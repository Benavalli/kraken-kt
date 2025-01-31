package com.benavalli.kraken.dao

import com.benavalli.kraken.mapper.DeviceTypeMapper
import com.benavalli.kraken.table.DeviceTypeTable
import model.DeviceType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll

class DeviceTypeDao(
    private val deviceTypeMapper: DeviceTypeMapper
) : BaseDao<DeviceType>(DeviceTypeTable, DeviceTypeTable.id) {

    suspend fun getDeviceTypeId(deviceType: DeviceType): Int? {
        return dbQuery {
            DeviceTypeTable
                .selectAll()
                .where(predicate = DeviceTypeTable.type eq deviceType.name)
                .map { deviceTypeMapper.fromResultRowToInt(it) }
                .singleOrNull()
        }
    }

    suspend fun insertDevice(deviceType: DeviceType) =
        insert(deviceType) { deviceTypeMapper.fromModelToUpdateStatement(deviceType) }
}
