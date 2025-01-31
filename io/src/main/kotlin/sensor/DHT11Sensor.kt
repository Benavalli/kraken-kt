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
        setupOutput()
    }

    private fun setupOutput() {
        // ✅ Check if GPIO 18 is already registered
        val existingOutput = context.registry().all().containsKey("dht11-gpio")
        if (existingOutput != null) {
           // println("Reusing existing GPIO instance: ${existingOutput}")
            output = existingOutput as DigitalOutput
            return
        }

        // ✅ If not registered, create new DigitalOutput
        val outputConfig = DigitalOutputConfigBuilder.newInstance(context)
            .id("dht11-gpio")
            .name("DHT11 Output")
            .address(pin)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.HIGH)
            .provider("pigpio-digital-output")
            .build()
        output = context.create(outputConfig)
    }

    private fun setupInput() {
        // ✅ Check if GPIO 18 is already registered
        val existingInput = context.registry().all().containsKey("dht11-gpio")
        if (existingInput != null) {
           // println("Reusing existing GPIO instance: ${existingInput.key()}")
            input = existingInput as DigitalInput
            return
        }

        // ✅ If not registered, create new DigitalInput
        val inputConfig = DigitalInputConfigBuilder.newInstance(context)
            .id("dht11-gpio")
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

        // ✅ Step 1: Set GPIO 18 as OUTPUT to trigger DHT11
        setupOutput()
        output?.low()
        delay(18)
        output?.high()

        // ✅ Step 2: Switch GPIO 18 to INPUT to read data
        setupInput()

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

        return@withContext if (checksum == ((humidity + temperature).toInt() and 0xFF)) {
            EnvironmentData(temperature, humidity)
        } else {
            println("Checksum error")
            null
        }
    }
}
