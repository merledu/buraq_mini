import chisel3._
import chisel3.iotesters.PeekPokeTester

class HarnessTester(c: Harness) extends PeekPokeTester(c) {
  step(100)
}
