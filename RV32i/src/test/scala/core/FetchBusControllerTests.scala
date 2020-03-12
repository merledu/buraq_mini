package core
import chisel3.iotesters.PeekPokeTester

class FetchBusControllerTests(c: FetchBusController) extends PeekPokeTester (c){
  poke(c.io.pcAddr, 0)
  poke(c.io.d_opcode, 0)
  poke(c.io.d_source, 0)
  poke(c.io.d_denied, 0)
  poke(c.io.d_valid, 0)
  poke(c.io.d_data, 0)
  step(1)
}
