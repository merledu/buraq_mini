package datapath

import chisel3.iotesters.PeekPokeTester

class IF_IDTests(c: IF_ID) extends PeekPokeTester(c) {
    poke(c.io.pc_in, 4)
    poke(c.io.inst_in, 33)
   // poke(c.io.inst_in, 53)
    poke(c.io.stall, 0)
    step(1)
    poke(c.io.pc_in, 8)
    poke(c.io.inst_in, 44)
    poke(c.io.stall, 1)
    step(1)
    poke(c.io.stall, 0)
    step(2)
}