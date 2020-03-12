package soc

import chisel3._
import merl.uit.tilelink.SlaveInterface

class ICCMControllerIO extends Bundle {
  // The data returned from the calling module in case of a GET request.
  val ackDataFromModule = Input(UInt(32.W))
  // The Channel A inputs from FetchBus Controller
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
  // The ICCM controller outputs for the Instruction memory
  val addr_out = Output(UInt(32.W))
}

class ICCMController extends Module {
  val io = IO(new ICCMControllerIO)

  val si = Module(new SlaveInterface(forFetch = true))
  si.io.ackDataFromModule := io.ackDataFromModule
  si.io.a_address := io.a_address
  si.io.a_data := io.a_data
  si.io.a_opcode := io.a_opcode
  si.io.a_source := io.a_source
  si.io.a_valid := io.a_valid
  io.d_opcode := si.io.d_opcode
  io.d_source := si.io.d_source
  io.d_denied := si.io.d_denied
  io.d_valid := si.io.d_valid
  io.d_data := si.io.d_data

  io.addr_out := si.io.addr_out

}
