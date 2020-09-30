package core

import chisel3._

class MEM_WB extends Module {
    val io = IO(new Bundle {
        val ctrl_RegWr_in = Input(UInt(1.W))
        val ctrl_MemToReg_in = Input(UInt(1.W))
        val rd_sel_in = Input(UInt(5.W))
        val ctrl_MemRd_in = Input(UInt(1.W))
        //val dmem_data_in = Input(SInt(32.W))
        val alu_in = Input(SInt(32.W))

       // val stall = Input(UInt(1.W))

        val ctrl_RegWr_out = Output(UInt(1.W))
        val ctrl_MemToReg_out = Output(UInt(1.W))
        val ctrl_MemRd_out = Output(UInt(1.W))
        val rd_sel_out = Output(UInt(5.W))
        //val dmem_data_out = Output(SInt(32.W))
        val alu_output = Output(SInt(32.W))
    })

    val reg_regWr = RegInit(0.U(1.W))

    val reg_memToReg = RegInit(0.U(1.W))

    val reg_memRd = RegInit(0.U(1.W))

    val reg_rdSel = RegInit(0.U(5.W))

    //val reg_dataMem_data = RegInit(0.S(32.W))

    val reg_alu_output = RegInit(0.S(32.W))

  //  when(io.stall =/= 1.U) {
        reg_regWr := io.ctrl_RegWr_in
        reg_memToReg := io.ctrl_MemToReg_in
        reg_memRd := io.ctrl_MemRd_in
        reg_rdSel := io.rd_sel_in
      //  reg_dataMem_data := io.dmem_data_in
        reg_alu_output := io.alu_in

        io.ctrl_MemToReg_out := reg_memToReg
        io.ctrl_RegWr_out := reg_regWr
        io.ctrl_MemRd_out := reg_memRd
        io.rd_sel_out := reg_rdSel
        //io.dmem_data_out := reg_dataMem_data
        io.alu_output := reg_alu_output
/*    } .otherwise {
        io.ctrl_MemToReg_out := reg_memToReg
        io.ctrl_RegWr_out := reg_regWr
        io.ctrl_MemRd_out := reg_memRd
        io.rd_sel_out := reg_rdSel
        io.dmem_data_out := reg_dataMem_data
        io.alu_output := reg_alu_output
    } */


}
