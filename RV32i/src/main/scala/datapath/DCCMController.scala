package datapath
import chisel3._
import merl.uit.tilelink.SlaveInterface

class DCCMControllerIO extends Bundle {
  // The data coming in from the DCCM memory in case of a Load instruction
  val data_in = Input(UInt(32.W))
  // The Channel A inputs from LoadStore BusController
  val a_address = Input(UInt(32.W))
  val a_data = Input(UInt(32.W))
  val a_opcode = Input(UInt(3.W))
  val a_source = Input(UInt(32.W))
  val a_valid = Input(Bool())
  // The Channel D outputs from the DCCM Controller to the LoadStore BusController
  val d_opcode = Output(UInt(3.W))
  val d_source = Output(UInt(32.W))
  val d_denied = Output(Bool())
  val d_valid = Output(Bool())
  val d_data = Output(UInt(32.W))

  val en = Output(UInt(1.W))
  val addr_out = Output(UInt(32.W))
  val data_out = Output(UInt(32.W))

}

class DCCMController extends Module {
  val io = IO(new DCCMControllerIO)

  val si = Module(new SlaveInterface(forFetch = false))
  si.io.a_address := io.a_address
  si.io.a_data := io.a_data
  si.io.a_opcode := io.a_opcode
  si.io.a_source := io.a_source
  si.io.a_valid := io.a_valid

  io.en := si.io.en
  io.addr_out := si.io.addr_out
  io.data_out := si.io.data_out

  si.io.ackDataFromModule := io.data_in

  io.d_opcode := si.io.d_opcode
  io.d_source := si.io.d_source
  io.d_denied := si.io.d_denied
  io.d_valid := si.io.d_valid
  io.d_data := si.io.d_data
}
