package datapath
import chisel3._
import chisel3.util._
class BusController extends Module {
  val io = IO(new Bundle {
    val isStalled = Input(Bool())
    val done = Output(UInt(1.W))
  })
  val idle :: stall :: Nil = Enum(2)
  val stateReg = RegInit(idle)

  val counter = RegInit(0.U(5.W))
  io.done := 0.U

  switch(stateReg) {
    is(idle) {
      when(io.isStalled) {
        stateReg := stall
      }
    }
    is(stall) {
      counter := counter + 1.U
      when(counter === 31.U) {
        counter := 0.U
        io.done := 1.U
        stateReg := idle
      }

    }
  }
}
