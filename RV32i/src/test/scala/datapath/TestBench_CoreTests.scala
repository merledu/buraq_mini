package datapath
import chisel3.iotesters.PeekPokeTester

class TestBench_CoreTests(c: TestBench_Core) extends PeekPokeTester(c) {
  poke(c.io.stall, 1)
  step(50)
  poke(c.io.stall, 0)
  step(2)
  poke(c.io.stall, 1)
  step(20)
  poke(c.io.stall, 0)
  step(100)
}