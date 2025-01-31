package com.benavalli.kraken.table

object DeviceTypeTable : AuditTable("device_type") {
    val id = integer("id").autoIncrement()
    val type = varchar("type", 50)
    override val primaryKey = PrimaryKey(id)
}
