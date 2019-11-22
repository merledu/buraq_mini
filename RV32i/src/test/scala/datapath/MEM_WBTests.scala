package datapath
import chisel3.iotesters.PeekPokeTester

class MEM_WBTests(c: MEM_WB) extends PeekPokeTester(c) {
    poke(c.io.EX_MEM_REGWR, 1)
    poke(c.io.EX_MEM_MEMTOREG, 1)
    poke(c.io.EX_MEM_RDSEL, 10)
    poke(c.io.in_dataMem_data, 100)
    poke(c.io.in_alu_output, 500)
    step(1)
}