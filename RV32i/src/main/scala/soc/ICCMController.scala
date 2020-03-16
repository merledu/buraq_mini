package soc
import chisel3._
import merl.uit.tilelink.SlaveInterface

/**
 * FetchSlaveIO is the bundle for carrying all the ports related to
 * the slave interface which is connected with the master interface of FetchBusController.
 * It contains the Channel A inputs coming in from the master interface of FetchBusController
 * and Channel D outputs going into the master interface of FetchBusController.
 */
class FetchSlaveIO extends Bundle {
  // Channel A
  val a_address = Input(UInt(32.W))
  val a_data = Input(UInt(32.W))
  val a_opcode = Input(UInt(3.W))
  val a_source = Input(UInt(32.W))
  val a_valid = Input(Bool())
  // Channel D
  val d_opcode = Output(UInt(3.W))
  val d_source = Output(UInt(32.W))
  val d_denied = Output(Bool())
  val d_valid = Output(Bool())
  val d_data = Output(UInt(32.W))
}

/**
 * UARTSlaveIO is the bundle for carrying all the ports related to the
 * slave interface which is connected with the master interface of the
 * UART Controller. It contains the Channel A inputs coming in from the
 * master interface of UART Controller and Channel D outputs going to
 * the master interface of UART Controller
 */
class UARTSlaveIO extends Bundle {
  // Channel A
  val a_address = Input(UInt(32.W))
  val a_data = Input(UInt(32.W))
  val a_opcode = Input(UInt(3.W))
  val a_source = Input(UInt(32.W))
  val a_valid = Input(Bool())
  // Channel D
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
  val uartSlaveIO = new UARTSlaveIO()
  // enable pin coming from the uart controller
  val UART_EN = Input(Bool())
  // The ICCM controller outputs for the Instruction memory
  val addr_out = Output(UInt(32.W))
  val data_out = Output(UInt(32.W))
  val en = Output(Bool())
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

  val si2 = Module(new SlaveInterface(forFetch = false))
  /**
   * ackDataFromModule is hardwired to 0 because this slave interface is for UART communication with
   * the memory and we won't be getting any data from the memory back to us.
   */
  si2.io.ackDataFromModule := 0.U

  si2.io.a_address := io.uartSlaveIO.a_address
  si2.io.a_data := io.uartSlaveIO.a_data
  si2.io.a_opcode := io.uartSlaveIO.a_opcode
  si2.io.a_source := io.uartSlaveIO.a_source
  si2.io.a_valid := io.uartSlaveIO.a_valid
  io.uartSlaveIO.d_opcode := si2.io.d_opcode
  io.uartSlaveIO.d_source := si2.io.d_source
  io.uartSlaveIO.d_denied := si2.io.d_denied
  io.uartSlaveIO.d_valid := si2.io.d_valid
  io.uartSlaveIO.d_data := si2.io.d_data

  /**
   * This UART_EN signal is coming from the uart controller which tells that the uart wants to write something
   * to the memory. Which means when UART_EN signal is true then the ICCM controller's output to the instruction
   * memory will be that of the UART scenario. Otherwise the ICCM Controller's output to the instruction memory
   * will be that of the normal fetch scenario.
   */
  when(io.UART_EN) {
    // UART SCENARIO
    io.addr_out := si2.io.addr_out
    io.data_out := si2.io.data_out
    io.en := io.UART_EN
  } .otherwise {
    // FETCH SCENARIO
    /**
     * In this case UART_EN will be false. The UART_EN get's false when it does not want to write
     * garbage value to the memory, that does not necessarily mean that the uart has finished doing
     * it's work. It might have to make UART_EN high when it samples the data and want's to write
     * to the memory again. In this case we will be in the FETCH SCENARIO even when the uart has not
     * completed it's work. So after the first UART_EN high iteration there will be a single instruction
     * in the memory and then when the UART_EN goes low we will be in the FETCH SCENARIO which then get's
     * the instruction from the memory to the Fetch Bus Controller. But the instruction doesn't go any further
     * from there since there is a check there which sees if the uart has completed it's work fully or not.*/

    /**
     * Since we don't need to PUT anything during fetch we set the data out and enable of
     * the ICCM Controller to 0.
     */
    io.addr_out := si1.io.addr_out
    io.data_out := 0.U
    io.en := false.B
  }

}
