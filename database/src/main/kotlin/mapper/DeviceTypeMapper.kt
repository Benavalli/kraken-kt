package com.benavalli.kraken.mapper

import com.benavalli.kraken.table.DeviceTypeTable
import model.DeviceType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateStatement

class DeviceTypeMapper {

    fun fromResultRowToInt(row: ResultRow) = row[DeviceTypeTable.id]

    fun fromModelToUpdateStatement(model: DeviceType): (UpdateStatement) -> Unit {
        return { statement ->
            statement[DeviceTypeTable.type] = model.name
        }
    }
}
