package uart_testbench
import chisel3._
import peripherals.UartController

class Top extends Module{
  val io = IO(new Bundle {
    val rxd_in = Input(UInt(1.W))
    val ready = Input(Bool())
    val inst_out = Output(UInt(32.W))
  })

  val uartController = Module(new UartController(10000,3000))
  val iccm = Module(new Iccm)

  uartController.io.rxd := io.rxd_in
  uartController.io.ready := io.ready

  iccm.io.addr := uartController.io.addr
  iccm.io.en := uartController.io.en
  iccm.io.data_in := uartController.io.finalData

  io.inst_out := iccm.io.data_out
}
