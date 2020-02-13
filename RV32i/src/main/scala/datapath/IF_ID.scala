package datapath
import chisel3._

class IF_ID extends Module {
    val io = IO(new Bundle {
        val pc_in = Input(SInt(32.W))
        val pc4_in = Input(SInt(32.W))
        val inst_in = Input(UInt(32.W))
        val stall = Input(UInt(1.W))
        val pc_out = Output(SInt(32.W))
        val pc4_out = Output(SInt(32.W))
        val inst_out = Output(UInt(32.W))
    })

    val pc_reg = RegInit(0.S(32.W))
    val pc4_reg = RegInit(0.S(32.W))
    val inst_reg = RegInit(0.U(32.W))
    when(io.stall =/= 1.U) {
        pc_reg := io.pc_in
        pc4_reg := io.pc4_in
        inst_reg := io.inst_in
        io.pc_out := pc_reg
        io.pc4_out := pc4_reg
        io.inst_out := inst_reg
    } .otherwise {
//        pc_reg := pc_reg
//        pc4_reg := pc4_reg
//        inst_reg := inst_reg
        io.pc_out := pc_reg
        io.pc4_out := pc4_reg
        io.inst_out := inst_reg
    }

}
