package core

import chisel3._

class Pc extends Module {
    val io = IO(new Bundle {
        val in = Input(SInt(32.W))
        val instr_rvalid_i = Input(Bool())
        val out = Output(SInt(32.W))
        val pc4 = Output(SInt(32.W))
    })

    val reg = RegInit(0.S(32.W))
        reg := io.in

    when(io.instr_rvalid_i)
    {
      io.pc4 := reg + 4.S
    }   
    .otherwise
    {
      io.pc4 := reg

    } 
    io.out := reg
    
}