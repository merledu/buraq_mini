package core
import chisel3.iotesters.PeekPokeTester
import soc.Soc

class SocTests(c: Soc) extends PeekPokeTester (c){
  poke(c.io.gpio_1, 1)
  poke(c.io.gpio_2, 0)
  poke(c.io.gpio_3, 0)
  poke(c.io.gpio_4, 1)
  step(100)
}
