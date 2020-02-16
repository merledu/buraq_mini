package datapath
import chisel3.iotesters.PeekPokeTester

class ICCMControllerTests(c: ICCMController) extends PeekPokeTester (c){
  step(1)
}
