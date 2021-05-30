package buraq_mini.core

import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig}
import chisel3._
import chisel3.util._
import main.scala.core.csrs.Exc_Cause

class Fetch[A <: AbstrRequest, B <: AbstrResponse, C <: BusConfig]
           (gen: A, gen1: B)
           (implicit val config: C) extends Module {
  val io = IO(new Bundle {
    // ------------------------------------ //
    // instruction memory interface(inputs) //
    // ------------------------------------ //
    val coreInstrReq = Decoupled(gen)
    val coreInstrRsp = Flipped(Decoupled(gen1))
    // ------------------------------------ //
    // csr register file(inputs/outputs)    //
    // ------------------------------------ //

    val csrRegFile_irq_pending_i                     =       Input(Bool())
    val csrRegFile_csr_mstatus_mie_i                 =       Input(Bool())
    val csrRegFile_csr_mtvec_i                       =       Input(UInt(32.W))
    val csrRegFile_csr_mtvec_init_o                  =       Output(Bool())
    val csrRegFile_csr_save_cause_o                  =       Output(Bool())
    val csrRegFile_csr_save_if_o                     =       Output(Bool())
    val csrRegFile_csr_if_pc_o                       =       Output(UInt(32.W))
    val csrRegFile_exc_cause_o                       =       Output(UInt(6.W))
    val csrRegFile_csr_mepc_i                        =       Input(UInt(32.W))

    val core_init_mtvec_i                            =       Input(Bool())

    val decode_sb_imm_i                              =       Input(SInt(32.W))
    val decode_uj_imm_i                              =       Input(SInt(32.W))
    val decode_jalr_imm_i                            =       Input(SInt(32.W))
    val decode_ctrl_next_pc_sel_i                    =       Input(UInt(2.W))
    val decode_ctrl_out_branch_i                     =       Input(UInt(1.W))
    val decode_branchLogic_output_i                  =       Input(UInt(1.W))
    val decode_hazardDetection_pc_i                  =       Input(SInt(32.W))
    val decode_hazardDetection_inst_i                =       Input(UInt(32.W))
    val decode_hazardDetection_current_pc_i          =       Input(SInt(32.W))
    val decode_hazardDetection_pc_forward_i          =       Input(UInt(1.W))
    val decode_hazardDetection_inst_forward_i        =       Input(UInt(1.W))

    // true when mret instruction decoded in decoded stage
    val decode_mret_inst_i                           =       Input(Bool())

    val core_stall_i                                 =       Input(Bool())


    // ------------------------------------- //
    //        decode stage (outputs)         //
    // ------------------------------------- //

    val decode_if_id_pc_o                            =       Output(SInt(32.W))
    val decode_if_id_pc4_o                           =       Output(SInt(32.W))
    val decode_if_id_inst_o                          =       Output(UInt(32.W))
  })

  val NOP = "b00000000000000000000000000010011".U(32.W) // NOP instruction

  // fetch always ready to accept data from bus
  io.coreInstrRsp.ready := true.B
  // always fetch 32 bits instructions
  io.coreInstrReq.bits.activeByteLane := "b1111".U
  // always read for fetch
  io.coreInstrReq.bits.isWrite := false.B
  // data output is dont care for reads
  io.coreInstrReq.bits.dataRequest := DontCare

  val pc = Module(new Pc())

  // by default setting the save pc in fetch to false.
  io.csrRegFile_csr_save_if_o := false.B
  io.csrRegFile_csr_if_pc_o := 0.U
  io.csrRegFile_csr_save_cause_o := false.B

  // IF/ID registers
  val if_id_pc_reg = Reg(SInt())
  val if_id_pc4_reg = Reg(SInt())
  val if_id_inst_reg = RegInit(NOP)

  // checking if we have an interrupt to deal with
  val handle_irq = io.csrRegFile_irq_pending_i && io.csrRegFile_csr_mstatus_mie_i

  // if irq then stopping the fetch for getting new instruction because we need to jump to the trap handler
  val halt_if = Wire(Bool())
  halt_if := Mux(handle_irq, true.B, false.B)

  // initializing the mtvec register
  io.csrRegFile_csr_mtvec_init_o := Mux(io.core_init_mtvec_i, true.B, false.B)

  // csr register file default outputs
  io.csrRegFile_csr_save_if_o := false.B
  io.csrRegFile_csr_save_cause_o := false.B
  io.csrRegFile_exc_cause_o := Exc_Cause.EXC_CAUSE_INSN_ADDR_MISA
  // Send the next pc value to the instruction memory
  io.coreInstrReq.bits.addrRequest := pc.io.in(13,0).asUInt()
//  io.core_instr_addr_o := pc.io.in(13, 0).asUInt
  // if device is ready to accept the request then send a valid signal to fetch from.
  io.coreInstrReq.valid := Mux(io.coreInstrReq.ready, true.B, false.B)
  // halt the pc if we cannot send data to the bus master
  pc.io.halt := Mux(io.coreInstrReq.ready, false.B, true.B)
  //io.core_instr_req_o := Mux(io.core_instr_gnt_i, true.B, false.B)
  // wait for valid signal to arrive indicating the fetched instruction is valid otherwise send NOP
  val instr = Mux(io.coreInstrRsp.valid, io.coreInstrRsp.bits.dataResponse, NOP)

  when(!io.core_stall_i && !halt_if) {
    // send the current pc value to the Decode stage
    if_id_pc_reg := pc.io.out
    // send the pc + 4 to the Decode stage
    if_id_pc4_reg := pc.io.out + 4.S    // not using pc4 since it is halted
  }

  when(!io.core_stall_i && !halt_if) {
    when(io.decode_hazardDetection_inst_forward_i === 1.U) {
      if_id_inst_reg := io.decode_hazardDetection_inst_i
      if_id_pc_reg := io.decode_hazardDetection_current_pc_i
    }.otherwise {
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
  when(!io.core_stall_i && !halt_if) {
    when(io.decode_hazardDetection_pc_forward_i === 1.U) {
      pc.io.in := io.decode_hazardDetection_pc_i
    }.otherwise {
      when(io.decode_ctrl_next_pc_sel_i === "b01".U) {
        when(io.decode_branchLogic_output_i === 1.U && io.decode_ctrl_out_branch_i === 1.U) {
          pc.io.in := io.decode_sb_imm_i
          if_id_pc_reg := 0.S
          if_id_pc4_reg := 0.S
          if_id_inst_reg := NOP
        }.otherwise {
          pc.io.in := pc.io.pc4
        }
      }.elsewhen(io.decode_ctrl_next_pc_sel_i === "b10".U) {
        pc.io.in := io.decode_uj_imm_i
        if_id_pc_reg := 0.S
        if_id_pc4_reg := 0.S
        if_id_inst_reg := NOP
      }.elsewhen(io.decode_ctrl_next_pc_sel_i === "b11".U) {
        pc.io.in := io.decode_jalr_imm_i
        if_id_pc_reg := 0.S
        if_id_pc4_reg := 0.S
        if_id_inst_reg := NOP
      }.elsewhen(io.decode_mret_inst_i) {
        pc.io.in := io.csrRegFile_csr_mepc_i.asSInt()
        if_id_pc_reg := 0.S
        if_id_pc4_reg := 0.S
        if_id_inst_reg := NOP
      }.otherwise {
        pc.io.in := pc.io.pc4
      }
    }
  }.elsewhen(!io.core_stall_i && halt_if) {
    pc.io.in := Cat(io.csrRegFile_csr_mtvec_i(31, 8), 0.U(1.W), Exc_Cause.EXC_CAUSE_IRQ_EXTERNAL_M(4, 0), 0.U(2.W)).asSInt()
    if_id_inst_reg := NOP // when halted pass NOP since we dont want to repeatedly send the current instruction as it will be executed twice
    io.csrRegFile_csr_save_if_o := true.B
    // Checking which pc to set in mepc. If the pc in had a branch instruction when interrupt came
    // then save the calculated branch addres in mepc to return back to correct instruction after mret
    // otherwise save pc's current value in mepc.
    io.csrRegFile_csr_if_pc_o := Mux(io.decode_ctrl_next_pc_sel_i === "b01".U, io.decode_sb_imm_i.asUInt(),
      Mux(io.decode_ctrl_next_pc_sel_i === "b10".U, io.decode_uj_imm_i.asUInt(),
        Mux(io.decode_ctrl_next_pc_sel_i === "b11".U, io.decode_jalr_imm_i.asUInt(), pc.io.out.asUInt())))
    io.csrRegFile_csr_save_cause_o := true.B
    io.csrRegFile_exc_cause_o := Exc_Cause.EXC_CAUSE_IRQ_EXTERNAL_M
  }
    .otherwise {
      pc.io.in := pc.io.out
    }


  io.decode_if_id_pc_o := if_id_pc_reg
  io.decode_if_id_pc4_o := if_id_pc4_reg
  io.decode_if_id_inst_o := if_id_inst_reg
}
