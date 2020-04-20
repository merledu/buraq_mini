package soc

import chisel3._

class DataMem extends Module {
    val io = IO(new Bundle {
            //val memWrite = Input(UInt(1.W))
            //val memRead = Input(UInt(1.W))
            val enable = Input(UInt(1.W))
            val memAddress = Input(UInt(32.W))
            val memData = Input(SInt(32.W))    
            val memOut = Output(SInt(32.W))  
    })

    val mem = Mem(268435456, SInt(32.W))
    when(io.enable === 1.U) {
        // Store
        mem.write(io.memAddress, io.memData)
        io.memOut := 0.S
        //io.memOut := mem.read(io.memAddress)
    } .otherwise {
        // Load
        io.memOut := mem.read(io.memAddress)
    }

}