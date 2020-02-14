package datapath
import chisel3.iotesters.PeekPokeTester

class StallDetectionTests(c: StallDetection) extends PeekPokeTester (c){
  step(1)
}
