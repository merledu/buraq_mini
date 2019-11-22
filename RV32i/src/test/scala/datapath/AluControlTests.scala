package datapath

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class AluControlTests(c: AluControl) extends PeekPokeTester(c) {
    poke(c.io.aluOp, 2)
    poke(c.io.func3, 7)
    poke(c.io.func7, 0)
    step(1)
    expect(c.io.output, 23)
}