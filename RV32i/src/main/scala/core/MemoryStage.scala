package core

import chisel3._
import chisel3.util.MuxCase

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
    val data_be_o  = Output(Vec(4, Bool()))
    val ctrl_MemWr_out = Output(UInt(1.W)) // data_we_o
    val rs2_out = Output(SInt(32.W)) // data_wdata_o
    val memAddress = Output(SInt(32.W)) // data_addr_o
    val data_out   = Output(SInt(32.W))


    val alu_output = Output(SInt(32.W))
    val rd_sel_out = Output(UInt(5.W))
    val ctrl_RegWr_out = Output(UInt(1.W))
    val ctrl_MemRd_out = Output(UInt(1.W))
    val ctrl_MemToReg_out = Output(UInt(1.W))

    val stall = Output(Bool())
  })

//  val store = Module(new Store_unit())
//  val load = Module(new Load_unit())

  val data_offset = io.EX_MEM_alu_output(1,0)


  // Stalling the pipeline as soon as we get a load or store instruction
  // and the data received from memory is not valid.
  // As soon as we get a valid data from memory we pull down the stall.

  io.stall := (io.EX_MEM_MemWr === 1.U || io.EX_MEM_MemRd === 1.U) && !io.data_rvalid_i

  /** [START] ||||||||||||||||| SETTING MASK BITS FOR WRITE OPERATIONS [START] ||||||||||||||||||| */

  /** ******************************************START****************************************************** */

  when(io.func3 === "b010".U && io.EX_MEM_MemWr === 1.U) {
    /** Visualize memory as follows
     *      11          10        9         8  -> address
     * | d[31:24] | d[23:16] | d[15:8] | d[7:0] |
     *      7           6         5         4  -> address
     * | d[31:24] | d[23:16] | d[15:8] | d[7:0] |
     *      3           2         1         0  -> address
     * | d[31:24] | d[23:16] | d[15:8] | d[7:0] |  */
    // store word
    when(data_offset === "b00".U) {
      // addressing 0,4,8... location of memory, enable all mask bits to write 32 bits data.
      for(i <- 0 until 4) {
        io.data_be_o(i) := true.B
      }
      // data_be_o -> 1111
    } .elsewhen(data_offset === "b01".U) {
      // addressing 1,5,9... location of memory, enable 3 MSB mask bits to write data, ignore the first byte location
      io.data_be_o(0) := false.B
      for(i <- 1 until 4) {
        io.data_be_o(i) := true.B
      }
      // data_be_o -> 1110
    } .elsewhen(data_offset === "b10".U) {
      // addressing 2,6,10... location of memory, enable first and second bytes to write data. Ignore the first two bytes
      for(i <- 0 until 2) {
        io.data_be_o(i) := false.B
      }
      for(i <- 2 until 4) {
        io.data_be_o(i) := true.B
      }
      // data_be_o -> 1100
    } .elsewhen(data_offset === "b11".U) {
      // addressing 3,7,11... location of memory, enable just 1 MSB bit to write data. Ignore 3 LSB bytes.
      for(i <- 0 until 3) {
        io.data_be_o(i) := false.B
      }
      io.data_be_o(3) := true.B
      // data_be_o -> 1000
    } .otherwise {
      for(i <- 0 until 4) {
        io.data_be_o(i) := true.B   // by default setting all bits of mask to 1.
      }
    }

  } .elsewhen(io.func3 === "b001".U && io.EX_MEM_MemWr === 1.U) {
    // store halfword
    when(data_offset === "b00".U) {
      // addressing 0,4,8... location of memory, enable two LSB mask bits to write 16 bits data.
      for(i <- 0 until 2) {
        io.data_be_o(i) := true.B
      }
      for(i <- 2 until 4) {
        io.data_be_o(i) := false.B
      }
      // data_be_o -> 0011
    } .elsewhen(data_offset === "b01".U) {
      // addressing 1,5,9... location of memory, enable 1st and 2nd MSB mask bits to write data, ignore the first and last byte location
      io.data_be_o(0) := false.B
      for(i <- 1 until 3) {
        io.data_be_o(i) := true.B
      }
      io.data_be_o(4) := false.B
      // data_be_o -> 0110
    } .elsewhen(data_offset === "b10".U) {
      // addressing 2,6,10... location of memory, enable 2 MSB mask bits to write data. Ignore the first two bytes
      for(i <- 0 until 2) {
        io.data_be_o(i) := false.B
      }
      for(i <- 2 until 4) {
        io.data_be_o(i) := true.B
      }
      // data_be_o -> 1100
    } .elsewhen(data_offset === "b11".U) {
      // addressing 3,7,11... location of memory, enable just 1 MSB bit to write data. Ignore 3 LSB bytes.
      for(i <- 0 until 3) {
        io.data_be_o(i) := false.B
      }
      io.data_be_o(3) := true.B
      // data_be_o -> 1000
    } .otherwise {
      for(i <- 0 until 4) {
        io.data_be_o(i) := true.B   // by default setting all bits of mask to 1.
      }
    }
  } .elsewhen(io.func3 === "b000".U && io.EX_MEM_MemWr === 1.U) {
    // store byte
    when(data_offset === "b00".U) {
      // addressing 0,4,8... location of memory, enable zeroth LSB mask bit to write 8 bits data.
      io.data_be_o(0) := true.B
      for(i <- 1 until 4) {
        io.data_be_o(i) := false.B
      }
      // data_be_o -> 0001
    } .elsewhen(data_offset === "b01".U) {
      // addressing 1,5,9... location of memory, enable 1st  mask bit to write 8 bits data, ignore the zeroth, second and last byte location
      io.data_be_o(0) := false.B
      io.data_be_o(1) := true.B
      for(i <- 2 until 4) {
        io.data_be_o(i) := false.B
      }
      // data_be_o -> 0010
    } .elsewhen(data_offset === "b10".U) {
      // addressing 2,6,10... location of memory, enable 2 MSB mask bits to write data. Ignore the first two bytes
      for(i <- 0 until 2) {
        io.data_be_o(i) := false.B
      }
      io.data_be_o(2) := true.B
      io.data_be_o(3) := false.B
      // data_be_o -> 0100
    } .elsewhen(data_offset === "b11".U) {
      // addressing 3,7,11... location of memory, enable just 1 MSB bit to write data. Ignore 3 LSB bytes.
      for(i <- 0 until 3) {
        io.data_be_o(i) := false.B
      }
      io.data_be_o(3) := true.B
      // data_be_o -> 1000
    } .otherwise {
      for(i <- 0 until 4) {
        io.data_be_o(i) := true.B   // by default setting all bits of mask to 1.
      }
    }
  } .otherwise {
    io.data_be_o := DontCare
  }
  // TODO use these connections to implement masked writes in dccm

  /** ******************************************END****************************************************** */





  when(io.data_gnt_i && (io.EX_MEM_MemWr===1.U))
  {
    io.data_req_o := true.B
    io.memAddress := io.EX_MEM_alu_output(13, 0).asSInt
//    io.rs2_out    := store.io.StoreData
    io.rs2_out    := io.EX_MEM_rs2
  } .elsewhen(io.data_gnt_i && (io.EX_MEM_MemRd === 1.U)) {
    io.data_req_o := true.B
    io.memAddress := io.EX_MEM_alu_output(13, 0).asSInt
    io.rs2_out := DontCare
  } .otherwise
    {
      io.data_req_o := false.B
      io.memAddress := DontCare
      io.rs2_out        := DontCare
    }

  when(io.data_rvalid_i)
  {
    io.data_out     := io.data_rdata_i
  }
    .otherwise
    {
      io.data_out     := DontCare
    }
      

  io.ctrl_MemWr_out := io.EX_MEM_MemWr
  io.alu_output := io.EX_MEM_alu_output

  io.rd_sel_out := io.EX_MEM_rd_sel
  io.ctrl_RegWr_out := io.EX_MEM_RegWr
  io.ctrl_MemRd_out := io.EX_MEM_MemRd
  io.ctrl_MemToReg_out := io.EX_MEM_MemToReg
    


}