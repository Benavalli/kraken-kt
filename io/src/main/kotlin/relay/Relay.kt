package com.benavalli.kraken.relay

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalOutput

class Relay {

    private val pi4j: Context by lazy { Pi4J.newAutoContext() }
    private val relayInput: DigitalInput? = null
    private val relayOutput: DigitalOutput? = null




}