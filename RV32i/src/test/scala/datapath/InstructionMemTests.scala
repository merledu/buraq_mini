package datapath
import chisel3.iotesters.PeekPokeTester

class InstructionMemTests(c: InstructionMem) extends PeekPokeTester(c) {
    poke(c.io.wrAddr, 1)
    step(1)
    
}