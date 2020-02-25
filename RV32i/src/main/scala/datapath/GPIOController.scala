package datapath
import chisel3._
import chisel3.util._

class GPIOController extends Module {
  val io = IO(new Bundle {
    val isReadGPIO = Input(UInt(1.W))
    val isSetGPIO = Input(UInt(1.W))
    val GPIO_data = Input(UInt(32.W))
    val gpio_1_in = Input(UInt(1.W))
    val gpio_2_in = Input(UInt(1.W))
    val gpio_3_in = Input(UInt(1.W))
    val gpio_4_in = Input(UInt(1.W))

    val GPIO_in_values = Output(UInt(32.W))
  })

  val gpio_in_registers = RegInit(VecInit(Seq.fill(4)(0.U(1.W))))
  val gpio_out_registers = RegInit(VecInit(Seq.fill(4)(0.U(1.W))))

  gpio_in_registers(0) := io.gpio_1_in
  gpio_in_registers(1) := io.gpio_2_in
  gpio_in_registers(2) := io.gpio_3_in
  gpio_in_registers(3) := io.gpio_4_in

  when(io.isReadGPIO === 1.U) {
    val gpio_1 = gpio_in_registers(0)
    val gpio_2 = gpio_in_registers(1)
    val gpio_3 = gpio_in_registers(2)
    val gpio_4 = gpio_in_registers(3)
    val gpio_values = Cat(Fill(28, 0.U), gpio_4, gpio_3, gpio_2, gpio_1)
    io.GPIO_in_values := gpio_values
  } .otherwise {
    io.GPIO_in_values := 0.U
  }

}
