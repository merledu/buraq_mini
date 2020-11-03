package core

import chisel3._
import chisel3.util._
import main.scala.core.csrs.{Exc_Cause}

class Fetch extends Module {
  val io = IO(new Bundle {
    // instruction memory interface(inputs)
    val instr_gnt_i = Input(Bool())
    val instr_rvalid_i = Input(Bool()) 
    val instr_rdata_i = Input(UInt(32.W))
    // csr register file inputs/outputs
    val irq_pending_i = Input(Bool())   // true when global interrupts are enabled
    val csr_mstatus_mie = Input(Bool())
    val csr_mtvec_i = Input(UInt(32.W))
    val csr_mtvec_init_o = Output(Bool())
    val csr_save_cause_o = Output(Bool())
    val csr_save_if_o = Output(Bool())
    val csr_if_pc_o = Output(UInt(32.W))
    val exc_cause_o = Output(UInt(6.W))
    val csr_mepc_i = Input(UInt(32.W))

    // sending a "done with bootup" signal so that core can initialize the mtvec register
    val bootup_done = Input(Bool())

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

    // mret instruction decoded in decoded stage
    val mret_inst_i = Input(Bool())

    val stall = Input(Bool())

 // instruction memory interface(outputs)
    val instr_addr_o = Output(UInt(32.W))
    val instr_req_o = Output(Bool())

    val if_id_pc_out = Output(SInt(32.W))
    val if_id_pc4_out = Output(SInt(32.W))
    val if_id_inst_out = Output(UInt(32.W))
  })
  val NOP = "b00000000000000000000000000010011".U(32.W) // NOP instruction

  val pc = Module(new Pc())

  // by default setting the save pc in fetch to false.
  io.csr_save_if_o := false.B
  io.csr_if_pc_o := 0.U
  io.csr_save_cause_o := false.B

  // IF/ID registers
  val if_id_pc_reg = Reg(SInt())
  val if_id_pc4_reg = Reg(SInt())
  val if_id_inst_reg = RegInit(NOP)

  // checking if we have an interrupt to deal with
  val handle_irq = io.irq_pending_i && io.csr_mstatus_mie

  // if irq then stopping the fetch for getting new instruction because we need to jump to the trap handler
  val halt_if = Wire(Bool())
  halt_if := Mux(handle_irq, true.B, false.B)

  // initializing the mtvec register when done with booting
  io.csr_mtvec_init_o := Mux(io.bootup_done, true.B, false.B)

  // csr register file default outputs
  io.csr_save_if_o := false.B
  io.csr_save_cause_o := false.B
  io.exc_cause_o := Exc_Cause.EXC_CAUSE_INSN_ADDR_MISA
  // Send the next pc value to the instruction memory
  io.instr_addr_o := pc.io.in(13,0).asUInt
  // if device is ready to accept the request then send a valid signal to fetch from.
  io.instr_req_o := Mux(io.instr_gnt_i, true.B, false.B)
  // wait for valid signal to arrive indicating the fetched instruction is valid otherwise send NOP
  val instr = Mux(io.instr_rvalid_i, io.instr_rdata_i, NOP)

  when(!io.stall && !halt_if) {
    // send the current pc value to the Decode stage
    if_id_pc_reg := pc.io.out
    // send the pc + 4 to the Decode stage
    if_id_pc4_reg := pc.io.pc4
  }

  when(!io.stall && !halt_if) {
    when(io.hazardDetection_inst_forward === 1.U) {
      if_id_inst_reg := io.hazardDetection_inst_out
      if_id_pc_reg := io.hazardDetection_current_pc_out
    } .otherwise {
      // instead of sending the instruction data directly to the decode first see if
      // the reset has been low for one cycle with the `started` val. If `started` is
      // high it means the reset has not been low for one clock cycle so still send
      // NOP instruction to the Decode otherwise send the received data from the ICCM.
      //    inst_reg := Mux(started, NOP, io.instr_rdata_i)

      if_id_inst_reg := instr

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
  when(!io.stall && !halt_if) {
    when(io.hazardDetection_pc_forward === 1.U) {
      pc.io.in := io.hazardDetection_pc_out
    }.otherwise
    {
      when(io.ctrl_next_pc_sel === "b01".U) {
        when(io.branchLogic_output === 1.U && io.ctrl_out_branch === 1.U) {
          pc.io.in := io.sb_imm
          if_id_pc_reg := 0.S
          if_id_pc4_reg := 0.S
          if_id_inst_reg := NOP
        }.otherwise {
          pc.io.in := pc.io.pc4
        }
      }.elsewhen(io.ctrl_next_pc_sel === "b10".U) {
        pc.io.in := io.uj_imm
        if_id_pc_reg := 0.S
        if_id_pc4_reg := 0.S
        if_id_inst_reg := NOP
      }.elsewhen(io.ctrl_next_pc_sel === "b11".U) {
        pc.io.in := io.jalr_imm
        if_id_pc_reg := 0.S
        if_id_pc4_reg := 0.S
        if_id_inst_reg := NOP
      } .elsewhen(io.mret_inst_i) {
        pc.io.in := io.csr_mepc_i.asSInt()
        if_id_pc_reg := 0.S
        if_id_pc4_reg := 0.S
        if_id_inst_reg := NOP
      }.otherwise {
        pc.io.in := pc.io.pc4
      }
    }
  } .elsewhen(!io.stall && halt_if) {
    pc.io.in := Cat(io.csr_mtvec_i(31,8), 0.U(1.W), Exc_Cause.EXC_CAUSE_IRQ_EXTERNAL_M(4,0), 0.U(2.W)).asSInt()
    if_id_inst_reg := NOP // when halted pass NOP since we dont want to repeatedly send the current instruction as it will be executed twice
    io.csr_save_if_o := true.B
    // Checking which pc to set in mepc. If the pc in had a branch instruction when interrupt came
    // then save the calculated branch addres in mepc to return back to correct instruction after mret
    // otherwise save pc's current value in mepc.
    io.csr_if_pc_o := Mux(io.ctrl_next_pc_sel === "b01".U, io.sb_imm.asUInt(),
                      Mux(io.ctrl_next_pc_sel === "b10".U, io.uj_imm.asUInt(),
                      Mux(io.ctrl_next_pc_sel === "b11".U, io.jalr_imm.asUInt(), pc.io.out.asUInt())))
    io.csr_save_cause_o := true.B
    io.exc_cause_o := Exc_Cause.EXC_CAUSE_IRQ_EXTERNAL_M
  }
    .otherwise {
    pc.io.in := pc.io.out
  }



  io.if_id_pc_out := if_id_pc_reg
  io.if_id_pc4_out:=  if_id_pc4_reg
  io.if_id_inst_out := if_id_inst_reg
}
