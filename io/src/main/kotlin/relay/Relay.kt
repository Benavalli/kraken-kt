package com.benavalli.kraken.relay

import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.ktx.console
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.io.digital.piGpioProvider
import com.pi4j.ktx.pi4j
import com.pi4j.ktx.pi4jAsync
import kotlinx.coroutines.delay
import model.Device
import model.DeviceState

class Relay {


    fun changeRelayState(device: Device) {
        pi4jAsync {
                digitalOutput(device.pin) {
                    id(device.id.toString())
                    name(device.type.name)
                    address(device.pin)
                    shutdown(DigitalState.LOW)
                    initial(DigitalState.LOW)
                    provider("raspberrypi-digital-output")
                }.apply {
                    println("entrei")
                    high()
                    delay(2000)
                    low()
                    high()
                    delay(2000)
                    low()
                    delay(2000)
                    high()
                    delay(2000)
                    low()
                    delay(2000)
                    high()
                    delay(2000)
                }
            }
    }
}
