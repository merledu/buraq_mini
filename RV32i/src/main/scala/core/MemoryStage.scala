package core

import chisel3._

class MemoryStage extends Module {
  val io = IO(new Bundle {
    val EX_MEM_alu_output = Input(SInt(32.W))
    val EX_MEM_rd_sel = Input(UInt(5.W))
    val EX_MEM_RegWr = Input(UInt(1.W))
    val EX_MEM_MemRd = Input(UInt(1.W))
    val EX_MEM_MemToReg = Input(UInt(1.W))
    val EX_MEM_MemWr = Input(UInt(1.W))
    val EX_MEM_rs2 = Input(SInt(32.W))
    val func3      = Input(UInt(3.W))

    val data_gnt_i   = Input(Bool())
    val data_rvalid_i= Input(Bool())
    val data_rdata_i = Input(SInt(32.W))
    val data_req_o   = Output(Bool())
    val data_be_o  = Output(UInt(4.W))
    val ctrl_MemWr_out = Output(UInt(1.W)) // data_we_o
    val rs2_out = Output(SInt(32.W)) // data_wdata_o
    val memAddress = Output(SInt(32.W)) // data_addr_o
    val data_out   = Output(SInt(32.W))


    val alu_output = Output(SInt(32.W))
    val rd_sel_out = Output(UInt(5.W))
    val ctrl_RegWr_out = Output(UInt(1.W))
//    val ctrl_MemRd_out = Output(UInt(1.W))
    val ctrl_MemToReg_out = Output(UInt(1.W))
    // 
  // 

    val stall = Output(Bool())
  })

  val store = Module(new Store_unit())
  val load = Module(new Load_unit())

  /** ||||||||||||||||| SETTING BYTE ENABLE io.data_be_o TO BE USED BY THE TL-HOST ||||||||||||||||| */
  // func3 -> 001 means store half word instruction (sh)
  //          checking if address bit 1 is set. If it is set then it means
  //          we can address 2, 6, 10 ... locations in memory and for that
  //          memory mask should be 1100 to set the last two bytes of memory
  //          if not set then we can address 0, 4, 8 ... location in memory
  //          for that the mask must be 0011 to set the first two bytes of memory
  // func3 -> 010 means store word instruction (sw)
  //          no need to check address bits all the mask bits must be set for a
  //          store word instruction 1111 to write all 4 bytes in memory.
  //  TODO Store Byte
  //  func3 -> 000 means store byte instruction (sb)
  io.data_be_o := Mux(io.func3 === "b001".U, Mux(io.EX_MEM_alu_output(1), "b1100".U, "b0011".U),
                  Mux(io.func3 === "b010".U,"b1111".U, "b0000".U))

  io.stall := (io.EX_MEM_MemWr === 1.U || io.EX_MEM_MemRd === 1.U) && !io.data_rvalid_i

  when(io.data_gnt_i & (io.EX_MEM_MemWr===1.U))
  {
    io.data_req_o := true.B
    io.memAddress := io.EX_MEM_alu_output(13, 0).asSInt
    io.rs2_out        := store.io.StoreData
  }
    .otherwise
    {
      io.data_req_o := false.B
      io.memAddress := DontCare
      io.rs2_out        := DontCare
    }

  when(io.EX_MEM_MemRd === 1.U)
  {
    when(io.data_gnt_i & (io.EX_MEM_MemWr=/=1.U))
    {
      io.data_req_o := true.B
      io.memAddress := io.EX_MEM_alu_output(13, 0).asSInt
    }
      .otherwise
      {
        io.data_req_o := false.B
        io.memAddress := DontCare
      }
    load.io.MemRead := ~io.EX_MEM_MemWr
    store.io.MemWrite := 0.U
    io.ctrl_MemWr_out := 0.U
  }
    .otherwise
    {
      load.io.MemRead := 0.U
      store.io.MemWrite := io.EX_MEM_MemWr
      io.ctrl_MemWr_out := io.EX_MEM_MemWr
    }

  store.io.func3 := io.func3
  store.io.MemWrite := io.EX_MEM_MemWr
  store.io.Rs2      := io.EX_MEM_rs2

      
  load.io.func3 := io.func3
      
  load.io.MemData:= io.data_rdata_i
  when(io.data_rvalid_i)
  {
    io.data_out     := load.io.LoadData
  }
    .otherwise
    {
      io.data_out     := DontCare
    }
      

 //   io.memAddress := io.EX_MEM_alu_output(13, 0).asUInt
  io.ctrl_MemWr_out := io.EX_MEM_MemWr

  io.alu_output := io.EX_MEM_alu_output
  //io.rs2_out := io.EX_MEM_rs2
  io.rd_sel_out := io.EX_MEM_rd_sel
  io.ctrl_RegWr_out := io.EX_MEM_RegWr
 //   io.ctrl_MemRd_out := io.EX_MEM_MemRd
  io.ctrl_MemToReg_out := io.EX_MEM_MemToReg
    


}