package peripherals
import chisel3._
import chisel3.util._
import merl.uit.tilelink.MasterInterface

class MasterInterfaceIO extends Bundle {
  // Channel A
  val a_address = Output(UInt(32.W))
  val a_data = Output(UInt(32.W))
  val a_opcode = Output(UInt(3.W))
  val a_source = Output(UInt(32.W))
  val a_valid = Output(Bool())
  // Channel D
  val d_opcode = Input(UInt(3.W))
  val d_source = Input(UInt(32.W))
  val d_denied = Input(Bool())
  val d_valid = Input(Bool())
  val d_data = Input(UInt(32.W))
}

class UartController(frequency: Int, baudRate: Int) extends Module{
  val io = IO(new Bundle {
    /**
     * isStalled input is coming in from the FetchBusController telling whether the core is stalled or not.
     * When this signal is true it means the core is stalled and we need to start the UART communciation. */
    val isStalled = Input(Bool())

    val masterInterfaceIO = new MasterInterfaceIO()

    val rxd = Input(UInt(1.W))
    val ready = Input(Bool())

//    val finalData = Output(UInt(32.W))
//    val addr = Output(UInt(10.W))
    val en = Output(Bool())

    /**
     * done is the control signal which is attached with FetchBusController.
     * It tells the FetchBusController that the UART is done writing to memory
     * On the basis of this signal the FetchBusController will remove the stall.*/
    val done = Output(Bool())
  })

  val regDone = RegInit(false.B)
  val count = RegInit(0.U(3.W))
  val regFinalData = RegInit(0.U(32.W))
  val regAddr = RegInit(1023.U(10.W))
  val regEn = RegInit(false.B)

  val rx = Module(new Rx(frequency, baudRate))
  rx.io.rxd := io.rxd
  rx.io.channel.ready := io.ready

  val dataReg = RegInit(0.U(8.W))
  val regLSB1 = RegInit(0.U(8.W))
  val regLSB2 = RegInit(0.U(8.W))
  val regMSB1 = RegInit(0.U(8.W))
  val regMSB2 = RegInit(0.U(8.W))

  when(io.isStalled && !regDone) {
    when(rx.io.channel.valid === 1.U) {
      // We get 1 byte of data from the Rx module
      dataReg := rx.io.channel.bits
      count := count + 1.U
      regEn := false.B
    }
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
      val data = Cat(dataReg, regMSB1, regLSB2, regLSB1)
      when(data === "h00000fff".U) {
        regDone := true.B
        regFinalData := 0.U
        regAddr := 0.U
        regEn := false.B
      } .otherwise {
        regFinalData := data
        regAddr := regAddr + 1.U
        regEn := true.B
      }

    }
  }

  when(count === 4.U) {
    count := 0.U
  }


  val mi = Module(new MasterInterface(sourceId = 3.U, forFetch = false))
  mi.io.memRd := 0.U
  mi.io.memWrt := 1.U
  mi.io.addr_in := regAddr
  mi.io.data_in := regFinalData

  io.masterInterfaceIO.a_address := mi.io.a_address
  io.masterInterfaceIO.a_data := mi.io.a_data
  io.masterInterfaceIO.a_opcode := mi.io.a_opcode
  io.masterInterfaceIO.a_source := mi.io.a_source
  io.masterInterfaceIO.a_valid := mi.io.a_valid

  mi.io.d_data := io.masterInterfaceIO.d_data
  mi.io.d_denied := io.masterInterfaceIO.d_denied
  mi.io.d_opcode := io.masterInterfaceIO.d_opcode
  mi.io.d_valid := io.masterInterfaceIO.d_valid
  mi.io.d_source := io.masterInterfaceIO.d_source




//  io.addr := regAddr
//  io.finalData := regFinalData
  io.en := regEn
  io.done := regDone

}