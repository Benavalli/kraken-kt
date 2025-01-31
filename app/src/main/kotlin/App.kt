package com.benavalli.kraken.app

import com.benavalli.kraken.app.controller.DeviceController
import com.benavalli.kraken.app.di.appModule
import com.benavalli.kraken.di.databaseModule
import com.benavalli.kraken.di.ioModule
import com.benavalli.kraken.sensor.DHT11Sensor
import com.benavalli.kraken.utils.Printer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication
import org.koin.java.KoinJavaComponent.getKoin

// This is the main entry point of the application.
// It uses the `Printer` class from the `:utils` subproject.
fun main() {
    koinApplication {
        startKoin {
            modules(
                appModule,
                databaseModule,
                ioModule
            )
        }
        val dht11: DHT11Sensor = getKoin().get()

        // Inicializa o banco de dados antes de iniciar a UI
        runBlocking {
            launch {
                val data = dht11.readData()
                println(data?.humidity.toString())
                println(data?.temperature.toString())
            }

            delay(2000)

        }
        val message = "Hello JetBrains!"
        val printer = Printer(message)
        printer.printMessage()
    }
}
