package com.benavalli.kraken.di

import com.benavalli.kraken.relay.Relay
import com.benavalli.kraken.sensor.DHT11Sensor
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module


val ioModule = module {
    single { Dispatchers.IO }
    single { Relay() }
    single { DHT11Sensor(get(), 18)}
}