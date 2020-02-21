package datapath
import chisel3._
import chisel3.util._
import merl.uit.tilelink.MasterInterface

class BusController extends Module {
  val io = IO(new Bundle {
    // The staller's isStalled output which tells that the core is stalled.
    val isStalled = Input(Bool())
    // The address of PC coming from Fetch
    val pcAddr = Input(UInt(32.W))
    // The data coming from the core
    val data_in = Input(UInt(32.W))
    // State control signals for the master interface coming from the core.
    val memRd = Input(UInt(1.W))
    val memWrt = Input(UInt(1.W))
    val uartEn = Input(UInt(1.W))
    // Channel D wires coming from the slave interface
    val d_opcode = Input(UInt(3.W))
    val d_source = Input(UInt(32.W))
    val d_denied = Input(Bool())
    val d_valid = Input(Bool())
    val d_data = Input(UInt(32.W))
    // Channel A outputs to Slave interface
    val a_address = Output(UInt(32.W))
    val a_data = Output(UInt(32.W))
    val a_opcode = Output(UInt(3.W))
    val a_source = Output(UInt(32.W))
    val a_valid = Output(Bool())
    // The output indicating that bus controller is done working and staller should not stall the core anymore
    val done = Output(UInt(1.W))
    // The data out containing the actual data from the bus controller in-case of a GET request.
    val data_out = Output(UInt(32.W))
  })
  val m3 = Module(new MasterInterface(sourceId = 3.U, forFetch = true))
  m3.io.addr_in := io.pcAddr
  m3.io.data_in := io.data_in
  m3.io.memRd := io.memRd
  m3.io.memWrt := io.memWrt
//  m3.io.uartEn := io.uartEn
  m3.io.d_opcode := io.d_opcode
  m3.io.d_data := io.d_data
  m3.io.d_source := io.d_source
  m3.io.d_denied := io.d_denied
  m3.io.d_valid := io.d_valid
  io.a_address := m3.io.a_address
  io.a_data := m3.io.a_data
  io.a_opcode := m3.io.a_opcode
  io.a_source := m3.io.a_source
  io.a_valid := m3.io.a_valid

  //val idle :: stall :: fetch :: Nil = Enum(3)
  //val stateReg = RegInit(idle)

  val counter = RegInit(0.U(5.W))

  //switch(stateReg) {
   // is(idle) {
//      when(io.isStalled) {
//      //  stateReg := stall
//      } .otherwise {
//        stateReg := fetch
//      }
//    }
    //is(stall) {
//      counter := counter + 1.U
//      when(counter === 31.U) {
//        counter := 0.U
//        io.done := 1.U
//        io.data_out := m3.io.data
//      //  stateReg := idle
//      } .otherwise {
//        io.done := 0.U
//        io.data_out := 0.U
//      }
  io.done := 0.U
  io.data_out := 0.U

  when(io.isStalled) {
    counter := counter + 1.U
    when(counter === 31.U) {
      counter := 0.U
      io.done := 1.U
      io.data_out := m3.io.data
    }.otherwise {
      io.done := 0.U
      io.data_out := 0.U
    }
  } .otherwise {
    //io.done := 1.U
    io.data_out := m3.io.data
  }

    //}

    //is(fetch) {
      //io.data_out := m3.io.data
      //stateReg := idle
    //}
  //}

  def initializeMasterInterface(mi: MasterInterface) = {
    mi.io.addr_in := io.pcAddr
    mi.io.data_in := io.data_in
    mi.io.memRd := io.memRd
    mi.io.memWrt := io.memWrt
   // mi.io.uartEn := io.uartEn
    mi.io.d_opcode := io.d_opcode
    mi.io.d_data := io.d_data
    mi.io.d_source := io.d_source
    mi.io.d_denied := io.d_denied
    mi.io.d_valid := io.d_valid
    io.a_address := mi.io.a_address
    io.a_data := mi.io.a_data
    io.a_opcode := mi.io.a_opcode
    io.a_source := mi.io.a_source
    io.a_valid := mi.io.a_valid
  }
}
