package datapath
import chisel3._

class CoreTop extends Module {
  val io = IO(new Bundle {
    val imem_out = Output(UInt(32.W))
  })

  val top = Module(new Top)
  val mem = Module(new Memory)

  mem.io.imem_wrAddr := top.io.imem_wrAddr
  top.io.imem_data := mem.io.imem_out

  mem.io.dmem_memAddr := top.io.dmem_memAddr
  mem.io.dmem_memWr := top.io.dmem_memWr
  mem.io.dmem_memRd := top.io.dmem_memRd
  mem.io.dmem_memData := top.io.dmem_memData

  top.io.dmem_data := mem.io.dmem_out

  // Just for simulation
  io.imem_out := mem.io.imem_out
}