package main.scala.core
import chisel3._

class CsrHazardUnit extends Module {
  val io = IO(new Bundle {
    val ID_EX_csrWen = Input(Bool())
    val ID_EX_csrAddr = Input(UInt(12.W))
    val ID_EX_rd_sel = Input(UInt(5.W))
    val rs1_sel_in_decode = Input(UInt(5.W))
    val csr_wen_in_decode = Input(Bool())
    val csr_addr_in_decode = Input(UInt(12.W))
    val forward_csr = Output(UInt(2.W))
    val forward_rs1 = Output(Bool())
  })
  io.forward_csr := "b00".U
  io.forward_rs1 := false.B
  // hazard in decode/execute stage
  // rs1 hazard
  when(io.ID_EX_rd_sel === io.rs1_sel_in_decode && io.csr_wen_in_decode) {
    io.forward_rs1 := true.B
  }
  // csr register hazard
  when(io.ID_EX_csrWen && io.csr_wen_in_decode && io.ID_EX_csrAddr === io.csr_addr_in_decode) {
    io.forward_csr := "b10".U
  }



}
