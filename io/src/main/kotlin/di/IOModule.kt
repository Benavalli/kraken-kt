package com.benavalli.kraken.di

import com.benavalli.kraken.sensor.DHT11Sensor
import com.pi4j.Pi4J
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module


val ioModule = module {
    single { Dispatchers.IO }
    single { Pi4J.newAutoContext() }
    single { DHT11Sensor(get(), 18)}
}