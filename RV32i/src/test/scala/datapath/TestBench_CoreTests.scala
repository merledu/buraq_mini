package datapath
import chisel3.iotesters.PeekPokeTester

class TestBench_CoreTests(c: TestBench_Core) extends PeekPokeTester(c) {
  step(20)
}