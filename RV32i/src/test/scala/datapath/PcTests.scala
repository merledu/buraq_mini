package datapath
import chisel3.iotesters.PeekPokeTester

class PcTests(c: Pc) extends PeekPokeTester(c) {
    poke(c.io.in, 0)
    step(1)
    poke(c.io.in, 4)
    step(1)
    poke(c.io.in, 8)
    step(1)
    poke(c.io.in, 12)
    step(1)
}