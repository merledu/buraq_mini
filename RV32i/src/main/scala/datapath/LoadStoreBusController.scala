package datapath
import chisel3._
import merl.uit.tilelink.MasterInterface

class LoadStoreBusControllerIO extends Bundle {
  // The inputs coming from the memory stage
  val isLoad = Input(Bool())
  val isStore = Input(Bool())
  val rs2 = Input(UInt(32.W))
  val addr = Input(UInt(32.W))

  // Channel A properties going out to the slave interface inside DCCM Controller
  val a_address = Output(UInt(32.W))
  val a_data = Output(UInt(32.W))
  val a_opcode = Output(UInt(3.W))
  val a_source = Output(UInt(32.W))
  val a_valid = Output(Bool())

  // Channel D properties coming in from the slave interface inside DCCM Controller
  val d_opcode = Input(UInt(3.W))
  val d_source = Input(UInt(32.W))
  val d_denied = Input(Bool())
  val d_valid = Input(Bool())
  val d_data = Input(UInt(32.W))

  // The resultant output of the module. Ack contains either the data for a Load instruction or just 1
  // which serves as an acknowledgement in case of a Store instruction
  val data = Output(UInt(32.W))
}

class LoadStoreBusController extends Module {
  val io = IO(new LoadStoreBusControllerIO)

  val miDccmSlave = Module(new MasterInterface(sourceId = 2.U, forFetch = false))
  miDccmSlave.io.memRd := io.isLoad
  miDccmSlave.io.memWrt := io.isStore
  miDccmSlave.io.addr_in := io.addr
  miDccmSlave.io.data_in := io.rs2

  io.a_address := miDccmSlave.io.a_address
  io.a_data := miDccmSlave.io.a_data
  io.a_opcode := miDccmSlave.io.a_opcode
  io.a_source := miDccmSlave.io.a_source
  io.a_valid := miDccmSlave.io.a_valid

  miDccmSlave.io.d_valid := io.d_valid
  miDccmSlave.io.d_source := io.d_source
  miDccmSlave.io.d_opcode := io.d_opcode
  miDccmSlave.io.d_denied := io.d_denied
  miDccmSlave.io.d_data := io.d_data

  io.data := miDccmSlave.io.data


}
