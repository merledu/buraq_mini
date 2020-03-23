package core
import chisel3.iotesters.PeekPokeTester
import merl.uit.tilelink.SlaveInterface

class SlaveInterfaceTests(c: SlaveInterface) extends PeekPokeTester(c) {
  step(1)
}
