package datapath
import chisel3._

class Staller extends Module{
  val io = IO(new Bundle {
    val isStall = Input(UInt(1.W))
    val stall = Output(UInt(1.W))
  })

  when(reset.asBool() === false.B && io.isStall === 1.U) {
    io.stall := 1.U
  } .otherwise {
    io.stall := 0.U
  }

}
