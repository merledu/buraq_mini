package core

import chisel3._

class Fetch extends Module {
  val io = IO(new Bundle {
    // instruction meory interface(inputs)
    val instr_gnt_i = Input(Bool())
    val instr_rvalid_i = Input(Bool()) 
    val instr_rdata_i = Input(UInt(32.W))
    
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
 //   val stall = Input(UInt(1.W))

 // instruction memory interface(outputs)
    val instr_addr_o = Output(UInt(32.W))
    val instr_req_o = Output(Bool())

    val pc_out = Output(SInt(32.W))
    val pc4_out = Output(SInt(32.W))
    val inst_out = Output(UInt(32.W))
  })

  val pc = Module(new Pc())

    val pc_reg = RegInit(0.S(32.W))
    val pc4_reg = RegInit(0.S(32.W))
    val inst_reg = RegInit(0.U(32.W))

  pc.io.instr_rvalid_i := io.instr_rvalid_i
  io.instr_addr_o := pc.io.out(13,0).asUInt

  when(io.instr_gnt_i)
  {
//    io.instr_addr_o := pc.io.out(13,0).asUInt
    io.instr_req_o := true.B
  }
  .otherwise
  {
//    io.instr_addr_o := 0.U(32.W)
    io.instr_req_o := false.B
  }
  
  pc_reg := pc.io.out
  pc4_reg := pc.io.pc4


  when(io.hazardDetection_inst_forward === 1.U) {
    inst_reg := io.hazardDetection_inst_out
    pc_reg := io.hazardDetection_current_pc_out
  } .otherwise {
    inst_reg := io.instr_rdata_i
  }

  // Stopping the pc from updating since the pipeline has to be stalled. When the instruction is 0 and the next pc select
  // is also 0 we have a condition where the PC needs to be stopped for UART. Used next pc select because there is
  // a condition where the instruction is 0 but next pc select has some value for JALR instruction so the pc will not
  // get updated.
//  when(io.inst_in === 0.U && io.ctrl_next_pc_sel === 0.U) {
//    pc.io.in := pc.io.out
//    //pc.io.stall := 1.U
//  } .otherwise {
    when(io.hazardDetection_pc_forward === 1.U) {
      pc.io.in := io.hazardDetection_pc_out
    }.otherwise {
      when(io.ctrl_next_pc_sel === "b01".U) {
        when(io.branchLogic_output === 1.U && io.ctrl_out_branch === 1.U) {
          pc.io.in := io.sb_imm
          pc_reg := 0.S
          pc4_reg := 0.S
          inst_reg := 0.U
        }.otherwise {
          pc.io.in := pc.io.pc4
        }
      }.elsewhen(io.ctrl_next_pc_sel === "b10".U) {
        pc.io.in := io.uj_imm
        pc_reg := 0.S
        pc4_reg := 0.S
        inst_reg := 0.U
      }.elsewhen(io.ctrl_next_pc_sel === "b11".U) {
        pc.io.in := io.jalr_imm
        pc_reg := 0.S
        pc4_reg := 0.S
        inst_reg := 0.U
      }.otherwise {
        pc.io.in := pc.io.pc4
      }
    }
//  }


    io.pc_out := pc_reg
    io.pc4_out:= pc4_reg
    io.inst_out := inst_reg 

}
