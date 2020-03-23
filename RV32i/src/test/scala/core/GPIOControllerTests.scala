package core
import chisel3.iotesters.PeekPokeTester
import peripherals.GPIOController

class GPIOControllerTests(c: GPIOController) extends PeekPokeTester (c){
  step(1)
}
