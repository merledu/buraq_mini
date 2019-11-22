package datapath
import chisel3.iotesters.PeekPokeTester

class DataMemTests(c: DataMem) extends PeekPokeTester(c) {
    poke(c.io.memWrite, 1)
    poke(c.io.memRead, 0)
    poke(c.io.memAddress, 0)
    poke(c.io.memData, 5)
    step(1)
    poke(c.io.memWrite, 0)
    poke(c.io.memRead, 1)
    poke(c.io.memAddress, 0)
    step(1)
}