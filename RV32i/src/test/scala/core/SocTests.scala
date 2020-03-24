package core
import chisel3.iotesters.PeekPokeTester
import soc.Soc

class SocTests(c: Soc) extends PeekPokeTester (c){
//  poke(c.io.gpio_1, 1)
//  poke(c.io.gpio_2, 0)
//  poke(c.io.gpio_3, 0)
//  poke(c.io.gpio_4, 1)
  val insts = Array(0xf9010113,
0x06812623,
0x07010413,
0xfa042e23,
0xfc042023,
0xfc042223,
0xfc042423,
0xfc042623,
0xfc042823,
0xfc042a23,
0xfc042c23,
0xfc042e23,
0xfe042023,
0xfe042223,
0x00b00793,
0xfaf42e23,
0x01d00793,
0xfcf42023,
0x05300793,
0xfcf42223,
0x06d00793,
0xfcf42423,
0x05a00793,
0xfcf42623,
0x07000793,
0xfcf42823,
0x06200793,
0xfcf42a23,
0x04300793,
0xfcf42c23,
0x04d00793,
0xfcf42e23,
0x02d00793,
0xfef42023,
0xf8042823,
0xf8042a23,
0xf8042c23,
0xf8042e23,
0xfa042023,
0xfa042223,
0xfa042423,
0xfa042623,
0xfa042823,
0xfa042a23,
0xfa042c23,
0x05900793,
0xf8f42823,
0x03e00793,
0xf8f42a23,
0x04900793,
0xf8f42c23,
0x05a00793,
0xf8f42e23,
0x00700793,
0xfaf42023,
0x05900793,
0xfaf42223,
0x02200793,
0xfaf42423,
0x01400793,
0xfaf42623,
0x04800793,
0xfaf42823,
0x06400793,
0xfaf42a23,
0xfe042423,
0xfe042623,
0x0400006f,
0xfec42783,
0x00279793,
0xff040713,
0x00f707b3,
0xfcc7a703,
0xfec42783,
0x00279793,
0xff040693,
0x00f687b3,
0xfa07a783,
0x00f707b3,
0xfef42423,
0xfec42783,
0x00178793,
0xfef42623,
0xfec42703,
0x00900793,
0xfae7dee3,
0x00000013,
0x00000013,
0x06c12403,
0x07010113, 0x00000fff)
//val insts = Array(0x00004237, 0x00000fff)
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
  }
  step(1000)
}

