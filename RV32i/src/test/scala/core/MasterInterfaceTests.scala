package core
import chisel3.iotesters.PeekPokeTester
import merl.uit.tilelink.MasterInterface

class MasterInterfaceTests(c: MasterInterface) extends PeekPokeTester(c) {
  step(1)
}
