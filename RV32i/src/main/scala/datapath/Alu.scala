package datapath

import chisel3._
import chisel3.util.Cat

class Alu extends Module {
    val io = IO(new Bundle {
        val oper_a = Input(SInt(32.W))
        val oper_b = Input(SInt(32.W))
        val aluCtrl = Input(UInt(5.W))
        val output = Output(SInt(32.W))
        val branch = Output(UInt(1.W))
    })
    
    when(io.aluCtrl === "b00000".U) {
        // ADD
        io.output := io.oper_a + io.oper_b
    } .elsewhen(io.aluCtrl === "b00001".U) {
        // SLL/SLLI     // works for both signed and unsigned numbers
        val shift_left_by = io.oper_b(4,0)
        io.output := io.oper_a << shift_left_by
    } .elsewhen(io.aluCtrl === "b00010".U) {
        // SLT/SLTI
        when(io.oper_a < io.oper_b) {
            io.output := 1.S
        } .otherwise {
            io.output := 0.S
        } 
    } .elsewhen(io.aluCtrl === "b00011".U || io.aluCtrl === "b10110".U) {
        // SLTU/SLTUI   BLTU
        when(io.oper_a.asUInt < io.oper_b.asUInt) {
            io.output := 1.S
        } .otherwise {
            io.output := 0.S
        }
    } .elsewhen(io.aluCtrl === "b00100".U) {
        // XOR/XORI
        io.output := io.oper_a ^ io.oper_b
    } .elsewhen(io.aluCtrl === "b00101".U || io.aluCtrl === "b01101".U) {
        // Since SRL/SRLI deals shifting unsigned numbers right and SRA/SRAI deals with shifting
        // signed numbers right, chisel has the same operator of doing right shift for
        // both the signed and unsigned numbers. So we combined the alu control signals
        // of both the instructions here and performed the right shift.

        // SRL/SRLI SRA/SRAI
        val shift_right_by = io.oper_b(4,0)
        io.output := io.oper_a >> shift_right_by
    } .elsewhen(io.aluCtrl === "b00110".U) {
        // OR/ORI
        io.output := io.oper_a | io.oper_b
    } .elsewhen(io.aluCtrl === "b00111".U) {
        // AND/ANDI
        io.output := io.oper_a & io.oper_b
    } .elsewhen(io.aluCtrl === "b01000".U) {
        // SUB
        io.output := io.oper_a - io.oper_b
    
    } .elsewhen(io.aluCtrl === "b10000".U) {
        // BEQ
        when(io.oper_a === io.oper_b) {
            io.output := 1.S
        } .otherwise {
            io.output := 0.S
        }
    } .elsewhen(io.aluCtrl === "b10001".U) {
        // BNE
        when(~(io.oper_a === io.oper_b)) {
            io.output := 1.S
        } .otherwise {
            io.output := 0.S
        }
    } .elsewhen(io.aluCtrl === "b10100".U) {
        // BLT
        when(io.oper_a < io.oper_b) {
            io.output := 1.S
        } .otherwise {
            io.output := 0.S
        }
    } .elsewhen(io.aluCtrl === "b10101".U) {
        // BGE
        when(io.oper_a >= io.oper_b) {
            io.output := 1.S
        } .otherwise {
            io.output := 0.S
        }
    } .elsewhen(io.aluCtrl === "b10111".U) {
        // BGEU
        when(io.oper_a.asUInt >= io.oper_b.asUInt) {
            io.output := 1.S
        } .otherwise {
            io.output := 0.S
        }
    } .elsewhen(io.aluCtrl === "b11111".U) {
        // JALR/JAL
        io.output := io.oper_a
    }
    
    .otherwise {
        io.output := DontCare
    }

    when(io.aluCtrl(4,3) === "b10".U && io.output === 1.S) {
        io.branch := 1.U
    } .otherwise {
        io.branch := 0.U
    }

}