package buraq_mini.core

import chisel3._

class Pc extends Module {
    val io = IO(new Bundle {
        val in = Input(SInt(32.W))
        val out = Output(SInt(32.W))
        val pc4 = Output(SInt(32.W))
        val halt = Input(Bool())
    })

    val reg = RegInit((CoreParams.reset_vector - 0x4).asSInt(32.W))
    reg := io.in
    io.pc4 := Mux(io.halt, reg, reg + 4.S)
    io.out := reg
//    when(io.instr_gnt_i)
//    {
//      io.pc4 := reg + 4.S
//    }
//    .otherwise
//    {
//      io.pc4 := reg
//
//    }

    
}