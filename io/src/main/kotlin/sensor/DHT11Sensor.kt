package com.benavalli.kraken.sensor

import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.*
import kotlinx.coroutines.*
import model.EnvironmentData
import org.koin.core.component.KoinComponent


class DHT11Sensor(private val context: Context, private val pin: Int) : KoinComponent {

    private var input: DigitalInput? = null
    private var output: DigitalOutput? = null

    init {
        // Configure Digital Output (for signaling DHT11)
        val outputConfig = DigitalOutputConfigBuilder.newInstance(context)
            .id("dht11-output")
            .name("DHT11 Output")
            .address(pin)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.HIGH)
            .provider("pigpio-digital-output")
            .build()
        output = context.create(outputConfig)

        // Configure Digital Input (to read data from DHT11)
        val inputConfig = DigitalInputConfigBuilder.newInstance(context)
            .id("dht11-input")
            .name("DHT11 Input")
            .address(pin)
            .pull(PullResistance.OFF)
            .provider("pigpio-digital-input")
            .build()
        input = context.create(inputConfig)
    }




    suspend fun readData(): EnvironmentData? = withContext(Dispatchers.IO) {
        val rawData = mutableListOf<Long>()
        val dataReady = CompletableDeferred<Boolean>()

        output?.low()
        delay(18)
        output?.high()

        val listener = DigitalStateChangeListener {
            rawData.add(System.nanoTime())
            if (rawData.size >= 84) dataReady.complete(true) // 84 transitions = 40 bits + start sequence
        }

        input?.addListener(listener)

        val success = withTimeoutOrNull(100) { dataReady.await() } ?: false

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

        // Validate checksum
        return@withContext if (checksum == ((humidity + temperature).toInt() and 0xFF)) {
            EnvironmentData(temperature, humidity)
        } else {
            println("Checksum error")
            null
        }
    }
}