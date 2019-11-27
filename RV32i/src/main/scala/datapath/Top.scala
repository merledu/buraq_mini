package datapath
import chisel3._

class Top extends Module {
    val io = IO(new Bundle {

        val reg_out = Output(SInt(32.W))
 })

    //val control = Module(new Control())
    //val reg_file = Module(new RegisterFile())
    //val alu = Module(new Alu())
    //val alu_control = Module(new AluControl())
    //val imm_generation = Module(new ImmediateGeneration())
    //val jalr = Module(new Jalr())
    val dmem = Module(new DataMem())
    val IF_ID = Module(new IF_ID())
    val ID_EX = Module(new ID_EX())
    val EX_MEM = Module(new EX_MEM())
    val MEM_WB = Module(new MEM_WB())
    //val forwardUnit = Module(new ForwardUnit())
    //val forwardUnitMem = Module(new ForwardUnitMem())
    //val hazardDetection = Module(new HazardDetection())
    //val branchLogic = Module(new BranchLogic())
    //val decodeForwardUnit = Module(new DecodeForwardUnit())
    //val structuralDetector = Module(new StructuralDetector())
    val fetch = Module(new Fetch())
    val decode = Module(new Decode())
    val execute = Module(new Execute())
    val writeback = Module(new WriteBack())


    // *********** ----------- INSTRUCTION FETCH (IF) STAGE ----------- ********* //

    fetch.io.sb_imm := decode.io.sb_imm
    fetch.io.uj_imm := decode.io.uj_imm
    fetch.io.jalr_imm := decode.io.jalr_output
    fetch.io.ctrl_next_pc_sel := decode.io.ctrl_next_pc_sel_out
    fetch.io.ctrl_out_branch := decode.io.ctrl_Branch_out
    fetch.io.branchLogic_output := decode.io.branchLogic_output
    fetch.io.hazardDetection_pc_out := decode.io.hazardDetection_pc_out
    fetch.io.hazardDetection_inst_out := decode.io.hazardDetection_inst_out
    fetch.io.hazardDetection_current_pc_out := decode.io.hazardDetection_current_pc_out
    fetch.io.hazardDetection_pc_forward := decode.io.hazardDetection_pc_forward
    fetch.io.hazardDetection_inst_forward := decode.io.hazardDetection_inst_forward

    IF_ID.io.pc_in := fetch.io.pc_out
    IF_ID.io.pc4_in := fetch.io.pc4_out
    IF_ID.io.inst_in := fetch.io.inst_out


    // *********** ----------- INSTRUCTION DECODE (ID) STAGE ----------- ********* //

    decode.io.IF_ID_inst := IF_ID.io.inst_out
    decode.io.IF_ID_pc := IF_ID.io.pc_out
    decode.io.IF_ID_pc4 := IF_ID.io.pc4_out
    decode.io.MEM_WB_ctrl_regWr := MEM_WB.io.mem_wb_regWr_output
    decode.io.MEM_WB_rd_sel := MEM_WB.io.mem_wb_rdSel_output
    decode.io.ID_EX_ctrl_MemRd := ID_EX.io.ctrl_MemRd_out
    decode.io.ID_EX_rd_sel := ID_EX.io.rd_sel_out
    decode.io.EX_MEM_rd_sel := EX_MEM.io.ex_mem_rdSel_output
    decode.io.EX_MEM_ctrl_MemRd := EX_MEM.io.ex_mem_memRd_out
    decode.io.MEM_WB_ctrl_MemRd := MEM_WB.io.mem_wb_memRd_output
    decode.io.writeback_write_data := writeback.io.write_data
    decode.io.alu_output := execute.io.alu_output
    decode.io.EX_MEM_alu_output := EX_MEM.io.ex_mem_alu_output
    decode.io.dmem_memOut := dmem.io.memOut

    ID_EX.io.ctrl_MemWr_in := decode.io.ctrl_MemWr_out
    ID_EX.io.ctrl_MemRd_in := decode.io.ctrl_MemRd_out
    ID_EX.io.ctrl_Branch_in := decode.io.ctrl_Branch_out
    ID_EX.io.ctrl_RegWr_in := decode.io.ctrl_RegWr_out
    ID_EX.io.ctrl_MemToReg_in := decode.io.ctrl_MemToReg_out
    ID_EX.io.ctrl_AluOp_in := decode.io.ctrl_AluOp_out
    ID_EX.io.ctrl_OpA_sel_in := decode.io.ctrl_OpA_sel_out
    ID_EX.io.ctrl_OpB_sel_in := decode.io.ctrl_OpB_sel_out
    ID_EX.io.ctrl_nextPc_sel_in := decode.io.ctrl_next_pc_sel_out

    ID_EX.io.rs1_in := decode.io.rs1_out
    ID_EX.io.rs2_in := decode.io.rs2_out
    ID_EX.io.imm := decode.io.imm_out

    ID_EX.io.IF_ID_pc := decode.io.pc_out
    ID_EX.io.IF_ID_pc4 := decode.io.pc4_out
    ID_EX.io.func3_in := decode.io.inst_out(14,12)
    ID_EX.io.func7_in := decode.io.inst_out(30)
    ID_EX.io.rd_sel_in := decode.io.inst_out(11,7)
    ID_EX.io.rs1_sel_in := decode.io.inst_out(19, 15)
    ID_EX.io.rs2_sel_in := decode.io.inst_out(24, 20)



    // *********** ----------- EXECUTION (EX) STAGE ----------- ********* //

    execute.io.EX_MEM_rd_sel := EX_MEM.io.ex_mem_rdSel_output
    execute.io.MEM_WB_rd_sel := MEM_WB.io.mem_wb_rdSel_output
    execute.io.ID_EX_rs1_sel := ID_EX.io.rs1_sel_out
    execute.io.ID_EX_rs2_sel := ID_EX.io.rs2_sel_out
    execute.io.EX_MEM_ctrl_RegWr := EX_MEM.io.ex_mem_regWr_out
    execute.io.MEM_WB_ctrl_RegWr := MEM_WB.io.mem_wb_regWr_output
    execute.io.ID_EX_ctrl_OpA_sel := ID_EX.io.ctrl_OpA_sel_out
    execute.io.ID_EX_ctrl_OpB_sel := ID_EX.io.ctrl_OpB_sel_out
    execute.io.ID_EX_pc4 := ID_EX.io.pc4_out
    execute.io.ID_EX_rs1 := ID_EX.io.rs1_out
    execute.io.ID_EX_rs2 := ID_EX.io.rs2_out
    execute.io.EX_MEM_alu_output := EX_MEM.io.ex_mem_alu_output
    execute.io.writeback_write_data := writeback.io.write_data
    execute.io.ID_EX_imm := ID_EX.io.imm_out
    execute.io.ID_EX_ctrl_AluOp := ID_EX.io.ctrl_AluOp_out
    execute.io.ID_EX_func7 := ID_EX.io.func7_out
    execute.io.ID_EX_func3 := ID_EX.io.func3_out
    execute.io.ID_EX_rd_sel := ID_EX.io.rd_sel_out
    execute.io.ID_EX_ctrl_MemWr := ID_EX.io.ctrl_MemWr_out
    execute.io.ID_EX_ctrl_MemRd := ID_EX.io.ctrl_MemRd_out
    execute.io.ID_EX_ctrl_RegWr := ID_EX.io.ctrl_RegWr_out
    execute.io.ID_EX_ctrl_MemToReg := ID_EX.io.ctrl_MemToReg_out

    // Passing the ALU output to the EX/MEM pipeline register
    EX_MEM.io.alu_output := execute.io.alu_output

    // Passing the rd_sel value in the EX/MEM pipeline register
    EX_MEM.io.ID_EX_RDSEL := execute.io.rd_sel_out
    EX_MEM.io.ID_EX_RS2SEL := execute.io.rs2_sel_out
    EX_MEM.io.ID_EX_RS2 := execute.io.rs2_out

    // Passing the control signals to EX/MEM pipeline register
    EX_MEM.io.ID_EX_MEMWR := execute.io.ctrl_MemWr_out
    EX_MEM.io.ID_EX_MEMRD := execute.io.ctrl_MemRd_out
    EX_MEM.io.ID_EX_REGWR := execute.io.ctrl_RegWr_out
    EX_MEM.io.ID_EX_MEMTOREG := execute.io.ctrl_MemToReg_out



    // *********** ----------- MEMORY (MEM) STAGE ----------- ********* //


    initialize_MEM_WB_Reg()

    //initializeMemForwardUnit()

    initializeDataMem()

    // when(forwardUnitMem.io.forward === "b1".U) {
    //   dmem.io.memData := MEM_WB.io.mem_wb_dataMem_data
    // } .otherwise {
      dmem.io.memData := EX_MEM.io.ex_mem_rs2_output
    // }



    // *********** ----------- WRITE BACK (WB) STAGE ----------- ********* //


    // when(MEM_WB.io.mem_wb_memToReg_output === "b1".U) {
    //     reg_file.io.writeData := MEM_WB.io.mem_wb_dataMem_data
    // } .otherwise {
    //     reg_file.io.writeData := MEM_WB.io.mem_wb_alu_output
    // }

    writeback.io.MEM_WB_MemToReg := MEM_WB.io.mem_wb_memToReg_output
    writeback.io.MEM_WB_dataMem_data := MEM_WB.io.mem_wb_dataMem_data
    writeback.io.MEM_WB_alu_output := MEM_WB.io.mem_wb_alu_output





    // def setControlPinsToZeroAndForwardToID_EX() : Unit = {
    //     ID_EX.io.ctrl_MemWr_in := 0.U
    //     ID_EX.io.ctrl_MemRd_in := 0.U
    //     ID_EX.io.ctrl_Branch_in := 0.U
    //     ID_EX.io.ctrl_RegWr_in := 0.U
    //     ID_EX.io.ctrl_MemToReg_in := 0.U
    //     ID_EX.io.ctrl_AluOp_in := 0.U
    //     ID_EX.io.ctrl_OpA_sel_in := 0.U
    //     ID_EX.io.ctrl_OpB_sel_in := 0.U
    //     ID_EX.io.ctrl_nextPc_sel_in := 0.U
    // }

    // def sendDefaultControlPinsToID_EX() : Unit = {
    //     ID_EX.io.ctrl_MemWr_in := control.io.out_memWrite
    //     ID_EX.io.ctrl_MemRd_in := control.io.out_memRead
    //     ID_EX.io.ctrl_Branch_in := control.io.out_branch
    //     ID_EX.io.ctrl_RegWr_in := control.io.out_regWrite
    //     ID_EX.io.ctrl_MemToReg_in := control.io.out_memToReg
    //     ID_EX.io.ctrl_AluOp_in := control.io.out_aluOp
    //     ID_EX.io.ctrl_OpA_sel_in := control.io.out_operand_a_sel
    //     ID_EX.io.ctrl_OpB_sel_in := control.io.out_operand_b_sel
    //     ID_EX.io.ctrl_nextPc_sel_in := control.io.out_next_pc_sel
    // }

    // def initializeHazardDetection() : Unit = {
    //     hazardDetection.io.IF_ID_INST := IF_ID.io.inst_out
    //     hazardDetection.io.ID_EX_MEMREAD := ID_EX.io.ctrl_MemRd_out
    //     hazardDetection.io.ID_EX_REGRD := ID_EX.io.rd_sel_out
    //     hazardDetection.io.pc_in := IF_ID.io.pc4_out
    //     hazardDetection.io.current_pc := IF_ID.io.pc_out
    // }

    // def initializeControl() : Unit = {
    //     control.io.in_opcode := IF_ID.io.inst_out(6, 0)     // 7-bit opcode to Control
    // }

    // def initializeImmediateGeneration() : Unit = {
    //     imm_generation.io.instruction := IF_ID.io.inst_out
    //     imm_generation.io.pc := IF_ID.io.pc_out
    // }

    // def initializeRegisterFile() : Unit = {
    //     reg_file.io.rs1_sel := IF_ID.io.inst_out(19, 15)
    //     reg_file.io.rs2_sel := IF_ID.io.inst_out(24, 20)
    //     reg_file.io.regWrite := MEM_WB.io.mem_wb_regWr_output
    //     reg_file.io.rd_sel := MEM_WB.io.mem_wb_rdSel_output
    // }

    // def initialize_ID_EX_Reg() : Unit = {
    //     ID_EX.io.IF_ID_pc := IF_ID.io.pc_out
    //     ID_EX.io.IF_ID_pc4 := IF_ID.io.pc4_out
    //     ID_EX.io.func3_in := IF_ID.io.inst_out(14,12)
    //     ID_EX.io.func7_in := IF_ID.io.inst_out(30)
    //     ID_EX.io.rd_sel_in := IF_ID.io.inst_out(11,7)
    //     ID_EX.io.rs1_sel_in := IF_ID.io.inst_out(19, 15)
    //     ID_EX.io.rs2_sel_in := IF_ID.io.inst_out(24, 20)

    //     initializeStructuralDetector()
    //     // FOR RS1
    //     when(structuralDetector.io.fwd_rs1 === 1.U) {
    //       ID_EX.io.rs1_in := reg_file.io.writeData
    //     } .otherwise {
    //       ID_EX.io.rs1_in := reg_file.io.rs1
    //     }
    //     // FOR RS2
    //     when(structuralDetector.io.fwd_rs2 === 1.U) {
    //       ID_EX.io.rs2_in := reg_file.io.writeData
    //     } .otherwise {
    //       ID_EX.io.rs2_in := reg_file.io.rs2
    //     }


    //     when(control.io.out_extend_sel === "b00".U) {
    //         // I-Type instruction
    //         ID_EX.io.imm := imm_generation.io.i_imm
    //     } .elsewhen(control.io.out_extend_sel === "b01".U) {
    //         // S-Type instruction
    //         ID_EX.io.imm := imm_generation.io.s_imm
    //     } .elsewhen(control.io.out_extend_sel === "b10".U) {
    //         // U-Type instruction
    //         ID_EX.io.imm := imm_generation.io.u_imm
    //     } .otherwise {
    //         ID_EX.io.imm := 0.S(32.W)
    //     }
    // }

//    def initializeForwardUnit() : Unit = {
//        forwardUnit.io.EX_MEM_REGRD := EX_MEM.io.ex_mem_rdSel_output
//        forwardUnit.io.MEM_WB_REGRD := MEM_WB.io.mem_wb_rdSel_output
//        forwardUnit.io.ID_EX_REGRS1 := ID_EX.io.rs1_sel_out
//        forwardUnit.io.ID_EX_REGRS2 := ID_EX.io.rs2_sel_out
//        forwardUnit.io.EX_MEM_REGWR := EX_MEM.io.ex_mem_regWr_out
//        forwardUnit.io.MEM_WB_REGWR := MEM_WB.io.mem_wb_regWr_output
//    }

//    def initializeAluControl() : Unit = {
//        alu_control.io.aluOp := ID_EX.io.ctrl_AluOp_out
//        alu_control.io.func7 := ID_EX.io.func7_out
//        alu_control.io.func3 := ID_EX.io.func3_out
//    }

//    def initialize_EX_MEM_Reg() : Unit = {
//        // Passing the ALU output to the EX/MEM pipeline register
//        EX_MEM.io.alu_output := alu.io.output
//
//        // Passing the rd_sel value in the EX/MEM pipeline register
//        EX_MEM.io.ID_EX_RDSEL := ID_EX.io.rd_sel_out
//        EX_MEM.io.ID_EX_RS2SEL := ID_EX.io.rs2_sel_out
//
//        // Passing the control signals to EX/MEM pipeline register
//        EX_MEM.io.ID_EX_MEMWR := ID_EX.io.ctrl_MemWr_out
//        EX_MEM.io.ID_EX_MEMRD := ID_EX.io.ctrl_MemRd_out
//        EX_MEM.io.ID_EX_REGWR := ID_EX.io.ctrl_RegWr_out
//        EX_MEM.io.ID_EX_MEMTOREG := ID_EX.io.ctrl_MemToReg_out
//    }

    def initialize_MEM_WB_Reg() : Unit = {
        MEM_WB.io.in_alu_output := EX_MEM.io.ex_mem_alu_output
        MEM_WB.io.in_dataMem_data := dmem.io.memOut
        MEM_WB.io.EX_MEM_RDSEL := EX_MEM.io.ex_mem_rdSel_output

        MEM_WB.io.EX_MEM_REGWR := EX_MEM.io.ex_mem_regWr_out
        MEM_WB.io.EX_MEM_MEMRD := EX_MEM.io.ex_mem_memRd_out
        MEM_WB.io.EX_MEM_MEMTOREG := EX_MEM.io.ex_mem_memToReg_out
    }

    // def initializeMemForwardUnit() : Unit = {
    //     forwardUnitMem.io.EX_MEM_RS2SEL := EX_MEM.io.ex_mem_rs2Sel_output
    //     forwardUnitMem.io.MEM_WB_RDSEL := MEM_WB.io.mem_wb_rdSel_output
    //     forwardUnitMem.io.EX_MEM_MEMWR := EX_MEM.io.ex_mem_memWr_out
    //     forwardUnitMem.io.MEM_WB_MEMRD := MEM_WB.io.mem_wb_memRd_output
    // }

    def initializeDataMem() : Unit = {
        dmem.io.memAddress := EX_MEM.io.ex_mem_alu_output(11, 2).asUInt
        dmem.io.memWrite := EX_MEM.io.ex_mem_memWr_out
        dmem.io.memRead := EX_MEM.io.ex_mem_memRd_out
    }

    // def initializeStructuralDetector() : Unit = {
    //   structuralDetector.io.rs1_sel := IF_ID.io.inst_out(19, 15)
    //   structuralDetector.io.rs2_sel := IF_ID.io.inst_out(24, 20)
    //   structuralDetector.io.MEM_WB_REGRD := MEM_WB.io.mem_wb_rdSel_output
    //   structuralDetector.io.MEM_WB_regWr := MEM_WB.io.mem_wb_regWr_output
    // }

    // Just for testing
    io.reg_out := writeback.io.write_data

}
