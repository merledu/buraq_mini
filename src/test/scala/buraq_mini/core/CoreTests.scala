package buraq_mini.core

import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}
import chisel3.iotesters.PeekPokeTester

class CoreTests[A <: AbstrRequest, B <: AbstrResponse, C <: BusConfig](c: Core[A,B,C]) extends PeekPokeTester(c) {

    step(1)
    
}