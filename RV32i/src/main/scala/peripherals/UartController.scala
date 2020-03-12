package peripherals
import chisel3._
import chisel3.util._

class UartController(frequency: Int, baudRate: Int) extends Module{
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val ready = Input(Bool())
    val data = Output(UInt(8.W))
    val finalData = Output(UInt(32.W))
    val addr = Output(UInt(10.W))
    val en = Output(Bool())
  })


  val count = RegInit(0.U(3.W))
  val regFinalData = RegInit(0.U(32.W))
  val regAddr = RegInit(1023.U(10.W))
  val regEn = RegInit(false.B)

  val rx = Module(new Rx(frequency, baudRate))
  rx.io.rxd := io.rxd
  rx.io.channel.ready := io.ready
  io.data := rx.io.channel.bits

  val dataReg = RegInit(0.U(8.W))
  val regLSB1 = RegInit(0.U(8.W))
  val regLSB2 = RegInit(0.U(8.W))
  val regMSB1 = RegInit(0.U(8.W))
  val regMSB2 = RegInit(0.U(8.W))

  when(rx.io.channel.valid === 1.U) {
    // We get 1 byte of data from the Rx module
    dataReg := rx.io.channel.bits
    count := count + 1.U
    regEn := false.B
  }

  switch(count) {
    is(1.U) {
      regLSB1 := dataReg
    }
    is(2.U) {
      regLSB2 := dataReg
    }
    is(3.U) {
      regMSB1 := dataReg
    }
    is(4.U) {
      // Now the 32-Bit data is complete here
      regFinalData := Cat(dataReg, regMSB1, regLSB2, regLSB1)
      regAddr := regAddr + 1.U
      regEn := true.B
    }
  }

  when(count === 4.U) {
    count := 0.U
  }



  io.addr := regAddr
  io.finalData := regFinalData
  io.en := regEn

}