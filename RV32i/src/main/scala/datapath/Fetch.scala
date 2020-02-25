package datapath
import chisel3._

class Fetch extends Module {
  val io = IO(new Bundle {
    val inst_in = Input(UInt(32.W))
    val sb_imm = Input(SInt(32.W))
    val uj_imm = Input(SInt(32.W))
    val jalr_imm = Input(SInt(32.W))
    val ctrl_next_pc_sel = Input(UInt(2.W))
    val ctrl_out_branch = Input(UInt(1.W))
    val branchLogic_output = Input(UInt(1.W))
    val hazardDetection_pc_out = Input(SInt(32.W))
    val hazardDetection_inst_out = Input(UInt(32.W))
    val hazardDetection_current_pc_out = Input(SInt(32.W))
    val hazardDetection_pc_forward = Input(UInt(1.W))
    val hazardDetection_inst_forward = Input(UInt(1.W))
    val stall = Input(UInt(1.W))
    val wrAddr = Output(UInt(10.W))
    val pc_out = Output(SInt(32.W))
    val pc4_out = Output(SInt(32.W))
    val inst_out = Output(UInt(32.W))
  })

  val pc = Module(new Pc())
  pc.io.stall := io.stall
  //val imem = Module(new InstructionMem())

  //imem.io.wrAddr := pc.io.out(11,2).asUInt
  io.wrAddr := pc.io.out(11,2).asUInt
  io.pc_out := pc.io.out
  io.pc4_out := pc.io.pc4


  when(io.hazardDetection_inst_forward === 1.U) {
    io.inst_out := io.hazardDetection_inst_out
    io.pc_out := io.hazardDetection_current_pc_out
  } .otherwise {
    io.inst_out := io.inst_in
  }

  // Stopping the pc from updating since the pipeline has to be stalled. When the instruction is 0 and the next pc select
  // is also 0 we have a condition where the PC needs to be stopped for UART. Used next pc select because there is
  // a condition where the instruction is 0 but next pc select has some value for JALR instruction so the pc will not
  // get updated.
  when(io.inst_in === 0.U && io.ctrl_next_pc_sel === 0.U) {
    pc.io.in := pc.io.out
  } .otherwise {
    when(io.hazardDetection_pc_forward === 1.U) {
      pc.io.in := io.hazardDetection_pc_out
    }.otherwise {
      when(io.ctrl_next_pc_sel === "b01".U) {
        when(io.branchLogic_output === 1.U && io.ctrl_out_branch === 1.U) {
          pc.io.in := io.sb_imm
          io.pc_out := 0.S
          io.pc4_out := 0.S
          io.inst_out := 0.U
        }.otherwise {
          pc.io.in := pc.io.pc4
        }
      }.elsewhen(io.ctrl_next_pc_sel === "b10".U) {
        pc.io.in := io.uj_imm
        io.pc_out := 0.S
        io.pc4_out := 0.S
        io.inst_out := 0.U
      }.elsewhen(io.ctrl_next_pc_sel === "b11".U) {
        pc.io.in := io.jalr_imm
        io.pc_out := 0.S
        io.pc4_out := 0.S
        io.inst_out := 0.U
      }.otherwise {
        pc.io.in := pc.io.pc4
      }
    }
  }






}