package core
import chisel3.iotesters.PeekPokeTester
import uart_testbench.Top

class TopTests(c: Top) extends PeekPokeTester (c){
  poke(c.io.rxd_in, 1)
  step(10)
  // 32 bit data payload
  pokeUart(0x13.toInt)
  pokeUart(0x01.toInt)
  pokeUart(0xf0.toInt)
  pokeUart(0x00.toInt)

  // Another 32-bit data payload
  pokeUart(0x23.toInt)
  pokeUart(0x22.toInt)
  pokeUart(0x20.toInt)
  pokeUart(0x20.toInt)
  step(5)



  def pokeUart(value: Int): Unit = {

    // start bit
    poke(c.io.rxd_in, 0)
    step(3)
    // 8 data bits
    for (i <- 0 until 8) {
      poke(c.io.rxd_in, (value >> i) & 0x01)
      step(3)
    }
    // stop bit
    poke(c.io.rxd_in, 1)
    step(2)

    // read it out
    poke(c.io.ready, 1)
    step(1)
    poke(c.io.ready, 0)
    step(1)
  }
}
