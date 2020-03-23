package core
import chisel3.iotesters.PeekPokeTester
import soc.DataMem

class DataMemTests(c: DataMem) extends PeekPokeTester(c) {
    step(1)
}