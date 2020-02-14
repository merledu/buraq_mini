package datapath
import chisel3._

class StallDetection extends Module {
  val io = IO(new Bundle {
    val memRead = Input(UInt(1.W))
    val memWrite = Input(UInt(1.W))
    val isStall = Output(UInt(1.W))
  })

  when(io.memRead === 1.U || io.memWrite === 1.U) {
    io.isStall := 1.U
  } .otherwise {
    io.isStall := 0.U
  }

}
