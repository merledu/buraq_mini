package core

import caravan.bus.wishbone.{Request, Response, WishboneConfig}
import chisel3._
import chisel3.util.{Cat, Decoupled}

class MemoryStage(implicit val config: WishboneConfig) extends Module {
  val io = IO(new Bundle {
    val EX_MEM_alu_output = Input(SInt(32.W))
    val EX_MEM_rd_sel = Input(UInt(5.W))
    val EX_MEM_RegWr = Input(UInt(1.W))
    val EX_MEM_CsrWe = Input(Bool())
    val EX_MEM_MemRd = Input(UInt(1.W))
    val EX_MEM_MemToReg = Input(UInt(1.W))
    val EX_MEM_MemWr = Input(UInt(1.W))
    val EX_MEM_rs2 = Input(SInt(32.W))
    val func3      = Input(UInt(3.W))
//    val EX_MEM_csr_addr = Input(SInt(32.W))
//    val EX_MEM_csr_op = Input(UInt(2.W))
    val EX_MEM_csr_data = Input(UInt(32.W))

    val coreDccmReq = Decoupled(new Request())
    val coreDccmRsp = Flipped(Decoupled(new Response()))
    //val data_gnt_i   = Input(Bool())
    //val data_rvalid_i= Input(Bool())
    //val data_rdata_i = Input(SInt(32.W))
    //val data_req_o   = Output(Bool())
    //val data_be_o  = Output(Vec(4, Bool()))
    //val ctrl_MemWr_out = Output(UInt(1.W)) // data_we_o
    //val data_wdata_o = Output(Vec(4, SInt(8.W))) // data_wdata_o
    //val memAddress = Output(SInt(32.W)) // data_addr_o
    val data_out   = Output(SInt(32.W))


    val alu_output = Output(SInt(32.W))
    val rd_sel_out = Output(UInt(5.W))
    val ctrl_RegWr_out = Output(UInt(1.W))
    val ctrl_CsrWen_out = Output(Bool())
    val ctrl_MemRd_out = Output(UInt(1.W))
    val ctrl_MemToReg_out = Output(UInt(1.W))
//    val csr_addr_out = Output(SInt(32.W))
//    val csr_op_out = Output(UInt(2.W))
    val csr_data_out = Output(UInt(32.W))

    val stall = Output(Bool())
  })

  val load_unit = Module(new Load_unit())

  val data_offset = io.EX_MEM_alu_output(1,0)
  val data_wdata = Wire(Vec(4, SInt(8.W)))

  // core always ready to accept response from bus
  io.coreDccmRsp.ready := true.B

  // Stalling the pipeline as soon as we get a load or store instruction
  // and the data received from memory is not valid.
  // As soon as we get a valid data from memory we pull down the stall.

  io.stall := (io.EX_MEM_MemWr === 1.U || io.EX_MEM_MemRd === 1.U) && !io.coreDccmRsp.valid

  /** |||||||||||||||||||||||||||||| INITIALIZING LOAD UNIT ||||||||||||||||||||||||||||||| */

  /** ******************************************START****************************************************** */
  load_unit.io.func3 := io.func3
  load_unit.io.memData := io.coreDccmRsp.bits.dataResponse.asSInt()
  load_unit.io.data_offset := data_offset
  load_unit.io.en := false.B

  /** ******************************************END****************************************************** */



  /** |||||||||||||||||||||||||||||| SETTING MASK BITS FOR WRITE OPERATIONS ||||||||||||||||||||||||||||||| */

  /** ******************************************START****************************************************** */

  /** Visualize memory as follows
   *      11          10        9         8  -> address
   * | d[31:24] | d[23:16] | d[15:8] | d[7:0] |
   *      7           6         5         4  -> address
   * | d[31:24] | d[23:16] | d[15:8] | d[7:0] |
   *      3           2         1         0  -> address
   * | d[31:24] | d[23:16] | d[15:8] | d[7:0] |  */

  when(io.func3 === "b010".U && io.EX_MEM_MemWr === 1.U) {
    /** !!!!!!!!!!!!!!!!!!!! STORE WORD !!!!!!!!!!!!!!!!!!!! */
    when(data_offset === "b00".U) {
      // addressing 0,4,8... location of memory, enable all mask bits to write 32 bits data.
      io.coreDccmReq.bits.activeByteLane := "b1111".U
      // data_be_o -> 1111
    } .elsewhen(data_offset === "b01".U) {
      // addressing 1,5,9... location of memory, enable 3 MSB mask bits to write data, ignore the first byte location
      io.coreDccmReq.bits.activeByteLane := "b1110".U
      // data_be_o -> 1110
    } .elsewhen(data_offset === "b10".U) {
      // addressing 2,6,10... location of memory, enable first and second bytes to write data. Ignore the first two bytes
      io.coreDccmReq.bits.activeByteLane := "b1100".U
      // data_be_o -> 1100
    } .elsewhen(data_offset === "b11".U) {
      // addressing 3,7,11... location of memory, enable just 1 MSB bit to write data. Ignore 3 LSB bytes.
      io.coreDccmReq.bits.activeByteLane := "b1000".U
      // data_be_o -> 1000
    } .otherwise {
      io.coreDccmReq.bits.activeByteLane := "b1111".U
    }

  } .elsewhen(io.func3 === "b001".U && io.EX_MEM_MemWr === 1.U) {
    /** !!!!!!!!!!!!!!!!!!!! STORE HALF WORD !!!!!!!!!!!!!!!!!!!! */
    when(data_offset === "b00".U) {
      // addressing 0,4,8... location of memory, enable two LSB mask bits to write 16 bits data.
      io.coreDccmReq.bits.activeByteLane := "b0011".U
      // data_be_o -> 0011
    } .elsewhen(data_offset === "b01".U) {
      // addressing 1,5,9... location of memory, enable 1st and 2nd MSB mask bits to write data, ignore the first and last byte location
      io.coreDccmReq.bits.activeByteLane := "b0110".U
      // data_be_o -> 0110
    } .elsewhen(data_offset === "b10".U) {
      // addressing 2,6,10... location of memory, enable 2 MSB mask bits to write data. Ignore the first two bytes
      io.coreDccmReq.bits.activeByteLane := "b1100".U
      // data_be_o -> 1100
    } .elsewhen(data_offset === "b11".U) {
      // addressing 3,7,11... location of memory, enable just 1 MSB bit to write data. Ignore 3 LSB bytes.
      io.coreDccmReq.bits.activeByteLane := "b1000".U
      // data_be_o -> 1000
    } .otherwise {
      io.coreDccmReq.bits.activeByteLane := "b1111".U
    }
  } .elsewhen(io.func3 === "b000".U && io.EX_MEM_MemWr === 1.U) {
    /** !!!!!!!!!!!!!!!!!!!! STORE BYTE !!!!!!!!!!!!!!!!!!!! */
    when(data_offset === "b00".U) {
      // addressing 0,4,8... location of memory, enable zeroth LSB mask bit to write 8 bits data.
      io.coreDccmReq.bits.activeByteLane := "b0001".U
      // data_be_o -> 0001
    } .elsewhen(data_offset === "b01".U) {
      // addressing 1,5,9... location of memory, enable 1st  mask bit to write 8 bits data, ignore the zeroth, second and last byte location
      io.coreDccmReq.bits.activeByteLane := "b0010".U
      // data_be_o -> 0010
    } .elsewhen(data_offset === "b10".U) {
      // addressing 2,6,10... location of memory, enable 2 MSB mask bits to write data. Ignore the first two bytes
      io.coreDccmReq.bits.activeByteLane := "b0100".U
      // data_be_o -> 0100
    } .elsewhen(data_offset === "b11".U) {
      // addressing 3,7,11... location of memory, enable just 1 MSB bit to write data. Ignore 3 LSB bytes.
      io.coreDccmReq.bits.activeByteLane := "b1000".U
      // data_be_o -> 1000
    } .otherwise {
      io.coreDccmReq.bits.activeByteLane := "b1111".U
    }
  } .otherwise {
    io.coreDccmReq.bits.activeByteLane := DontCare
  }

  /** ******************************************END****************************************************** */


  /** |||||||||||||||||||||||||||| ALIGNING DATA TO BE WRITTEN INTO THE MEMORY |||||||||||||||||||||||||||| */

  /** ******************************************START****************************************************** */

  when(data_offset === "b00".U) {
    data_wdata(0) := io.EX_MEM_rs2(7,0).asSInt()
    data_wdata(1) := io.EX_MEM_rs2(15,8).asSInt()
    data_wdata(2) := io.EX_MEM_rs2(23,16).asSInt()
    data_wdata(3) := io.EX_MEM_rs2(31,24).asSInt()
  } .elsewhen(data_offset === "b01".U) {
    data_wdata(0) := io.EX_MEM_rs2(31,24).asSInt()
    data_wdata(1) := io.EX_MEM_rs2(7,0).asSInt()
    data_wdata(2) := io.EX_MEM_rs2(15,8).asSInt()
    data_wdata(3) := io.EX_MEM_rs2(23,16).asSInt()
  } .elsewhen(data_offset === "b10".U) {
    data_wdata(0) := io.EX_MEM_rs2(31,24).asSInt()
    data_wdata(1) := io.EX_MEM_rs2(23,16).asSInt()
    data_wdata(2) := io.EX_MEM_rs2(7,0).asSInt()
    data_wdata(3) := io.EX_MEM_rs2(15,8).asSInt()
  } .elsewhen(data_offset === "b11".U) {
    data_wdata(0) := io.EX_MEM_rs2(31,24).asSInt()
    data_wdata(1) := io.EX_MEM_rs2(23,16).asSInt()
    data_wdata(2) := io.EX_MEM_rs2(15,8).asSInt()
    data_wdata(3) := io.EX_MEM_rs2(7,0).asSInt()
  } .otherwise {
    data_wdata(0) := io.EX_MEM_rs2(7,0).asSInt()
    data_wdata(1) := io.EX_MEM_rs2(15,8).asSInt()
    data_wdata(2) := io.EX_MEM_rs2(23,16).asSInt()
    data_wdata(3) := io.EX_MEM_rs2(31,24).asSInt()
  }

  /** ******************************************END****************************************************** */


  /** |||||||||||||||||||||||||||| GENERATING WRITE/READ REQUEST TO TILELINK |||||||||||||||||||||||||||| */

  /** ******************************************START****************************************************** */

  io.coreDccmReq.bits.addrRequest := io.EX_MEM_alu_output.asUInt()
  when(io.coreDccmReq.ready && (io.EX_MEM_MemWr===1.U))
  {
    io.coreDccmReq.valid := true.B
    io.coreDccmReq.bits.dataRequest := data_wdata.asUInt()
  } .elsewhen(io.coreDccmReq.ready && (io.EX_MEM_MemRd === 1.U)) {
    io.coreDccmReq.valid := true.B
    io.coreDccmReq.bits.dataRequest := DontCare
  } .otherwise
    {
      io.coreDccmReq.valid := false.B
      io.coreDccmReq.bits.dataRequest := DontCare
    }


  /** ******************************************END****************************************************** */


  /** |||||||||||||||||||||||||||| READING DATA FROM MEMORY IN NEXT CLOCK CYCLE |||||||||||||||||||||||||||| */

  /** ******************************************START****************************************************** */
  // TODO lh,lhu,lb,lbu working correctly for word-aligned addresses only, need to align it with un-aligned addresses as well
  when(io.coreDccmRsp.valid && io.EX_MEM_MemRd === 1.U)
  {
    load_unit.io.en := true.B   // enabling the load_unit to now read and sign extend the valid data
//    io.data_out     := io.data_rdata_i
    io.data_out     := load_unit.io.LoadData
  }
    .otherwise
    {
      io.data_out     := DontCare
    }


  /** ******************************************END****************************************************** */



  /** |||||||||||||||||||||||||||| PASSING SIGNALS TO THE MEM/WB REGISTER |||||||||||||||||||||||||||| */

  /** ******************************************START****************************************************** */
  io.coreDccmReq.bits.isWrite := io.EX_MEM_MemWr
  io.alu_output := io.EX_MEM_alu_output

  io.rd_sel_out := io.EX_MEM_rd_sel
  io.ctrl_RegWr_out := io.EX_MEM_RegWr
  io.ctrl_CsrWen_out := io.EX_MEM_CsrWe
  io.ctrl_MemRd_out := io.EX_MEM_MemRd
  io.ctrl_MemToReg_out := io.EX_MEM_MemToReg
//  io.csr_addr_out := io.EX_MEM_csr_addr
//  io.csr_op_out := io.EX_MEM_csr_op
  io.csr_data_out := io.EX_MEM_csr_data
  /** ******************************************END****************************************************** */

}