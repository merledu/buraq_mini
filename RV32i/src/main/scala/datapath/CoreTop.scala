package datapath
import chisel3._

class CoreTop extends Module {
  val io = IO(new Bundle {
    val imem_out = Output(UInt(32.W))
  })

  val top = Module(new Top)
  val imem = Module(new InstructionMem)
  val dmem = Module(new DataMem)

  imem.io.wrAddr := top.io.imem_wrAddr
  top.io.imem_data := imem.io.readData

  dmem.io.memAddress := top.io.dmem_memAddr
  dmem.io.memWrite := top.io.dmem_memWr
  dmem.io.memRead := top.io.dmem_memRd
  dmem.io.memData := top.io.dmem_memData

  top.io.dmem_data := dmem.io.memOut

  // Just for simulation
  io.imem_out := imem.io.readData
}