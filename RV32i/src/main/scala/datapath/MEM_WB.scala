package datapath
import chisel3._

class MEM_WB extends Module {
    val io = IO(new Bundle {
        val EX_MEM_REGWR = Input(UInt(1.W))
        val EX_MEM_MEMTOREG = Input(UInt(1.W))
        val EX_MEM_RDSEL = Input(UInt(5.W))
        val EX_MEM_MEMRD = Input(UInt(1.W))
        val in_dataMem_data = Input(SInt(32.W))
        val in_alu_output = Input(SInt(32.W))

        val mem_wb_regWr_output = Output(UInt(1.W))
        val mem_wb_memToReg_output = Output(UInt(1.W))
        val mem_wb_memRd_output = Output(UInt(1.W))
        val mem_wb_rdSel_output = Output(UInt(5.W))
        val mem_wb_dataMem_data = Output(SInt(32.W))
        val mem_wb_alu_output = Output(SInt(32.W))
    })

    val reg_regWr = RegInit(0.U(1.W))
    reg_regWr := io.EX_MEM_REGWR
    io.mem_wb_regWr_output := reg_regWr

    val reg_memToReg = RegInit(0.U(1.W))
    reg_memToReg := io.EX_MEM_MEMTOREG
    io.mem_wb_memToReg_output := reg_memToReg

    val reg_memRd = RegInit(0.U(1.W))
    reg_memRd := io.EX_MEM_MEMRD
    io.mem_wb_memRd_output := reg_memRd

    val reg_rdSel = RegInit(0.U(5.W))
    reg_rdSel := io.EX_MEM_RDSEL
    io.mem_wb_rdSel_output := reg_rdSel

    val reg_dataMem_data = RegInit(0.S(32.W))
    reg_dataMem_data := io.in_dataMem_data
    io.mem_wb_dataMem_data := reg_dataMem_data

    val reg_alu_output = RegInit(0.S(32.W))
    reg_alu_output := io.in_alu_output
    io.mem_wb_alu_output := reg_alu_output

}
