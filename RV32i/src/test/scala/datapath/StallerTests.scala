package datapath
import chisel3.iotesters.PeekPokeTester

class StallerTests(c: Staller) extends PeekPokeTester (c){
  poke(c.io.isLoad, 1)
  poke(c.io.isStore, 0)
  expect(c.io.stall, 1)
}
