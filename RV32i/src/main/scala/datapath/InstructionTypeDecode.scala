package datapath

import chisel3._

class InstructionTypeDecode extends Module {
    val io = IO(new Bundle {
        val opcode = Input(UInt(7.W))
        val r_type = Output(UInt(1.W))
        val load_type = Output(UInt(1.W))
        val s_type = Output(UInt(1.W))
        val sb_type = Output(UInt(1.W))
        val i_type = Output(UInt(1.W))
        val jalr_type = Output(UInt(1.W))
        val jal_type = Output(UInt(1.W))
        val lui_type = Output(UInt(1.W))
    })
        io.r_type := 0.U
        io.load_type := 0.U
        io.s_type := 0.U
        io.sb_type := 0.U
        io.i_type := 0.U
        io.jalr_type := 0.U
        io.jal_type := 0.U
        io.lui_type := 0.U

    when(io.opcode === "b0110011".U) {
        io.r_type := 1.U
    } .elsewhen(io.opcode === "b0000011".U) {
        io.load_type := 1.U
    } .elsewhen(io.opcode === "b0100011".U) {        
        io.s_type := 1.U        
    } .elsewhen(io.opcode === "b1100011".U) {        
        io.sb_type := 1.U        
    } .elsewhen(io.opcode === "b0010011".U) {        
        io.i_type := 1.U        
    } .elsewhen(io.opcode === "b1100111".U) {
        io.jalr_type := 1.U
    } .elsewhen(io.opcode === "b1101111".U) {
        io.jal_type := 1.U
    } .elsewhen(io.opcode === "b0110111".U) {
        io.lui_type := 1.U
    } .otherwise {
        io.r_type := 0.U
        io.load_type := 0.U
        io.s_type := 0.U
        io.sb_type := 0.U
        io.i_type := 0.U
        io.jalr_type := 0.U
        io.jal_type := 0.U
        io.lui_type := 0.U
    }

}