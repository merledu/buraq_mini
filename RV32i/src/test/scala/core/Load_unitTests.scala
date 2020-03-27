package core
import chisel3.iotesters.PeekPokeTester

class Load_unitTests(c: Load_unit) extends PeekPokeTester(c) {
  poke(c.io.func3, 0)
  poke(c.io.MemRead,1)
  poke(c.io.MemData, 32)
  step(1)
}
