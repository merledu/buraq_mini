package core
import chisel3.iotesters.PeekPokeTester
import soc.ICCMController

class ICCMControllerTests(c: ICCMController) extends PeekPokeTester (c){
  step(1)
}
