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
        // ✅ Try to get the existing GPIO instance as a DigitalOutput
        val existingOutput: DigitalOutput? = context.registry().get<DigitalOutput>("dht11-gpio")

        if (existingOutput != null) {
            println("Reusing existing GPIO output: ${existingOutput.id()}")
            output = existingOutput
            return
        }

        // ✅ If no existing output, configure new Digital Output
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
        // ✅ Try to get the existing GPIO instance as a DigitalInput
        val existingInput: DigitalInput? = context.registry().get<DigitalInput>("dht11-gpio")

        if (existingInput != null) {
            println("Reusing existing GPIO input: ${existingInput.id()}")
            input = existingInput
            return
        }

        // ✅ If no existing input, configure new Digital Input
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
