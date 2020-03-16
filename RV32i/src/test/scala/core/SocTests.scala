package core
import chisel3.iotesters.PeekPokeTester
import soc.Soc

class SocTests(c: Soc) extends PeekPokeTester (c){
//  poke(c.io.gpio_1, 1)
//  poke(c.io.gpio_2, 0)
//  poke(c.io.gpio_3, 0)
//  poke(c.io.gpio_4, 1)
  val insts = Array(0x00200113, 0x00100193, 0x00310233, 0x00000fff)
  poke(c.io.rxd, 1)
  //step(10)
  for (inst <- insts) {
    val half_byte1 = inst & 0x0f  // 3
    val half_byte2 = (inst & 0xf0) >> 4 // 1
    val byte1 = (half_byte2 << 4) | half_byte1 // 0x13

    val half_byte3 = (inst & 0xf00) >> 8  // 1
    val half_byte4 = (inst & 0xf000) >> 12  // 0
    val byte2 = (half_byte4 << 4) | half_byte3  // 0x01

    val half_byte5 = (inst & 0xf0000) >> 16 // 0
    val half_byte6 = (inst & 0xf00000) >> 20  // 2
    val byte3 = (half_byte6 << 4) | half_byte5  // 0x20

    val half_byte7 = (inst & 0xf000000) >> 24 // 0
    val half_byte8 = (inst & 0xf0000000) >> 28  // 0
    val byte4 = (half_byte8 << 4) | half_byte7  // 0x00

    //printf("The instruction is %x".format(byte4))
    pokeUart(byte1.toInt)
    pokeUart(byte2.toInt)
    pokeUart(byte3.toInt)
    pokeUart(byte4.toInt)
  }


 // printf("First two LSB %s".format(inst.slice(8, 10)))


  // 32 bit data payload
//  pokeUart(0x13.toInt)
//  pokeUart(0x01.toInt)
//  pokeUart(0x20.toInt)
//  pokeUart(0x00.toInt)
//
//  // Another 32-bit data payload
//  pokeUart(0x93.toInt)
//  pokeUart(0x01.toInt)
//  pokeUart(0x10.toInt)
//  pokeUart(0x00.toInt)
//
//  // Another 32-bit data payload
//  pokeUart(0x33.toInt)
//  pokeUart(0x02.toInt)
//  pokeUart(0x31.toInt)
//  pokeUart(0x00.toInt)
//
//  // The data payload which tells the uart that program to be loaded is finished
//  pokeUart(0xff.toInt)
//  pokeUart(0x0f.toInt)
//  pokeUart(0x00.toInt)
//  pokeUart(0x00.toInt)


  step(5)



  def pokeUart(value: Int): Unit = {

    // start bit
    poke(c.io.rxd, 0)
    step(3)
    // 8 data bits
    for (i <- 0 until 8) {
      poke(c.io.rxd, (value >> i) & 0x01)
      step(3)
    }
    // stop bit
    poke(c.io.rxd, 1)
    step(2)

    // read it out
    poke(c.io.ready, 1)
    step(1)
    poke(c.io.ready, 0)
    step(1)
  }

  step(100)
}
