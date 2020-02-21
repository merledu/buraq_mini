package datapath
import chisel3.iotesters.PeekPokeTester

class DCCMControllerTests(c: DCCMController) extends PeekPokeTester (c){
  step(1)
}
