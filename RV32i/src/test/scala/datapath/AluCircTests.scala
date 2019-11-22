package datapath

import chisel3.iotesters.PeekPokeTester

class AluCircTests(c: AluCirc) extends PeekPokeTester(c) {
    poke(c.io.in_func3, 0)
    poke(c.io.in_func7, 1)
    poke(c.io.in_aluOp, 0)
    poke(c.io.in_oper_A, 10)
    poke(c.io.in_oper_B, 4)
    step(1)
    expect(c.io.output, 6)
}