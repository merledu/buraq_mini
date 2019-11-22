package datapath

import chisel3._
import chisel3.util.Cat

class AluControl extends Module {
    val io = IO(new Bundle {
        val aluOp = Input(UInt(3.W))
        val func7 = Input(UInt(1.W))
        val func3 = Input(UInt(3.W))
        val output = Output(UInt(5.W))        
    })
    
    when(io.aluOp === "b000".U){
        // R-Type Instruction
        io.output := Cat("b0".U, io.func7, io.func3)
    } .elsewhen(io.aluOp === "b100".U) {
        // Load Type Instruction
        io.output := "b00000".U
    } .elsewhen(io.aluOp === "b001".U) {
        // I-Type Instruction
        when(io.func3 === "b101".U) {
            io.output := Cat("b0".U, io.func7, io.func3)
        } .otherwise {
            io.output := Cat("b0".U, "b0".U, io.func3)
        }
        //
    } .elsewhen(io.aluOp === "b101".U) {
        // S-Type Instruction
        io.output := "b00000".U
    } .elsewhen(io.aluOp === "b010".U) {
        // SB-Type Instruction
        io.output := Cat("b10".U, io.func3)
    } .elsewhen(io.aluOp === "b011".U) {
        // JALR instruction // JAL instruction
        io.output := "b11111".U
    } .elsewhen(io.aluOp === "b110".U) {
        // LUI type instruction
        io.output := "b00000".U
    } .otherwise {
        io.output := DontCare
    }

}