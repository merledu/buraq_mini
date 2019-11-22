package datapath
import chisel3.iotesters.PeekPokeTester

class EX_MEMTests(c: EX_MEM) extends PeekPokeTester(c) {
    poke(c.io.ID_EX_MEMWR, 1)
    poke(c.io.ID_EX_MEMRD, 0)
    poke(c.io.ID_EX_REGWR, 0)
    poke(c.io.ID_EX_MEMTOREG, 1)
    poke(c.io.ID_EX_RS2, 0)
    poke(c.io.ID_EX_RDSEL, 30)
    poke(c.io.alu_output, 23)
    step(1)
}