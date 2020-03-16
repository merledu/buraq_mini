package core

import chisel3._
import merl.uit.tilelink.MasterInterface

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
   * This pin is also connected with the UART controller telling that it can now start to receive items from Tx.
   * This pin is also connected with the ICCM controller telling that it needs to set the output of ICCM controller
   * to the instruction memory according to the UART scenario and not the FETCH scenario.
   */
  val stall = Output(UInt(1.W))
  // Instruction output going to the Fetch module
  val inst = Output(UInt(32.W))

}

class FetchBusController extends Module {
  val io = IO(new FetchBusControllerIO)

  val miIccmSlave = Module(new MasterInterface(sourceId = 1.U, forFetch = true))
  miIccmSlave.io.memRd := 0.U
  miIccmSlave.io.memWrt := 0.U
  miIccmSlave.io.addr_in := io.pcAddr
  miIccmSlave.io.data_in := 0.U
  miIccmSlave.io.d_data := io.d_data
  miIccmSlave.io.d_denied := io.d_denied
  miIccmSlave.io.d_opcode := io.d_opcode
  miIccmSlave.io.d_source := io.d_source
  miIccmSlave.io.d_valid := io.d_valid

  io.a_address := miIccmSlave.io.a_address
  io.a_data := miIccmSlave.io.a_data
  io.a_opcode := miIccmSlave.io.a_opcode
  io.a_source := miIccmSlave.io.a_source
  io.a_valid := miIccmSlave.io.a_valid


  io.inst := miIccmSlave.io.data
  io.stall := 0.U

  when(miIccmSlave.io.data === 0.U && io.pcAddr === 0.U && io.UART_DONE === false.B) {
    io.inst := 0.U
    io.stall := 1.U
  }
  when(miIccmSlave.io.data =/= 0.U && io.pcAddr === 0.U && io.UART_DONE === false.B) {
    io.inst := 0.U
    io.stall := 1.U
    }



}

