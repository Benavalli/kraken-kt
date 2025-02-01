package com.benavalli.kraken.relay

import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.ktx.console
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.pi4j
import com.pi4j.ktx.pi4jAsync
import model.Device
import model.DeviceState

class Relay {

    fun changeRelayState(device: Device) {
        pi4jAsync {
            console {
                digitalOutput(device.pin) {
                    id(device.id.toString())
                    name(device.type.name)
                    address(device.pin)
                    shutdown(if (device.state == DeviceState.ENABLED) DigitalState.LOW else DigitalState.HIGH)
                    initial(if (device.state == DeviceState.ENABLED) DigitalState.HIGH else DigitalState.LOW)
                }.apply {
                    if (device.state == DeviceState.ENABLED) {
                        low()
                        // Todo log
                    } else {
                        high()
                        // Todo log
                    }
                }
            }

        }
    }
}