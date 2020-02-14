package datapath
import chisel3.iotesters.PeekPokeTester

class BusControllerTests(c: BusController) extends PeekPokeTester (c){

  step(50)
}
