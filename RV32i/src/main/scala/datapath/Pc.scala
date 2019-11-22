package datapath
import chisel3._

class Pc extends Module {
    val io = IO(new Bundle {
        val in = Input(SInt(32.W))
        val out = Output(SInt(32.W))
        val pc4 = Output(SInt(32.W))
    })

    val reg = RegInit(0.S(32.W))
    reg := io.in
    io.out := reg
    io.pc4 := reg + 4.S
}