package soc
import chisel3._
import _root_.core.Core
import peripherals.GPIOController

class Soc extends Module {
  val io = IO(new Bundle {
    val gpio_1 = Input(UInt(1.W))
    val gpio_2 = Input(UInt(1.W))
    val gpio_3 = Input(UInt(1.W))
    val gpio_4 = Input(UInt(1.W))

    val gpio_5 = Output(UInt(1.W))
    val gpio_6 = Output(UInt(1.W))
    val gpio_7 = Output(UInt(1.W))
    val gpio_8 = Output(UInt(1.W))

    val inst_out = Output(UInt(32.W))
  })

  val core = Module(new Core)
  val iccmController = Module(new ICCMController)
  val dccmController = Module(new DCCMController)
  val iccm = Module(new InstructionMem)
  val dccm = Module(new DataMem)
  val gpio = Module(new GPIOController)


  core.io.iccm_d_valid := iccmController.io.d_valid
  core.io.iccm_d_source := iccmController.io.d_source
  core.io.iccm_d_opcode := iccmController.io.d_opcode
  core.io.iccm_d_denied := iccmController.io.d_denied
  core.io.iccm_d_data := iccmController.io.d_data

  core.io.dccm_d_valid := dccmController.io.d_valid
  core.io.dccm_d_source := dccmController.io.d_source
  core.io.dccm_d_opcode := dccmController.io.d_opcode
  core.io.dccm_d_denied := dccmController.io.d_denied
  core.io.dccm_d_data := dccmController.io.d_data

  iccmController.io.a_valid := core.io.iccm_a_valid
  iccmController.io.a_source := core.io.iccm_a_source
  iccmController.io.a_opcode := core.io.iccm_a_opcode
  iccmController.io.a_data := core.io.iccm_a_data
  iccmController.io.a_address := core.io.iccm_a_address

  dccmController.io.a_valid := core.io.dccm_a_valid
  dccmController.io.a_source := core.io.dccm_a_source
  dccmController.io.a_opcode := core.io.dccm_a_opcode
  dccmController.io.a_data := core.io.dccm_a_data
  dccmController.io.a_address := core.io.dccm_a_address

  iccm.io.wrAddr := iccmController.io.addr_out
  iccmController.io.ackDataFromModule := iccm.io.readData
  io.inst_out :=  iccmController.io.ackDataFromModule

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
