package core

import chisel3._
/** TODO: rs1 out and rs2 out must be 0 when instruction is lui */
class Decode extends Module {
  val io = IO(new Bundle {
 //   val enable_M_extension = Input(UInt(1.W))
    val IF_ID_inst = Input(UInt(32.W))
    val IF_ID_pc = Input(SInt(32.W))
    val IF_ID_pc4 = Input(SInt(32.W))
    val MEM_WB_ctrl_regWr = Input(UInt(1.W))
    val MEM_WB_rd_sel = Input(UInt(5.W))
    val ID_EX_ctrl_MemRd = Input(UInt(1.W))
    val ID_EX_rd_sel = Input(UInt(5.W))
    val EX_MEM_rd_sel = Input(UInt(5.W))
    val EX_MEM_ctrl_MemRd = Input(UInt(1.W))
    val MEM_WB_ctrl_MemRd = Input(UInt(1.W))
    val alu_output = Input(SInt(32.W))
    val EX_MEM_alu_output = Input(SInt(32.W))
    val dmem_memOut = Input(SInt(32.W))
    val writeback_write_data = Input(SInt(32.W))

    val execute_regwrite = Input(UInt(1.W))
    val mem_regwrite     = Input(UInt(1.W))
    val wb_regwrite      = Input(UInt(1.W))

    //val stall = Input(UInt(1.W))

    val pc_out = Output(SInt(32.W))
    val pc4_out = Output(SInt(32.W))
    val inst_op_out = Output(UInt(32.W))
    val func3_out = Output(UInt(3.W))
    val func7_out = Output(UInt(7.W))
    val rd_sel_out = Output(UInt(5.W))
    val rs1_sel_out = Output(UInt(5.W))
    val rs2_sel_out = Output(UInt(5.W))
    val rs1_out = Output(SInt(32.W))
    val rs2_out = Output(SInt(32.W))
    val imm_out = Output(SInt(32.W))
    val sb_imm = Output(SInt(32.W))
    val uj_imm = Output(SInt(32.W))
    val jalr_output = Output(SInt(32.W))
    val branchLogic_output = Output(UInt(1.W))
    val hazardDetection_pc_out = Output(SInt(32.W))
    val hazardDetection_inst_out = Output(UInt(32.W))
    val hazardDetection_current_pc_out = Output(SInt(32.W))
    val hazardDetection_pc_forward = Output(UInt(1.W))
    val hazardDetection_inst_forward = Output(UInt(1.W))
    val ctrl_MemWr_out = Output(UInt(1.W))
    val ctrl_MemRd_out = Output(UInt(1.W))
    val ctrl_Branch_out = Output(UInt(1.W))
    val ctrl_RegWr_out = Output(UInt(1.W))
    val ctrl_MemToReg_out = Output(UInt(1.W))
    val ctrl_AluOp_out = Output(UInt(4.W))
    val ctrl_OpA_sel_out = Output(UInt(2.W))
    val ctrl_OpB_sel_out = Output(UInt(1.W))
    val ctrl_next_pc_sel_out = Output(UInt(2.W))
    val reg_7_out = Output(SInt(32.W))
    val mret_inst_o = Output(Bool())
  //  val M_extension_enabled = Output(UInt(1.W))
  })

  val hazardDetection = Module(new HazardDetection())
  val control = Module(new Control())
  val decodeForwardUnit = Module(new DecodeForwardUnit())
  val branchLogic = Module(new BranchLogic())
  val reg_file = Module(new RegisterFile())
  val imm_generation = Module(new ImmediateGeneration())
  val structuralDetector = Module(new StructuralDetector())
  val jalr = Module(new Jalr())


  // detecting MRET instruction
  io.mret_inst_o := Mux(io.IF_ID_inst(14,12) === "b000".U && io.IF_ID_inst(31,20) === "h302".U(12.W), true.B, false.B)

  // Initialize Hazard Detection unit
  hazardDetection.io.IF_ID_INST := io.IF_ID_inst
  hazardDetection.io.ID_EX_MEMREAD := io.ID_EX_ctrl_MemRd
  hazardDetection.io.ID_EX_REGRD := io.ID_EX_rd_sel
  hazardDetection.io.pc_in := io.IF_ID_pc4
  hazardDetection.io.current_pc := io.IF_ID_pc
  hazardDetection.io.IF_ID_MEMREAD := control.io.out_memRead

  // Sending hazard detection outputs for Fetch
  io.hazardDetection_pc_out := hazardDetection.io.pc_out
  io.hazardDetection_current_pc_out := hazardDetection.io.current_pc_out
  io.hazardDetection_pc_forward := hazardDetection.io.pc_forward
  io.hazardDetection_inst_out := hazardDetection.io.inst_out
  io.hazardDetection_inst_forward := hazardDetection.io.inst_forward

  // Initialize Control Unit
  control.io.in_opcode := io.IF_ID_inst(6, 0)
 // control.io.enable_M_extension := io.enable_M_extension // M extension
  control.io.func7      := io.IF_ID_inst(31,25)

  // Initialize Decode Forward Unit
  decodeForwardUnit.io.ID_EX_REGRD := io.ID_EX_rd_sel
  decodeForwardUnit.io.ID_EX_MEMRD := io.ID_EX_ctrl_MemRd
  decodeForwardUnit.io.EX_MEM_REGRD := io.EX_MEM_rd_sel
  decodeForwardUnit.io.MEM_WB_REGRD := io.MEM_WB_rd_sel
  decodeForwardUnit.io.EX_MEM_MEMRD := io.EX_MEM_ctrl_MemRd
  decodeForwardUnit.io.MEM_WB_MEMRD := io.MEM_WB_ctrl_MemRd
  decodeForwardUnit.io.rs1_sel := io.IF_ID_inst(19, 15)
  decodeForwardUnit.io.rs2_sel := io.IF_ID_inst(24, 20)
  decodeForwardUnit.io.ctrl_branch := control.io.out_branch

  decodeForwardUnit.io.execute_regwrite := io.execute_regwrite
  decodeForwardUnit.io.mem_regwrite := io.mem_regwrite
  decodeForwardUnit.io.wb_regwrite := io.wb_regwrite

  branchLogic.io.in_func3 := io.IF_ID_inst(14,12)

  // FOR REGISTER RS1 in BRANCH LOGIC UNIT and JALR UNIT

  // These forwarding values come only when the Control's branch pin is high which means SB-Type
  // instruction is in the decode stage so we don't need to forward any values to the JALR unit
  // Hence for all these conditions we wire JALR unit with register file's output by default.
  when(decodeForwardUnit.io.forward_rs1 === "b0000".U) {
    // No hazard just use register file data
    branchLogic.io.in_rs1 := reg_file.io.rs1
    jalr.io.input_a := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b0001".U) {
    // hazard in alu stage forward data from alu output
    branchLogic.io.in_rs1 := io.alu_output
    jalr.io.input_a := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b0010".U) {
    // hazard in EX/MEM stage forward data from EX/MEM.alu_output
    branchLogic.io.in_rs1 := io.EX_MEM_alu_output
    jalr.io.input_a := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b0011".U) {
    // hazard in MEM/WB stage forward data from register file write data which will have correct data from the MEM/WB mux
    branchLogic.io.in_rs1 := reg_file.io.writeData
    jalr.io.input_a := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b0100".U) {
    // hazard in EX/MEM stage and load type instruction so forwarding from data memory data output instead of EX/MEM.alu_output
    branchLogic.io.in_rs1 := io.dmem_memOut
    jalr.io.input_a := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b0101".U) {
    // hazard in MEM/WB stage and load type instruction so forwarding from register file write data which will have the correct output from the mux
    branchLogic.io.in_rs1 := reg_file.io.writeData
    jalr.io.input_a := reg_file.io.rs1
  }

    // These forwarding values come only when the Control's branch pin is low which means JALR
    // instruction maybe in the decode stage so we don't need to forward any values to the Branch Logic unit
    // Hence for all these conditions we wire Branch Logic unit with register file's output by default.

    .elsewhen(decodeForwardUnit.io.forward_rs1 === "b0110".U) {
      // hazard in alu stage forward data from alu output
      jalr.io.input_a := io.alu_output
      branchLogic.io.in_rs1 := reg_file.io.rs1
    } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b0111".U) {
    // hazard in EX/MEM stage forward data from EX/MEM.alu_output
    jalr.io.input_a := io.EX_MEM_alu_output
    branchLogic.io.in_rs1 := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b1000".U) {
    // hazard in MEM/WB stage forward data from register file write data which will have correct data from the MEM/WB mux
    jalr.io.input_a := reg_file.io.writeData
    branchLogic.io.in_rs1 := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b1001".U) {
    // hazard in EX/MEM stage and load type instruction so forwarding from data memory data output instead of EX/MEM.alu_output
    jalr.io.input_a := io.dmem_memOut
    branchLogic.io.in_rs1 := reg_file.io.rs1
  } .elsewhen(decodeForwardUnit.io.forward_rs1 === "b1010".U) {
    // hazard in MEM/WB stage and load type instruction so forwarding from register file write data which will have the correct output from the mux
    jalr.io.input_a := reg_file.io.writeData
    branchLogic.io.in_rs1 := reg_file.io.rs1
  }
    .otherwise {
      branchLogic.io.in_rs1 := reg_file.io.rs1
      jalr.io.input_a := reg_file.io.rs1
    }


  // FOR REGISTER RS2 in BRANCH LOGIC UNIT
  when(decodeForwardUnit.io.forward_rs2 === "b0000".U) {
    // No hazard just use register file data
    branchLogic.io.in_rs2 := reg_file.io.rs2
  } .elsewhen(decodeForwardUnit.io.forward_rs2 === "b0001".U) {
    // hazard in alu stage forward data from alu output
    branchLogic.io.in_rs2 := io.alu_output
  } .elsewhen(decodeForwardUnit.io.forward_rs2 === "b0010".U) {
    // hazard in EX/MEM stage forward data from EX/MEM.alu_output
    branchLogic.io.in_rs2 := io.EX_MEM_alu_output
  } .elsewhen(decodeForwardUnit.io.forward_rs2 === "b0011".U) {
    // hazard in MEM/WB stage forward data from register file write data which will have correct data from the MEM/WB mux
    branchLogic.io.in_rs2 := reg_file.io.writeData
  } .elsewhen(decodeForwardUnit.io.forward_rs2 === "b0100".U) {
    // hazard in EX/MEM stage and load type instruction so forwarding from data memory data output instead of EX/MEM.alu_output
    branchLogic.io.in_rs2 := io.dmem_memOut
  } .elsewhen(decodeForwardUnit.io.forward_rs2 === "b0101".U) {
    // hazard in MEM/WB stage and load type instruction so forwarding from register file write data which will have the correct output from the mux
    branchLogic.io.in_rs2 := reg_file.io.writeData
  }
    .otherwise {
      branchLogic.io.in_rs2 := reg_file.io.rs2
    }

  jalr.io.input_b := imm_generation.io.i_imm

  // Sending the branch logic unit output for Fetch
  io.branchLogic_output := branchLogic.io.output

  // The Mux after the Control module which selects the control inputs of
  // the ID/EX Pipeline register either from the Control or default 0 values
  // for stalling the pipeline one clock cycle.
  when(hazardDetection.io.ctrl_forward === "b1".U) {
    setControlPinsToZero()
  } .otherwise {
    sendDefaultControlPins()
  }



  // Initialize Register File
  reg_file.io.rs1_sel := io.IF_ID_inst(19, 15)
  reg_file.io.rs2_sel := io.IF_ID_inst(24, 20)
  reg_file.io.regWrite := io.MEM_WB_ctrl_regWr
//  reg_file.io.stall := io.stall
  reg_file.io.rd_sel := io.MEM_WB_rd_sel
  reg_file.io.writeData := io.writeback_write_data


  // Initialize Immediate Generation
  imm_generation.io.instruction := io.IF_ID_inst
  imm_generation.io.pc := io.IF_ID_pc

  // Sending immediate generation outputs for Fetch
  io.sb_imm := imm_generation.io.sb_imm
  io.uj_imm := imm_generation.io.uj_imm
  io.jalr_output := jalr.io.output

  // Initialize Structural Hazard Detector
  structuralDetector.io.rs1_sel := io.IF_ID_inst(19, 15)
  structuralDetector.io.rs2_sel := io.IF_ID_inst(24, 20)
  structuralDetector.io.MEM_WB_REGRD := io.MEM_WB_rd_sel
  structuralDetector.io.MEM_WB_regWr := io.MEM_WB_ctrl_regWr
  structuralDetector.io.inst_op_in := io.IF_ID_inst(6,0)
  // FOR RS1
  when(structuralDetector.io.fwd_rs1 === 1.U) {
    // additionally checking if the instruction is lui or not. We should not pass out
    // any value from the rs1 if lui is currently being decoded since it does not have
    // an rs1 field in it's encoding
    io.rs1_out := Mux(io.IF_ID_inst(6,0) =/= "b0110111".U, reg_file.io.writeData, 0.S)
  } .otherwise {
    io.rs1_out := Mux(io.IF_ID_inst(6,0) =/= "b0110111".U, reg_file.io.rs1, 0.S)
  }

  // FOR RS2
  when(structuralDetector.io.fwd_rs2 === 1.U) {
    // additionally checking if the instruction is lui or not. We should not pass out
    // any value from the rs2 if lui is currently being decoded since it does not have
    // an rs2 field in it's encoding
    io.rs2_out := Mux(io.IF_ID_inst(6,0) =/= "b0110111".U, reg_file.io.writeData, 0.S)
  } .otherwise {
    io.rs2_out := Mux(io.IF_ID_inst(6,0) =/= "b0110111".U, reg_file.io.rs2, 0.S)
  }

  when(control.io.out_extend_sel === "b00".U) {
    // I-Type instruction
    io.imm_out := imm_generation.io.i_imm
  } .elsewhen(control.io.out_extend_sel === "b01".U) {
    // S-Type instruction
    io.imm_out := imm_generation.io.s_imm
  } .elsewhen(control.io.out_extend_sel === "b10".U) {
    // U-Type instruction
    io.imm_out := imm_generation.io.u_imm
  } .otherwise {
    io.imm_out := 0.S(32.W)
  }

  io.pc_out := io.IF_ID_pc
  io.pc4_out := io.IF_ID_pc4
  io.inst_op_out := io.IF_ID_inst(6,0)    // used by the forward unit to see if instruction is eligible for data hazards
  io.func3_out := io.IF_ID_inst(14,12)
  io.func7_out := io.IF_ID_inst(31,25)
  io.rd_sel_out := io.IF_ID_inst(11,7)
  io.rs1_sel_out := io.IF_ID_inst(19,15)
  io.rs2_sel_out := io.IF_ID_inst(24,20)

  def setControlPinsToZero() : Unit = {
    io.ctrl_MemWr_out := 0.U
    io.ctrl_MemRd_out := 0.U
    io.ctrl_Branch_out := 0.U
    io.ctrl_RegWr_out := 0.U
    io.ctrl_MemToReg_out := 0.U
    io.ctrl_AluOp_out := 0.U
    io.ctrl_OpA_sel_out := 0.U
    io.ctrl_OpB_sel_out := 0.U
    io.ctrl_next_pc_sel_out := 0.U
 //   io.M_extension_enabled := 0.U
  }

  def sendDefaultControlPins() : Unit = {
    io.ctrl_MemWr_out := control.io.out_memWrite
    io.ctrl_MemRd_out := control.io.out_memRead
    io.ctrl_Branch_out := control.io.out_branch
    io.ctrl_RegWr_out := control.io.out_regWrite
    io.ctrl_MemToReg_out := control.io.out_memToReg
    io.ctrl_AluOp_out := control.io.out_aluOp
    io.ctrl_OpA_sel_out := control.io.out_operand_a_sel
    io.ctrl_OpB_sel_out := control.io.out_operand_b_sel
    io.ctrl_next_pc_sel_out := control.io.out_next_pc_sel
 //   io.M_extension_enabled := control.io.M_extension_enabled
  }

  io.reg_7_out := reg_file.io.reg_7


}