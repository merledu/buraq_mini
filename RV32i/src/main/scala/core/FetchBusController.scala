package core

import chisel3._
//import merl.uit.tilelink.MasterInterface

class FetchBusControllerIO extends Bundle {
  // PC input coming in from the fetch module in core.
  val pcAddr = Input(UInt(32.W))

  /** signal coming in from the UART Controller telling whether UART is
   * done writing the instruction memory. If done then the fetch bus
   * controller must remove the stall from the core, read the instruction
   * and send it to the pipeline*
   * Initially UART_DONE    -->   false
   * After updating memory  -->   true
   **/
  val UART_DONE = Input(Bool())
  // Channel D properties coming in from the slave interface inside ICCM Controller
  val d_opcode = Input(UInt(3.W))
  val d_source = Input(UInt(32.W))
  val d_denied = Input(Bool())
  val d_valid = Input(Bool())
  val d_data = Input(UInt(32.W))
  // Channel A properties going out to the slave interface inside ICCM Controller
  val a_address = Output(UInt(32.W))
  val a_data = Output(UInt(32.W))
  val a_opcode = Output(UInt(3.W))
  val a_source = Output(UInt(32.W))
  val a_valid = Output(Bool())
  /**
   * Stall pin going to the staller module indicating the staller to stall the core.
   * This pin is also connected with the UART controller telling that it can now start to receive items from Rx.
   */
  val stall = Output(UInt(1.W))
  // Instruction output going to the Fetch module
  val inst = Output(UInt(32.W))

}

class FetchBusController extends Module {
  val io = IO(new FetchBusControllerIO)

  // Instantiating the master interface whose parent will be FetchBusController
  val miIccmSlave = Module(new MasterInterface(sourceId = 1.U, forFetch = true))
  // setting the memRd and memWrt properties to be 0 which are not needed for fetch
  miIccmSlave.io.memRd := 0.U
  miIccmSlave.io.memWrt := 0.U
  // address that is provided to the master interface for querying the memory
  miIccmSlave.io.addr_in := io.pcAddr
  // data is set to 0 since we don't need any data for fetch purposes
  miIccmSlave.io.data_in := 0.U
  // attaching the channel D wires coming from ICCMController's slave interface
  // to the master interface inside this parent module
  miIccmSlave.io.d_data := io.d_data
  miIccmSlave.io.d_denied := io.d_denied
  miIccmSlave.io.d_opcode := io.d_opcode
  miIccmSlave.io.d_source := io.d_source
  miIccmSlave.io.d_valid := io.d_valid
  // sending the channel A values of the master interface to the output of this parent module
  // to be used by the slave interface of ICCMController
  io.a_address := miIccmSlave.io.a_address
  io.a_data := miIccmSlave.io.a_data
  io.a_opcode := miIccmSlave.io.a_opcode
  io.a_source := miIccmSlave.io.a_source
  io.a_valid := miIccmSlave.io.a_valid

  // the data received by the master interface is then sent out to be used by the fetch module
  io.inst := miIccmSlave.io.data
  // setting the stall pin to be 0 since we don't need to stall the core right now
  io.stall := 0.U

  // here we see if the data i.e the instruction received by the master is 0 on PC 0
  // it means that the instruction memory is empty and we need to stall the core and wait
  // for the UART to write the program to memory, further we are checking for UART_DONE signal
  // which is true if the UART has finished writing program to instruction memory
  when(miIccmSlave.io.data === 0.U && io.pcAddr === 0.U && io.UART_DONE === false.B) {
    io.inst := 0.U
    io.stall := 1.U
  }

  // as soon as the UART writes the first instruction of the program into the memory our above
  // condition will fail i.e at PC 0 the data will not be 0 now but it will be the instruction
  // but right now we can't resume the core until the uart has written all the program instructions
  // in the memory, so we check for this condition if data is not 0 (instruction is present) at
  // PC -> 0 but the UART_DONE is still false that means we need to wait for the UART to completely
  // write the program to the memory
  when(miIccmSlave.io.data =/= 0.U && io.pcAddr === 0.U && io.UART_DONE === false.B) {
    io.inst := 0.U
    io.stall := 1.U
    }



}

