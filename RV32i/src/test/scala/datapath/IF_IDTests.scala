package datapath

import chisel3.iotesters.PeekPokeTester

class IF_IDTests(c: IF_ID) extends PeekPokeTester(c) {
    poke(c.io.pc_in, 4)
    poke(c.io.inst_in, 53)
    step(1)
}