package datapath
import chisel3.iotesters.PeekPokeTester

class CoreTests(c: Core) extends PeekPokeTester(c) {

    step(1)
}
