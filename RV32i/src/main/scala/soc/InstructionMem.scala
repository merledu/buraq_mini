package soc

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile


class InstructionMem extends Module {
//    val io = IO(new Bundle {
//            val wrAddr = Input(UInt(10.W))
//            val readData = Output(UInt(32.W))
//    })
//
//    val mem = Mem(1024, UInt(32.W))
////    loadMemoryFromFile(mem, "/Users/mbp/Desktop/mem1.txt")
//  loadMemoryFromFile(mem, "/home/hadirkhan/Desktop/mem1.txt")
//  io.readData := mem(io.wrAddr)

//    //io.readData := "h00200093".U

  val io = IO(new Bundle {
    val addr = Input(UInt(32.W))
    val en = Input(Bool())
    val data_in = Input(UInt(32.W))
    val data_out = Output(UInt(32.W))
  })

  val mem = Mem(4096, UInt(32.W))
  when(io.en) {
    mem.write(io.addr, io.data_in)
    io.data_out := 0.U
  } .otherwise {
    io.data_out := mem.read(io.addr)
  }

}
