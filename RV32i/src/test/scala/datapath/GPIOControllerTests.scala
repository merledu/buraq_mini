package datapath
import chisel3.iotesters.PeekPokeTester

class GPIOControllerTests(c: GPIOController) extends PeekPokeTester (c){
  step(1)
}
