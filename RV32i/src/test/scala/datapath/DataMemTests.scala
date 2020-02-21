package datapath
import chisel3.iotesters.PeekPokeTester

class DataMemTests(c: DataMem) extends PeekPokeTester(c) {
    step(1)
}