package com.benavalli.kraken

import com.benavalli.kraken.table.DeviceTable
import com.benavalli.kraken.table.DeviceTypeTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DatabaseManager : KoinComponent {

    private val dispatcherIO: CoroutineDispatcher by inject()
    private var database: Database? = null

    suspend fun initializeDatabase() = withContext(dispatcherIO) {
        try {
            database = connect()
            createTables()
        } catch (exception: Exception) {
            // Todo: log
        }
    }
    private fun connect() = Database.connect(
        url = "jdbc:sqlite:devices.db",
        driver = "org.sqlite.JDBC"
    )

    private suspend fun createTables() {
        newSuspendedTransaction {
            SchemaUtils.create(
                DeviceTypeTable,
                DeviceTable
            )
        }
    }

//    private suspend fun insertDefaultDeviceTypes() {
//        newSuspendedTransaction {
//            val existingTypes = DeviceTypeTable.selectAll().map { result ->
//                result[DeviceTypeTable.type]
//            }
//            DeviceType.entries.forEach { deviceType ->
//                if (deviceType.name !in existingTypes) {
//                    DeviceTypeTable.insert { body ->
//                        body[type] = deviceType.name
//                    }
//                }
//            }
//        }
//    }

    suspend fun close() = withContext(dispatcherIO) {
        try {
            database?.connector?.invoke()?.close()
        } catch (exception: Exception) {
            // Todo: log
        }
    }
}
