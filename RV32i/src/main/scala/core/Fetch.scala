package core

import chisel3._
import chisel3.util._

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
    val stall = Input(Bool())

 // instruction memory interface(outputs)
    val instr_addr_o = Output(UInt(32.W))
    val instr_req_o = Output(Bool())

    val pc_out = Output(SInt(32.W))
    val pc4_out = Output(SInt(32.W))
    val inst_out = Output(UInt(32.W))
  })
  val NOP = "b00000000000000000000000000010011".U(32.W) // NOP instruction

  val pc = Module(new Pc())

  // IF/ID registers
  val pc_reg = Reg(SInt())
  val pc4_reg = Reg(SInt())
  val inst_reg = RegInit(NOP)

  // This holds the reset value until next clock cycle
  //val started = RegNext(reset.asBool())

  // Send the next pc value to the instruction memory
  io.instr_addr_o := pc.io.in(13,0).asUInt
  // if device is ready to accept the request then send a valid signal to fetch from.
  io.instr_req_o := Mux(io.instr_gnt_i, true.B, false.B)
  // wait for valid signal to arrive indicating the fetched instruction is valid otherwise send NOP
  val instr = Mux(io.instr_rvalid_i, io.instr_rdata_i, NOP)

  when(!io.stall) {
    // send the current pc value to the Decode stage
    pc_reg := pc.io.out
    // send the pc + 4 to the Decode stage
    pc4_reg := pc.io.pc4
  }

  when(!io.stall) {
    when(io.hazardDetection_inst_forward === 1.U) {
      inst_reg := io.hazardDetection_inst_out
      pc_reg := io.hazardDetection_current_pc_out
    } .otherwise {
      // instead of sending the instruction data directly to the decode first see if
      // the reset has been low for one cycle with the `started` val. If `started` is
      // high it means the reset has not been low for one clock cycle so still send
      // NOP instruction to the Decode otherwise send the received data from the ICCM.
      //    inst_reg := Mux(started, NOP, io.instr_rdata_i)

      inst_reg := instr

    }
  }


  // Stopping the pc from updating since the pipeline has to be stalled. When the instruction is 0 and the next pc select
  // is also 0 we have a condition where the PC needs to be stopped for UART. Used next pc select because there is
  // a condition where the instruction is 0 but next pc select has some value for JALR instruction so the pc will not
  // get updated.
//  when(io.inst_in === 0.U && io.ctrl_next_pc_sel === 0.U) {
//    pc.io.in := pc.io.out
//    //pc.io.stall := 1.U
//  } .otherwise {
  when(!io.stall) {
    when(io.hazardDetection_pc_forward === 1.U) {
      pc.io.in := io.hazardDetection_pc_out
    }.otherwise
    {
      when(io.ctrl_next_pc_sel === "b01".U) {
        when(io.branchLogic_output === 1.U && io.ctrl_out_branch === 1.U) {
          pc.io.in := io.sb_imm
          pc_reg := 0.S
          pc4_reg := 0.S
          inst_reg := NOP
        }.otherwise {
          pc.io.in := pc.io.pc4
        }
      }.elsewhen(io.ctrl_next_pc_sel === "b10".U) {
        pc.io.in := io.uj_imm
        pc_reg := 0.S
        pc4_reg := 0.S
        inst_reg := NOP
      }.elsewhen(io.ctrl_next_pc_sel === "b11".U) {
        pc.io.in := io.jalr_imm
        pc_reg := 0.S
        pc4_reg := 0.S
        inst_reg := NOP
      }.otherwise {
        pc.io.in := pc.io.pc4
      }
    }
  } .otherwise {
    pc.io.in := pc.io.out
  }



  io.pc_out := pc_reg
  io.pc4_out:=  pc4_reg
  io.inst_out := inst_reg
}
