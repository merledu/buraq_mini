package datapath
import chisel3._

class Staller extends Module{
  val io = IO(new Bundle {
    val isLoad = Input(Bool())
    val isStore = Input(Bool())
    val stall = Output(UInt(1.W))
  })

  when(io.isLoad || io.isStore) {
    io.stall := 1.U
  } .otherwise {
    io.stall := 0.U
  }

}
