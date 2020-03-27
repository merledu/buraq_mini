package core

import chisel3._

class MemoryStage extends Module {
  val io = IO(new Bundle {
    val EX_MEM_alu_output = Input(SInt(32.W))
    val EX_MEM_rd_sel = Input(UInt(5.W))
    val EX_MEM_RegWr = Input(UInt(1.W))
    val EX_MEM_MemRd = Input(UInt(1.W))
    val EX_MEM_MemToReg = Input(UInt(1.W))
    val EX_MEM_MemWr = Input(UInt(1.W))
    val EX_MEM_rs2 = Input(SInt(32.W))

    val alu_output = Output(SInt(32.W))
    val rs2_out = Output(SInt(32.W))
    val memAddress = Output(UInt(32.W))
    val rd_sel_out = Output(UInt(5.W))
    val ctrl_RegWr_out = Output(UInt(1.W))
    val ctrl_MemRd_out = Output(UInt(1.W))
    val ctrl_MemToReg_out = Output(UInt(1.W))
    val ctrl_MemWr_out = Output(UInt(1.W))

  })


    io.alu_output := io.EX_MEM_alu_output
    io.memAddress := (io.EX_MEM_alu_output(21, 0)>>2).asUInt
    io.rs2_out := io.EX_MEM_rs2
    io.rd_sel_out := io.EX_MEM_rd_sel
    io.ctrl_RegWr_out := io.EX_MEM_RegWr
    io.ctrl_MemRd_out := io.EX_MEM_MemRd
    io.ctrl_MemToReg_out := io.EX_MEM_MemToReg
    io.ctrl_MemWr_out := io.EX_MEM_MemWr


}