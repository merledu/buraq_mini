package core
import chisel3._
import chisel3.util.Cat
import chisel3.util.Fill
class Load_unit extends Module {
    val io=IO(new Bundle{
        val func3 = Input(UInt(3.W))
        val en = Input(Bool())
        val MemData  = Input(SInt(32.W))
        val LoadData = Output(SInt(32.W))
})
    val lb = io.MemData(7,0)
    val lh = io.MemData(15,0)
    val zero = 0.U

    when(io.en) {
        when(io.func3 === "b000".U) {
            // load byte
            io.LoadData := Cat(Fill(24,lb(7)),lb).asSInt
        } .elsewhen(io.func3 === "b001".U) {
            // load halfword
            io.LoadData := Cat(Fill(16,lh(15)),lh).asSInt
        } .elsewhen(io.func3 === "b110".U) {
            //  load word unsigned
            io.LoadData := io.MemData
        } .elsewhen(io.func3 === "b100".U) {
            // load byte unsigned
            io.LoadData := Cat(Fill(24,zero),lb).asSInt
        } .elsewhen(io.func3 === "b101".U) {
            // load byte unsigned
            io.LoadData := Cat(Fill(16,zero),lh).asSInt
        } .otherwise {
            io.LoadData := io.MemData // load word
        }
    } .otherwise {
        io.LoadData := DontCare
    }

    
}