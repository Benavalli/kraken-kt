package com.benavalli.kraken.di

import com.benavalli.kraken.DatabaseManager
import com.benavalli.kraken.dao.DeviceDao
import com.benavalli.kraken.dao.DeviceTypeDao
import com.benavalli.kraken.mapper.DeviceMapper
import com.benavalli.kraken.mapper.DeviceTypeMapper
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val databaseModule = module {
    single { Dispatchers.IO }
    single { DeviceMapper() }
    single { DeviceTypeMapper() }
    single { DeviceTypeDao(get()) }
    single { DeviceDao(get()) }
    single { DatabaseManager() }
}
