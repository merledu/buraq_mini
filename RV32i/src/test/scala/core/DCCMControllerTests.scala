package core
import chisel3.iotesters.PeekPokeTester
import soc.DCCMController

class DCCMControllerTests(c: DCCMController) extends PeekPokeTester (c){
  step(1)
}
