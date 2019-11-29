package datapath
import chisel3._

class Top extends Module {
    val io = IO(new Bundle {

        val reg_out = Output(SInt(32.W))
 })

    val imem = Module(new InstructionMem())
    val dmem = Module(new DataMem())
    val IF_ID = Module(new IF_ID())
    val ID_EX = Module(new ID_EX())
    val EX_MEM = Module(new EX_MEM())
    val MEM_WB = Module(new MEM_WB())
    val fetch = Module(new Fetch())
    val decode = Module(new Decode())
    val execute = Module(new Execute())
    val memory_stage = Module(new Memory())
    val writeback = Module(new WriteBack())


    imem.io.wrAddr := fetch.io.wrAddr

    // *********** ----------- INSTRUCTION FETCH (IF) STAGE ----------- ********* //
    fetch.io.inst_in := imem.io.readData
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
    decode.io.MEM_WB_ctrl_regWr := MEM_WB.io.ctrl_RegWr_out
    decode.io.MEM_WB_rd_sel := MEM_WB.io.rd_sel_out
    decode.io.ID_EX_ctrl_MemRd := ID_EX.io.ctrl_MemRd_out
    decode.io.ID_EX_rd_sel := ID_EX.io.rd_sel_out
    decode.io.EX_MEM_rd_sel := EX_MEM.io.rd_sel_out
    decode.io.EX_MEM_ctrl_MemRd := EX_MEM.io.ctrl_MemRd_out
    decode.io.MEM_WB_ctrl_MemRd := MEM_WB.io.ctrl_MemRd_out
    decode.io.writeback_write_data := writeback.io.write_data
    decode.io.alu_output := execute.io.alu_output
    decode.io.EX_MEM_alu_output := EX_MEM.io.alu_output
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

    ID_EX.io.pc_in := decode.io.pc_out
    ID_EX.io.pc4_in := decode.io.pc4_out
    ID_EX.io.func3_in := decode.io.inst_out(14,12)
    ID_EX.io.func7_in := decode.io.inst_out(30)
    ID_EX.io.rd_sel_in := decode.io.inst_out(11,7)
    ID_EX.io.rs1_sel_in := decode.io.inst_out(19, 15)
    ID_EX.io.rs2_sel_in := decode.io.inst_out(24, 20)



    // *********** ----------- EXECUTION (EX) STAGE ----------- ********* //

    execute.io.EX_MEM_rd_sel := EX_MEM.io.rd_sel_out
    execute.io.MEM_WB_rd_sel := MEM_WB.io.rd_sel_out
    execute.io.ID_EX_rs1_sel := ID_EX.io.rs1_sel_out
    execute.io.ID_EX_rs2_sel := ID_EX.io.rs2_sel_out
    execute.io.EX_MEM_ctrl_RegWr := EX_MEM.io.ctrl_RegWr_out
    execute.io.MEM_WB_ctrl_RegWr := MEM_WB.io.ctrl_RegWr_out
    execute.io.ID_EX_ctrl_OpA_sel := ID_EX.io.ctrl_OpA_sel_out
    execute.io.ID_EX_ctrl_OpB_sel := ID_EX.io.ctrl_OpB_sel_out
    execute.io.ID_EX_pc4 := ID_EX.io.pc4_out
    execute.io.ID_EX_rs1 := ID_EX.io.rs1_out
    execute.io.ID_EX_rs2 := ID_EX.io.rs2_out
    execute.io.EX_MEM_alu_output := EX_MEM.io.alu_output
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
    EX_MEM.io.alu_in := execute.io.alu_output

    // Passing the rd_sel value in the EX/MEM pipeline register
    EX_MEM.io.rd_sel_in := execute.io.rd_sel_out
    EX_MEM.io.rs2_sel_in := execute.io.rs2_sel_out
    EX_MEM.io.rs2_in := execute.io.rs2_out

    // Passing the control signals to EX/MEM pipeline register
    EX_MEM.io.ctrl_MemWr_in := execute.io.ctrl_MemWr_out
    EX_MEM.io.ctrl_MemRd_in := execute.io.ctrl_MemRd_out
    EX_MEM.io.ctrl_RegWr_in := execute.io.ctrl_RegWr_out
    EX_MEM.io.ctrl_MemToReg_in := execute.io.ctrl_MemToReg_out



    // *********** ----------- MEMORY (MEM) STAGE ----------- ********* //

    memory_stage.io.EX_MEM_alu_output := EX_MEM.io.alu_output
    memory_stage.io.EX_MEM_rd_sel := EX_MEM.io.rd_sel_out
    memory_stage.io.EX_MEM_RegWr := EX_MEM.io.ctrl_RegWr_out
    memory_stage.io.EX_MEM_MemRd := EX_MEM.io.ctrl_MemRd_out
    memory_stage.io.EX_MEM_MemToReg := EX_MEM.io.ctrl_MemToReg_out
    memory_stage.io.EX_MEM_MemWr := EX_MEM.io.ctrl_MemWr_out
    memory_stage.io.EX_MEM_rs2 := EX_MEM.io.rs2_out

    dmem.io.memAddress := memory_stage.io.memAddress
    dmem.io.memWrite := memory_stage.io.ctrl_MemWr_out
    dmem.io.memRead := memory_stage.io.ctrl_MemRd_out
    dmem.io.memData := memory_stage.io.rs2_out

    MEM_WB.io.alu_in := memory_stage.io.alu_output
    MEM_WB.io.dmem_data_in := dmem.io.memOut
    MEM_WB.io.rd_sel_in := memory_stage.io.rd_sel_out

    MEM_WB.io.ctrl_RegWr_in := memory_stage.io.ctrl_RegWr_out
    MEM_WB.io.ctrl_MemRd_in := memory_stage.io.ctrl_MemRd_out
    MEM_WB.io.ctrl_MemToReg_in := memory_stage.io.ctrl_MemToReg_out




    // *********** ----------- WRITE BACK (WB) STAGE ----------- ********* //


    writeback.io.MEM_WB_MemToReg := MEM_WB.io.ctrl_MemToReg_out
    writeback.io.MEM_WB_dataMem_data := MEM_WB.io.dmem_data_out
    writeback.io.MEM_WB_alu_output := MEM_WB.io.alu_output


    // Just for testing
    io.reg_out := writeback.io.write_data

}
