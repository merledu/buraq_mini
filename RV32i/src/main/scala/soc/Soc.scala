package soc
import chisel3._
import _root_.core.Core
import peripherals.{GPIOController, UartController}

class Soc extends Module {
  val io = IO(new Bundle {
    val gpio_1 = Input(UInt(1.W))
    val gpio_2 = Input(UInt(1.W))
    val gpio_3 = Input(UInt(1.W))
    val gpio_4 = Input(UInt(1.W))
    val rxd = Input(UInt(1.W))

    val gpio_5 = Output(UInt(1.W))
    val gpio_6 = Output(UInt(1.W))
    val gpio_7 = Output(UInt(1.W))
    val gpio_8 = Output(UInt(1.W))

//    val inst_out = Output(UInt(32.W))
  })

  val core = Module(new Core)
  val iccmController = Module(new ICCMController)
  val dccmController = Module(new DCCMController)
  val uartController = Module(new UartController(10000,3000))
  val iccm = Module(new InstructionMem)
  val dccm = Module(new DataMem)
  val gpio = Module(new GPIOController)


  core.io.iccm_d_valid := iccmController.io.fetchSlaveIO.d_valid
  core.io.iccm_d_source := iccmController.io.fetchSlaveIO.d_source
  core.io.iccm_d_opcode := iccmController.io.fetchSlaveIO.d_opcode
  core.io.iccm_d_denied := iccmController.io.fetchSlaveIO.d_denied
  core.io.iccm_d_data := iccmController.io.fetchSlaveIO.d_data

  core.io.dccm_d_valid := dccmController.io.d_valid
  core.io.dccm_d_source := dccmController.io.d_source
  core.io.dccm_d_opcode := dccmController.io.d_opcode
  core.io.dccm_d_denied := dccmController.io.d_denied
  core.io.dccm_d_data := dccmController.io.d_data

  core.io.UART_DONE := uartController.io.done

  iccmController.io.fetchSlaveIO.a_valid := core.io.iccm_a_valid
  iccmController.io.fetchSlaveIO.a_source := core.io.iccm_a_source
  iccmController.io.fetchSlaveIO.a_opcode := core.io.iccm_a_opcode
  iccmController.io.fetchSlaveIO.a_data := core.io.iccm_a_data
  iccmController.io.fetchSlaveIO.a_address := core.io.iccm_a_address
  iccmController.io.UART_EN := uartController.io.en

  iccmController.io.uartSlaveIO.a_valid := uartController.io.masterInterfaceIO.a_valid
  iccmController.io.uartSlaveIO.a_source := uartController.io.masterInterfaceIO.a_source
  iccmController.io.uartSlaveIO.a_opcode := uartController.io.masterInterfaceIO.a_opcode
  iccmController.io.uartSlaveIO.a_data := uartController.io.masterInterfaceIO.a_data
  iccmController.io.uartSlaveIO.a_address := uartController.io.masterInterfaceIO.a_address

  uartController.io.masterInterfaceIO.d_valid := iccmController.io.uartSlaveIO.d_valid
  uartController.io.masterInterfaceIO.d_source := iccmController.io.uartSlaveIO.d_source
  uartController.io.masterInterfaceIO.d_opcode := iccmController.io.uartSlaveIO.d_opcode
  uartController.io.masterInterfaceIO.d_denied := iccmController.io.uartSlaveIO.d_denied
  uartController.io.masterInterfaceIO.d_data := iccmController.io.uartSlaveIO.d_data

  uartController.io.isStalled := core.io.isStalled
  uartController.io.rxd := io.rxd

  dccmController.io.a_valid := core.io.dccm_a_valid
  dccmController.io.a_source := core.io.dccm_a_source
  dccmController.io.a_opcode := core.io.dccm_a_opcode
  dccmController.io.a_data := core.io.dccm_a_data
  dccmController.io.a_address := core.io.dccm_a_address

  iccm.io.addr := iccmController.io.addr_out
  iccm.io.data_in := iccmController.io.data_out
  iccm.io.en := iccmController.io.en

  iccmController.io.ackDataFromModule := iccm.io.data_out
//  io.inst_out :=  iccmController.io.ackDataFromModule

  dccm.io.enable := dccmController.io.en
  dccm.io.memAddress := dccmController.io.addr_out
  dccm.io.memData := dccmController.io.data_out.asSInt
  dccmController.io.data_in := dccm.io.memOut.asUInt

  // GPIO connections
  gpio.io.isReadGPIO := core.io.readGPIO
  gpio.io.isSetGPIO := core.io.setGPIO
  gpio.io.GPIO_data := core.io.gpioData
  gpio.io.gpio_1_in := io.gpio_1
  gpio.io.gpio_2_in := io.gpio_2
  gpio.io.gpio_3_in := io.gpio_3
  gpio.io.gpio_4_in := io.gpio_4
  core.io.GPIO_values := gpio.io.GPIO_in_values

  io.gpio_5 := gpio.io.gpio_5_out
  io.gpio_6 := gpio.io.gpio_6_out
  io.gpio_7 := gpio.io.gpio_7_out
  io.gpio_8 := gpio.io.gpio_8_out

}
