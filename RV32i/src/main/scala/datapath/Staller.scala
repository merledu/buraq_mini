package datapath
import chisel3._

class Staller extends Module{
  val io = IO(new Bundle {
    val isStall = Input(UInt(1.W))
    val ack = Input(Bool())
    val stall = Output(UInt(1.W))
  })

  when((io.isStall === 1.U) && !io.ack) {
    io.stall := 1.U
  } .otherwise {
    io.stall := 0.U
  }

}
