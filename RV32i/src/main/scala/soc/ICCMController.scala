package soc
import chisel3._
import merl.uit.tilelink.SlaveInterface

class FetchSlaveIO extends Bundle {
  val a_address = Input(UInt(32.W))
  val a_data = Input(UInt(32.W))
  val a_opcode = Input(UInt(3.W))
  val a_source = Input(UInt(32.W))
  val a_valid = Input(Bool())
  // The Channel D outputs from the ICCM Controller to the Bus Controller
  val d_opcode = Output(UInt(3.W))
  val d_source = Output(UInt(32.W))
  val d_denied = Output(Bool())
  val d_valid = Output(Bool())
  val d_data = Output(UInt(32.W))
}

class ICCMControllerIO extends Bundle {
  // The data returned from the calling module in case of a GET request.
  val ackDataFromModule = Input(UInt(32.W))
  val fetchSlaveIO = new FetchSlaveIO()
  // The ICCM controller outputs for the Instruction memory
  val addr_out = Output(UInt(32.W))
}

class ICCMController extends Module {
  val io = IO(new ICCMControllerIO)

  val si1 = Module(new SlaveInterface(forFetch = true))
  si1.io.ackDataFromModule := io.ackDataFromModule
  si1.io.a_address := io.fetchSlaveIO.a_address
  si1.io.a_data := io.fetchSlaveIO.a_data
  si1.io.a_opcode := io.fetchSlaveIO.a_opcode
  si1.io.a_source := io.fetchSlaveIO.a_source
  si1.io.a_valid := io.fetchSlaveIO.a_valid
  io.fetchSlaveIO.d_opcode := si1.io.d_opcode
  io.fetchSlaveIO.d_source := si1.io.d_source
  io.fetchSlaveIO.d_denied := si1.io.d_denied
  io.fetchSlaveIO.d_valid := si1.io.d_valid
  io.fetchSlaveIO.d_data := si1.io.d_data

  io.addr_out := si1.io.addr_out

}
