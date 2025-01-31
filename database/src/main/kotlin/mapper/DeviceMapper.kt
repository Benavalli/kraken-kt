package com.benavalli.kraken.mapper

import com.benavalli.kraken.table.DeviceTable
import com.benavalli.kraken.table.DeviceTypeTable
import model.Device
import model.DeviceState
import model.DeviceType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

class DeviceMapper {

    fun fromResultRowToModel(row: ResultRow) = Device(
        id = row[DeviceTable.id],
        pin = row[DeviceTable.pin],
        type = DeviceType.entries.find { it.name == row[DeviceTypeTable.type] } ?: DeviceType.UNKNOWN,
        state = if (row[DeviceTable.state]) DeviceState.ENABLED else DeviceState.DISABLED
    )

    fun fromModelToInsertStatement(model: Device, typeId: Int): (InsertStatement<Number>) -> Unit {
        return { statement ->
            statement[DeviceTable.pin] = model.pin
            statement[DeviceTable.typeId] = typeId
            statement[DeviceTable.state] = model.state.value
        }
    }

    fun fromModelToUpdateStatement(model: Device): (UpdateStatement) -> Unit {
        return { updateStatement ->
            updateStatement[DeviceTable.pin] = model.pin
            updateStatement[DeviceTable.state] = model.state.value
        }
    }
}
