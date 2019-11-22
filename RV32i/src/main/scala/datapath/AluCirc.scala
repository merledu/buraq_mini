package datapath

import chisel3._


class AluCirc extends Module {
    val io = IO(new Bundle {
        val in_func3 = Input(UInt(3.W))
        val in_func7 = Input(UInt(1.W))
        val in_aluOp = Input(UInt(3.W))
        val in_oper_A = Input(UInt(32.W))
        val in_oper_B = Input(UInt(32.W))
        //val in_aluCtrl = Input(UInt(5.W))
        val output = Output(UInt(32.W))
    })

    val aluControl = Module(new AluControl())
    val alu = Module(new Alu())

    aluControl.io.aluOp := io.in_aluOp
    aluControl.io.func7 := io.in_func7
    aluControl.io.func3 := io.in_func3
    alu.io.aluCtrl := aluControl.io.output
    alu.io.oper_a := io.in_oper_A
    alu.io.oper_b := io.in_oper_B
    io.output := alu.io.output
}