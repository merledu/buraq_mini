package datapath
import chisel3._

class ControlDecode extends Module {
    val io = IO(new Bundle {
        // Inputs
        val in_r_type = Input(UInt(1.W))
        val in_load_type = Input(UInt(1.W))
        val in_s_type = Input(UInt(1.W))
        val in_sb_type = Input(UInt(1.W))
        val in_i_type = Input(UInt(1.W))
        val in_jalr_type = Input(UInt(1.W))
        val in_jal_type = Input(UInt(1.W))
        val in_lui_type = Input(UInt(1.W))
        // Outputs
        val memWrite = Output(UInt(1.W))
        val branch = Output(UInt(1.W))
        val memRead = Output(UInt(1.W))
        val regWrite = Output(UInt(1.W))
        val memToReg = Output(UInt(1.W))
        val aluOperation = Output(UInt(3.W))
        val operand_a_sel = Output(UInt(2.W))
        val operand_b_sel = Output(UInt(1.W))
        val extend_sel = Output(UInt(2.W))
        val next_pc_sel = Output(UInt(2.W))
    })
    // R-Type instruction
    when(io.in_r_type === 1.U) {
        io.memWrite := 0.U
        io.branch := 0.U
        io.memRead := 0.U
        io.regWrite := 1.U
        io.memToReg := 0.U
        io.aluOperation := "b000".U
        io.operand_a_sel := "b00".U
        io.operand_b_sel := 0.U
        io.extend_sel := "b00".U
        io.next_pc_sel := "b00".U
    } .elsewhen(io.in_load_type === 1.U) {
        // Load type instruction
        io.memWrite := 0.U
        io.branch := 0.U
        io.memRead := 1.U
        io.regWrite := 1.U
        io.memToReg := 1.U
        io.aluOperation := "b100".U
        io.operand_a_sel := "b00".U
        io.operand_b_sel := 1.U
        io.extend_sel := "b00".U
        io.next_pc_sel := "b00".U
    } .elsewhen(io.in_s_type === 1.U) {
        // S-Type instruction
        io.memWrite := 1.U
        io.branch := 0.U
        io.memRead := 0.U
        io.regWrite := 0.U
        io.memToReg := 0.U
        io.aluOperation := "b101".U
        io.operand_a_sel := "b00".U
        io.operand_b_sel := 1.U
        io.extend_sel := "b01".U
        io.next_pc_sel := "b00".U
    } .elsewhen(io.in_sb_type === 1.U) {
        io.memWrite := 0.U
        io.branch := 1.U
        io.memRead := 0.U
        io.regWrite := 0.U
        io.memToReg := 0.U
        io.aluOperation := "b010".U
        io.operand_a_sel := "b00".U
        io.operand_b_sel := 0.U
        io.extend_sel := "b00".U
        io.next_pc_sel := "b01".U
    } .elsewhen(io.in_i_type === 1.U) {
        io.memWrite := 0.U
        io.branch := 0.U
        io.memRead := 0.U
        io.regWrite := 1.U
        io.memToReg := 0.U
        io.aluOperation := "b001".U
        io.operand_a_sel := "b00".U
        io.operand_b_sel := 1.U
        io.extend_sel := "b00".U
        io.next_pc_sel := "b00".U
    } .elsewhen(io.in_jalr_type === 1.U) {
        io.memWrite := 0.U
        io.branch := 0.U
        io.memRead := 0.U
        io.regWrite := 1.U
        io.memToReg := 0.U
        io.aluOperation := "b011".U
        io.operand_a_sel := "b10".U
        io.operand_b_sel := 0.U
        io.extend_sel := "b00".U
        io.next_pc_sel := "b11".U
    } .elsewhen(io.in_jal_type === 1.U) {
        io.memWrite := 0.U
        io.branch := 0.U
        io.memRead := 0.U
        io.regWrite := 1.U
        io.memToReg := 0.U
        io.aluOperation := "b011".U
        io.operand_a_sel := "b10".U
        io.operand_b_sel := 0.U
        io.extend_sel := "b00".U
        io.next_pc_sel := "b10".U
    } .elsewhen(io.in_lui_type === 1.U) {
        io.memWrite := 0.U
        io.branch := 0.U
        io.memRead := 0.U
        io.regWrite := 1.U
        io.memToReg := 0.U
        io.aluOperation := "b110".U
        io.operand_a_sel := "b11".U
        io.operand_b_sel := 1.U
        io.extend_sel := "b10".U
        io.next_pc_sel := "b00".U
    } .otherwise {
        io.memWrite := 0.U
        io.branch := 0.U
        io.memRead := 0.U
        io.regWrite := 0.U
        io.memToReg := 0.U
        io.aluOperation := "b111".U
        io.operand_a_sel := "b00".U
        io.operand_b_sel := 0.U
        io.extend_sel := "b00".U
        io.next_pc_sel := "b00".U
    }
}