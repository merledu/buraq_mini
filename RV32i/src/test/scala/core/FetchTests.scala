package core
import chisel3.iotesters.PeekPokeTester

class FetchTests(c: Fetch) extends PeekPokeTester(c) {
  poke(c.io.sb_imm, 0)
  poke(c.io.uj_imm, 0)
  poke(c.io.jalr_imm, 0)
  poke(c.io.ctrl_next_pc_sel, 0)
  poke(c.io.ctrl_out_branch, 0)
  poke(c.io.branchLogic_output, 0)
  poke(c.io.hazardDetection_pc_out, 0)
  poke(c.io.hazardDetection_inst_out, 0)
  poke(c.io.hazardDetection_current_pc_out, 0)
  poke(c.io.hazardDetection_pc_forward, 0)
  poke(c.io.hazardDetection_inst_forward, 0)
  poke(c.io.stall , 0)
  poke(c.io.inst_in, 0)

  step(10)

  poke(c.io.inst_in, 33)
  step(10)
}