package com.benavalli.kraken.sensor

import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.*
import kotlinx.coroutines.*
import model.EnvironmentData
import org.koin.core.component.KoinComponent


class DHT11Sensor(private val context: Context, private val pin: Int) : KoinComponent {

    private var input: DigitalInput? = null
    private var output: DigitalOutput? = null


    private fun setupOutput() {
        try {
            // ✅ Check if GPIO 18 is already registered
            if (context.registry().exists("dht11-gpio")) {
                val existingInstance = context.registry().all()["dht11-gpio"]

                if (existingInstance is DigitalOutput) {
                    println("Reusing existing DigitalOutput: ${existingInstance.id()}")
                    output = existingInstance
                    return
                }

                if (existingInstance is DigitalInput) {
                    println("Releasing existing DigitalInput: ${existingInstance.id()} before setting OUTPUT mode")
                    context.registry().remove<DigitalInput>("dht11-gpio")
                }
            }

            println("Creating new DigitalOutput for GPIO 18")

            // ✅ Now configure as Digital Output
            val outputConfig = DigitalOutputConfigBuilder.newInstance(context)
                .id("dht11-gpio")
                .name("DHT11 Output")
                .address(pin)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.HIGH)
                .build()

            output = context.create(outputConfig)

        } catch (e: Exception) {
            println("Error setting up DigitalOutput: ${e.message}")
        }
    }

    private fun setupInput() {
        try {
            // ✅ Check if GPIO 18 is already registered
            if (context.registry().exists("dht11-gpio")) {
                val existingInstance = context.registry().all()["dht11-gpio"]

                if (existingInstance is DigitalInput) {
                    println("Reusing existing DigitalInput: ${existingInstance.id()}")
                    input = existingInstance
                    return
                }

                if (existingInstance is DigitalOutput) {
                    println("Releasing existing DigitalOutput: ${existingInstance.id()} before setting INPUT mode")
                    context.registry().remove<DigitalOutput>("dht11-gpio")
                }
            }

            println("Creating new DigitalInput for GPIO 18")

            // ✅ Now configure as Digital Input
            val inputConfig = DigitalInputConfigBuilder.newInstance(context)
                .id("dht11-gpio")
                .name("DHT11 Input")
                .address(pin)
                .pull(PullResistance.OFF)
                .build()

            input = context.create(inputConfig)

        } catch (e: Exception) {
            println("Error setting up DigitalInput: ${e.message}")
        }
    }

    suspend fun readData(): EnvironmentData? = withContext(Dispatchers.IO) {
        val rawData = mutableListOf<Long>()
        val dataReady = CompletableDeferred<Boolean>()

        // ✅ Step 1: Set GPIO 18 as OUTPUT to trigger DHT11
        setupOutput()
        output?.low()
        delay(18)
        output?.high()

        delay(40)
        // ✅ Step 2: Switch GPIO 18 to INPUT to read data
        setupInput()

        val listener = DigitalStateChangeListener {
            rawData.add(System.nanoTime())
            if (rawData.size >= 84) dataReady.complete(true) // 84 transitions = 40 bits + start sequence
        }

        input?.addListener(listener)

        val success = withTimeoutOrNull(2000) { dataReady.await() } ?: false
        input?.removeListener(listener)

        if (!success) {
            println("Error: No response from DHT11")
            return@withContext null
        }

        // Process raw timings into binary data
        val pulses = rawData.windowed(2, 2) { it[1] - it[0] }
        val bits = pulses.drop(3).map { if (it > 50000) 1 else 0 }

        if (bits.size < 40) {
            println("Error: Incomplete data received")
            return@withContext null
        }

        // Decode bytes
        val humidity = (bits.slice(0..7).joinToString("").toInt(2)).toFloat()
        val temperature = (bits.slice(16..23).joinToString("").toInt(2)).toFloat()
        val checksum = bits.slice(32..39).joinToString("").toInt(2)

        return@withContext if (checksum == ((humidity + temperature).toInt() and 0xFF)) {
            EnvironmentData(temperature, humidity)
        } else {
            println("Checksum error")
            null
        }
    }
}
