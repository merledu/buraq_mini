package core
import chisel3.iotesters.PeekPokeTester
import soc.InstructionMem

class InstructionMemTests(c: InstructionMem) extends PeekPokeTester(c) {

    step(1)
    
}