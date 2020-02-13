package datapath
import chisel3._

class TestBench_Core extends Module {
  val io = IO(new Bundle {
    val stall = Input(UInt(1.W))
    val imem_out = Output(UInt(32.W))
    val reg_7_output = Output(SInt(32.W))
  })

  val core = Module(new Core)
  val mem = Module(new Memory)

  core.io.stall := io.stall

  mem.io.imem_wrAddr := core.io.imem_wrAddr
  core.io.imem_data := mem.io.imem_out

  mem.io.dmem_memAddr := core.io.dmem_memAddr
  mem.io.dmem_memWr := core.io.dmem_memWr
  mem.io.dmem_memRd := core.io.dmem_memRd
  mem.io.dmem_memData := core.io.dmem_memData

  core.io.dmem_data := mem.io.dmem_out

  // Just for simulation
  io.imem_out := mem.io.imem_out

  io.reg_7_output := core.io.reg_7
}