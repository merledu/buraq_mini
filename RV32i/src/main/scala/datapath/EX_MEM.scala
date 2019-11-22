package datapath
import chisel3._

class EX_MEM extends Module {
    val io = IO(new Bundle {
        val ID_EX_MEMWR = Input(UInt(1.W))
        val ID_EX_MEMRD = Input(UInt(1.W))
        val ID_EX_REGWR = Input(UInt(1.W))
        val ID_EX_MEMTOREG = Input(UInt(1.W))
        val ID_EX_RS2 = Input(SInt(32.W))
        val ID_EX_RDSEL = Input(UInt(5.W))
        val ID_EX_RS2SEL = Input(UInt(5.W))

        val alu_output = Input(SInt(32.W))

        val ex_mem_memWr_out = Output(UInt(1.W))
        val ex_mem_memRd_out = Output(UInt(1.W))
        val ex_mem_regWr_out = Output(UInt(1.W))
        val ex_mem_memToReg_out = Output(UInt(1.W))
        val ex_mem_rs2_output = Output(SInt(32.W))
        val ex_mem_rdSel_output = Output(UInt(5.W))
        val ex_mem_rs2Sel_output = Output(UInt(5.W))
        val ex_mem_alu_output = Output(SInt(32.W))

    })

        val reg_memWr = RegInit(0.U(1.W))
        reg_memWr := io.ID_EX_MEMWR
        io.ex_mem_memWr_out := reg_memWr

        val reg_memRd = RegInit(0.U(1.W))
        reg_memRd := io.ID_EX_MEMRD
        io.ex_mem_memRd_out := reg_memRd

        val reg_regWr = RegInit(0.U(1.W))
        reg_regWr := io.ID_EX_REGWR
        io.ex_mem_regWr_out := reg_regWr

        val reg_memToReg = RegInit(0.U(1.W))
        reg_memToReg := io.ID_EX_MEMTOREG
        io.ex_mem_memToReg_out := reg_memToReg

        val reg_rs2 = RegInit(0.S(32.W))
        reg_rs2 := io.ID_EX_RS2
        io.ex_mem_rs2_output := reg_rs2

        val reg_rd_sel = RegInit(0.U(5.W))
        reg_rd_sel := io.ID_EX_RDSEL
        io.ex_mem_rdSel_output := reg_rd_sel

        val reg_rs2_sel = RegInit(0.U(5.W))
        reg_rs2_sel := io.ID_EX_RS2SEL
        io.ex_mem_rs2Sel_output := reg_rs2_sel

        val reg_alu_output = RegInit(0.S(32.W))
        reg_alu_output := io.alu_output
        io.ex_mem_alu_output := reg_alu_output


}
