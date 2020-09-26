package core
import chisel3._
import chisel3.util.Cat
import chisel3.util.Fill
class Load_unit extends Module
{
    val io=IO(new Bundle{
        val func3 = Input(UInt(3.W))
        val MemRead= Input(UInt(1.W))
        val MemData  = Input(SInt(32.W))
        val LoadData = Output(SInt(32.W))
})

     val lb = io.MemData(7,0)
     val lh = io.MemData(15,0)
     val zero = 0.U

    when(io.func3 === "b000".U && io.MemRead === 1.U) // load byte
    {
        io.LoadData := Cat(Fill(24,lb(7)),lb).asSInt
    }
    .elsewhen(io.func3 === "b001".U && io.MemRead === 1.U) // load halfword
    {
        io.LoadData := Cat(Fill(16,lh(15)),lh).asSInt
    }
    .elsewhen(io.func3 === "b110".U && io.MemRead === 1.U) //  load word unsigned
    {
        io.LoadData := io.MemData 
    }
    .elsewhen(io.func3 === "b100".U && io.MemRead === 1.U) // load byte unsigned
    {
        io.LoadData := Cat(Fill(24,zero),lb).asSInt
    }
    .elsewhen(io.func3 === "b101".U && io.MemRead === 1.U) // load half word unsigned
    {
        io.LoadData := Cat(Fill(16,zero),lh).asSInt
    }
    .otherwise
    {
        io.LoadData := io.MemData // load word
    }
    
}