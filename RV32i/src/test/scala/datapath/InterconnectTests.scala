package datapath
import chisel3.iotesters.PeekPokeTester

class InterconnectTests(c: Interconnect) extends PeekPokeTester (c){
  step(100)
}
