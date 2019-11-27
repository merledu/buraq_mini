package datapath
import chisel3._

class EX_MEM extends Module {
    val io = IO(new Bundle {
        val ctrl_MemWr_in = Input(UInt(1.W))
        val ctrl_MemRd_in = Input(UInt(1.W))
        val ctrl_RegWr_in = Input(UInt(1.W))
        val ctrl_MemToReg_in = Input(UInt(1.W))
        val rs2_in = Input(SInt(32.W))
        val rd_sel_in = Input(UInt(5.W))
        val rs2_sel_in = Input(UInt(5.W))

        val alu_in = Input(SInt(32.W))

        val ctrl_MemWr_out = Output(UInt(1.W))
        val ctrl_MemRd_out = Output(UInt(1.W))
        val ctrl_RegWr_out = Output(UInt(1.W))
        val ctrl_MemToReg_out = Output(UInt(1.W))
        val rs2_out = Output(SInt(32.W))
        val rd_sel_out = Output(UInt(5.W))
        val rs2_sel_out = Output(UInt(5.W))
        val alu_output = Output(SInt(32.W))

    })

        val reg_memWr = RegInit(0.U(1.W))
        reg_memWr := io.ctrl_MemWr_in
        io.ctrl_MemWr_out := reg_memWr

        val reg_memRd = RegInit(0.U(1.W))
        reg_memRd := io.ctrl_MemRd_in
        io.ctrl_MemRd_out := reg_memRd

        val reg_regWr = RegInit(0.U(1.W))
        reg_regWr := io.ctrl_RegWr_in
        io.ctrl_RegWr_out := reg_regWr

        val reg_memToReg = RegInit(0.U(1.W))
        reg_memToReg := io.ctrl_MemToReg_in
        io.ctrl_MemToReg_out := reg_memToReg

        val reg_rs2 = RegInit(0.S(32.W))
        reg_rs2 := io.rs2_in
        io.rs2_out := reg_rs2

        val reg_rd_sel = RegInit(0.U(5.W))
        reg_rd_sel := io.rd_sel_in
        io.rd_sel_out := reg_rd_sel

        val reg_rs2_sel = RegInit(0.U(5.W))
        reg_rs2_sel := io.rs2_sel_in
        io.rs2_sel_out := reg_rs2_sel

        val reg_alu_output = RegInit(0.S(32.W))
        reg_alu_output := io.alu_in
        io.alu_output := reg_alu_output


}
