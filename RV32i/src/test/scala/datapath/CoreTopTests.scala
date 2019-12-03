package datapath
import chisel3.iotesters.PeekPokeTester

class CoreTopTests(c: CoreTop) extends PeekPokeTester(c) {
  step(20)
}