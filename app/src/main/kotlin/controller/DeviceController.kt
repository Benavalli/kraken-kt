package com.benavalli.kraken.app.controller

import com.benavalli.kraken.DatabaseManager
import com.benavalli.kraken.dao.DeviceDao
import com.benavalli.kraken.dao.DeviceTypeDao
import org.koin.core.component.KoinComponent

class DeviceController(
    private val databaseManager: DatabaseManager,
    private val deviceDao: DeviceDao,
    private val deviceTypeDao: DeviceTypeDao
) : KoinComponent {

    suspend fun initializeDatabase() {
        databaseManager.initializeDatabase()
    }

//    fun getDevices(): List<Device> {
//        return deviceDao.getAllDevices()
//    }


}