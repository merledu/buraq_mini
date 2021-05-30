package buraq_mini.core
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}
import chisel3.iotesters.PeekPokeTester

class MemoryStageTests[A <: AbstrRequest, B <: AbstrResponse, C <: BusConfig](c: MemoryStage[A,B,C]) extends PeekPokeTester(c) {
  poke(c.io.EX_MEM_alu_output, 1)
  poke(c.io.EX_MEM_rd_sel, 1)
  poke(c.io.EX_MEM_RegWr, 1)
//  poke(c.io.EX_MEM_MemRd, 1)
  poke(c.io.EX_MEM_MemToReg, 1)
  poke(c.io.EX_MEM_MemWr, 1)
  poke(c.io.EX_MEM_rs2, 1)
}