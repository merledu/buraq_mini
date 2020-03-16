package core

import chisel3._

class Core extends Module {
    val io = IO(new Bundle {
        // Channel D wires coming from the ICCM Controller's slave interface outside the core.
        val iccm_d_opcode = Input(UInt(3.W))
        val iccm_d_source = Input(UInt(32.W))
        val iccm_d_denied = Input(Bool())
        val iccm_d_valid = Input(Bool())
        val iccm_d_data = Input(UInt(32.W))
        // Channel D wires coming from the DCCM Controller's slave interface outside the core.
        val dccm_d_opcode = Input(UInt(3.W))
        val dccm_d_source = Input(UInt(32.W))
        val dccm_d_denied = Input(Bool())
        val dccm_d_valid = Input(Bool())
        val dccm_d_data = Input(UInt(32.W))

        // GPIO values coming in from the GPIO controller to the core for load/store bus controller
        val GPIO_values = Input(UInt(32.W))

        /**
         * The done pin coming in from the UART Controller intended to go into the FetchBusController
         * through the core. It's purpose is to tell the FetchBusController that UART is done writing
         * to the memory and the FetchBusController can remove the stall from the core and continue to
         * fetch the instructions from the Instruction memory*/
        val UART_DONE = Input(Bool())

        // Channel A outputs from the core to the ICCM controller's slave interface
        val iccm_a_address = Output(UInt(32.W))
        val iccm_a_data = Output(UInt(32.W))
        val iccm_a_opcode = Output(UInt(3.W))
        val iccm_a_source = Output(UInt(32.W))
        val iccm_a_valid = Output(Bool())
        // Channel A outputs from the core to the DCCM controller's slave interface
        val dccm_a_address = Output(UInt(32.W))
        val dccm_a_data = Output(UInt(32.W))
        val dccm_a_opcode = Output(UInt(3.W))
        val dccm_a_source = Output(UInt(32.W))
        val dccm_a_valid = Output(Bool())

        // GPIO set or read outputs from the core to the GPIO Controller
        val readGPIO = Output(UInt(1.W))
        val setGPIO = Output(UInt(1.W))
        // The data to be written to the GPIO pins
        val gpioData = Output(UInt(32.W))

        /**
         * The stall pin output coming from the FetchBusController going out from the core for UART Controller
         * as well as for ICCM Controller*/
        val isStalled = Output(Bool())

        val reg_7 = Output(SInt(32.W))
        val reg_out = Output(SInt(32.W))
 })
    val staller = Module(new Staller)
    val fetchBusController = Module(new FetchBusController)
    val loadStoreBusController = Module(new LoadStoreBusController)
    val IF_ID = Module(new IF_ID())
    val ID_EX = Module(new ID_EX())
    val EX_MEM = Module(new EX_MEM())
    val MEM_WB = Module(new MEM_WB())
    val fetch = Module(new Fetch())
    val decode = Module(new Decode())
    val execute = Module(new Execute())
    val memory_stage = Module(new MemoryStage())
    val writeback = Module(new WriteBack())
    val memReadReg = RegInit(0.U(32.W))
    val memWriteReg = RegInit(0.U(32.W))

   // Initializing fetch bus controller
   fetchBusController.io.pcAddr := fetch.io.wrAddr.asUInt
   fetchBusController.io.d_valid := io.iccm_d_valid
   fetchBusController.io.d_source := io.iccm_d_source
   fetchBusController.io.d_opcode := io.iccm_d_opcode
   fetchBusController.io.d_denied := io.iccm_d_denied
   fetchBusController.io.d_data := io.iccm_d_data
   fetchBusController.io.UART_DONE := io.UART_DONE

   io.iccm_a_valid := fetchBusController.io.a_valid
   io.iccm_a_source := fetchBusController.io.a_source
   io.iccm_a_opcode := fetchBusController.io.a_opcode
   io.iccm_a_data := fetchBusController.io.a_data
   io.iccm_a_address := fetchBusController.io.a_address
   staller.io.isUART := fetchBusController.io.stall
   staller.io.isMMIO := loadStoreBusController.io.stallForMMIO

   // Stall pin output from the core.
   io.isStalled := fetchBusController.io.stall


   // Initializing load store bus controller
   loadStoreBusController.io.isLoad := EX_MEM.io.ctrl_MemRd_out
   loadStoreBusController.io.isStore := EX_MEM.io.ctrl_MemWr_out
   loadStoreBusController.io.d_valid := io.dccm_d_valid
   loadStoreBusController.io.d_source := io.dccm_d_source
   loadStoreBusController.io.d_opcode := io.dccm_d_opcode
   loadStoreBusController.io.d_denied := io.dccm_d_denied
   loadStoreBusController.io.d_data := io.dccm_d_data
   // GPIO values being provided to store them in memory
   loadStoreBusController.io.GPIO_values := io.GPIO_values
   // The rs2 value that needs to be stored in the memory
   loadStoreBusController.io.rs2 := EX_MEM.io.rs2_out.asUInt
   // The alu output which tells the address for storing the data in memory.
   loadStoreBusController.io.addr := EX_MEM.io.alu_output(11,0).asUInt


    // Control signals for the GPIO controller
    io.setGPIO := loadStoreBusController.io.setGPIO
    io.readGPIO := loadStoreBusController.io.readGPIO
    io.gpioData := loadStoreBusController.io.setGPIOData




   io.dccm_a_address := loadStoreBusController.io.a_address
   io.dccm_a_data := loadStoreBusController.io.a_data
   io.dccm_a_opcode := loadStoreBusController.io.a_opcode
   io.dccm_a_source := loadStoreBusController.io.a_source
   io.dccm_a_valid := loadStoreBusController.io.a_valid


    // *********** ----------- INSTRUCTION FETCH (IF) STAGE ----------- ********* //
    fetch.io.stall := staller.io.stall
    fetch.io.inst_in := fetchBusController.io.inst
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

    IF_ID.io.stall := staller.io.stall
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
//    decode.io.dmem_memOut := io.dmem_data
    decode.io.dmem_memOut := loadStoreBusController.io.data.asSInt
    decode.io.stall := staller.io.stall

    ID_EX.io.stall := staller.io.stall
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
    ID_EX.io.func3_in := decode.io.func3_out
    ID_EX.io.func7_in := decode.io.func7_out
    ID_EX.io.rd_sel_in := decode.io.rd_sel_out
    ID_EX.io.rs1_sel_in := decode.io.rs1_sel_out
    ID_EX.io.rs2_sel_in := decode.io.rs2_sel_out



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

    EX_MEM.io.stall := staller.io.stall
    // Passing the ALU output to the EX/MEM pipeline register
    EX_MEM.io.alu_in := execute.io.alu_output

    // Passing the rd_sel value in the EX/MEM pipeline register
    EX_MEM.io.rd_sel_in := execute.io.rd_sel_out
    EX_MEM.io.rs2_sel_in := execute.io.rs2_sel_out
    EX_MEM.io.rs2_in := execute.io.rs2_out

    when(staller.io.stall =/= 1.U) {
      memWriteReg := execute.io.ctrl_MemWr_out
      memReadReg := execute.io.ctrl_MemRd_out
    }

    // Passing the control signals to EX/MEM pipeline register and (memRead / memWrite control registers for stall detection unit)
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

    //io.dmem_memWr := memory_stage.io.ctrl_MemWr_out
    //io.dmem_memRd := memory_stage.io.ctrl_MemRd_out
    //io.dmem_memAddr := memory_stage.io.memAddress
    //io.dmem_memData := memory_stage.io.rs2_out





    MEM_WB.io.stall := staller.io.stall
    MEM_WB.io.alu_in := memory_stage.io.alu_output
    //MEM_WB.io.dmem_data_in := io.dmem_data
    MEM_WB.io.dmem_data_in := loadStoreBusController.io.data.asSInt
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

    io.reg_7 := decode.io.reg_7_out

}
