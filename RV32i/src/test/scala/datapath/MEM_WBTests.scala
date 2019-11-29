package datapath
import chisel3.iotesters.PeekPokeTester

class MEM_WBTests(c: MEM_WB) extends PeekPokeTester(c) {
    poke(c.io.ctrl_RegWr_in, 1)
    poke(c.io.ctrl_MemToReg_in, 1)
    poke(c.io.rd_sel_in, 10)
    poke(c.io.dmem_data_in, 100)
    poke(c.io.alu_in, 500)
    step(1)
}