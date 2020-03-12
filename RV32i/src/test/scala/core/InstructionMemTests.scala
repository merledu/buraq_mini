package core
import chisel3.iotesters.PeekPokeTester
import soc.InstructionMem

class InstructionMemTests(c: InstructionMem) extends PeekPokeTester(c) {
    poke(c.io.wrAddr, 1)
    step(1)
    
}