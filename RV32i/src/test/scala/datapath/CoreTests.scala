package datapath
import chisel3.iotesters.PeekPokeTester

class CoreTests(c: Core) extends PeekPokeTester(c) {
    poke(c.io.dmem_data, 20)
    poke(c.io.imem_data, 20)
    step(1)
}
