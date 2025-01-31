package com.benavalli.kraken.app.di

import com.benavalli.kraken.app.controller.DeviceController
import org.koin.dsl.module

val appModule = module {
    single { DeviceController(get(), get(), get()) }
}