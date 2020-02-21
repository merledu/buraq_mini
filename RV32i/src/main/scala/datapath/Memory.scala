package datapath
import chisel3._

class Memory extends Module {
  val io = IO(new Bundle {
    val imem_wrAddr = Input(UInt(10.W))
    val dmem_memAddr = Input(UInt(10.W))
    val dmem_memWr = Input(UInt(1.W))
    val dmem_memRd = Input(UInt(1.W))
    val dmem_memData = Input(SInt(32.W))

    val imem_out = Output(UInt(32.W))
    val dmem_out = Output(SInt(32.W))
  })

//  val imem = Module(new InstructionMem)
//  val dmem = Module(new DataMem)
//
//  imem.io.wrAddr := io.imem_wrAddr
//  io.imem_out := imem.io.readData
//
//  dmem.io.memAddress := io.dmem_memAddr
//  dmem.io.memRead := io.dmem_memRd
//  dmem.io.memWrite := io.dmem_memWr
//  dmem.io.memData := io.dmem_memData
//  io.dmem_out := dmem.io.memOut

}