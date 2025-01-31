package com.benavalli.kraken.table

import model.DeviceState
import org.jetbrains.exposed.sql.ReferenceOption

object DeviceTable : AuditTable("device") {
    val id = integer("id").autoIncrement()
    val pin = integer("pin")
    val typeId = integer("type_id").references(
        ref = DeviceTypeTable.id,
        onDelete = ReferenceOption.CASCADE
    )
    val state = bool("state").default(DeviceState.DISABLED.value)
    override val primaryKey = PrimaryKey(id)
}
