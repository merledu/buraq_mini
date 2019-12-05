package datapath
import chisel3.iotesters.PeekPokeTester

class MemoryTests(c: Memory) extends PeekPokeTester(c) {
  poke(c.io.imem_wrAddr, 0)
  poke(c.io.dmem_memAddr, 0)
  poke(c.io.dmem_memWr, 0)
  poke(c.io.dmem_memRd, 0)
  poke(c.io.dmem_memData, 0)
  step(1)
}