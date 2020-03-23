package uart_testbench
import chisel3._

class Iccm extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(10.W))
    val en = Input(Bool())
    val data_in = Input(UInt(32.W))
    val data_out = Output(UInt(32.W))
  })

  val mem = Mem(1024, UInt(32.W))
  when(io.en) {
    mem.write(io.addr, io.data_in)
    io.data_out := 0.U
  } .otherwise {
    io.data_out := mem.read(io.addr)
  }

}
