package merl.uit.tilelink
import chisel3._
import chisel3.util._

class MasterInterface(sourceId: UInt, forFetch: Boolean) extends Module with ChannelA {
  val io = IO(new Bundle {
    // State control inputs from the core
    val memRd = Input(UInt(1.W))
    val memWrt = Input(UInt(1.W))
    val uartEn = Input(UInt(1.W))
    // Inputs from the calling Module
    val addr_in = Input(UInt(32.W))
    val data_in = Input(UInt(32.W))
    // Channel D inputs from Slave interface
    val d_opcode = Input(UInt(3.W))
    val d_source = Input(UInt(32.W))
    val d_denied = Input(Bool())
    val d_valid = Input(Bool())
    val d_data = Input(UInt(32.W))
    // Channel A outputs to Slave interface
    val a_address = Output(UInt(32.W))
    val a_data = Output(UInt(32.W))
    val a_opcode = Output(UInt(3.W))
    val a_source = Output(UInt(32.W))
    val a_valid = Output(Bool())
    // Data output from Master interface to be used by the calling module.
    val data = Output(UInt(32.W))

  })
  var a_opcode = 0.U
  var a_source = sourceId
  var a_address = 0.U
  var a_data = 0.U
  var a_ready = false.B
  var a_valid = false.B

  io.a_address := 0.U
  io.a_data := 0.U
  io.a_opcode := 0.U
  io.a_source := 0.U
  io.a_valid := false.B
  io.data := 0.U

  if(!forFetch) {

    val idle :: get_transaction :: put_transaction :: ack_wait :: ackWithData_wait :: Nil  = Enum(5)
    val stateReg = RegInit(idle)


    switch(stateReg) {
        is (idle) {
        when(io.memRd === 1.U && io.memWrt === 0.U && io.uartEn === 0.U) {
            stateReg := get_transaction
        } .elsewhen((io.memRd === 0.U) && (io.memWrt === 1.U || io.uartEn === 1.U)) {
            stateReg := put_transaction
        }
        }
        is (get_transaction) {
        a_opcode = 4.U
        a_source = sourceId
        a_address = io.addr_in
        a_data = 0.U
        a_ready = true.B
        a_valid = false.B
        io.a_address := a_address
        io.a_data := a_data
        io.a_opcode := a_opcode
        io.a_source := a_source
        io.a_valid := a_valid
        stateReg := ackWithData_wait
        }
        is (ackWithData_wait) {
        when(io.d_opcode === 1.U && io.d_denied === false.B && io.d_valid === true.B && a_source === io.d_source) {
            io.data := io.d_data
            stateReg := idle
        }
        }
        is (put_transaction) {
        a_opcode = 0.U
        a_source = sourceId
        a_address = io.addr_in
        a_data = io.data_in
        a_ready = true.B
        a_valid = true.B
        io.a_address := a_address
        io.a_data := a_data
        io.a_opcode := a_opcode
        io.a_source := a_source
        io.a_valid := a_valid
        stateReg := ack_wait
        }
        is (ack_wait) {
        when(io.d_opcode === 0.U && io.d_denied === false.B && a_source === io.d_source) {
            io.data := 1.U  // sending 1 as output as an Ack signal which means the data is written successfully
            stateReg := idle
        }
        }
    }
  
  } else {
    val get_transaction :: ackWithData_wait :: Nil  = Enum(2)
    val stateReg = RegInit(get_transaction)
    switch (stateReg) {
      is (get_transaction) {
        a_opcode = 4.U
        a_source = sourceId
        a_address = io.addr_in
        a_data = 0.U
        a_ready = true.B
        a_valid = false.B
        io.a_address := a_address
        io.a_data := a_data
        io.a_opcode := a_opcode
        io.a_source := a_source
        io.a_valid := a_valid
        stateReg := ackWithData_wait

      }
      is (ackWithData_wait) {
        when(io.d_opcode === 1.U && io.d_denied === false.B && io.d_valid === true.B && a_source === io.d_source) {
          io.data := io.d_data
          stateReg := get_transaction
        }
      }
    }
  }

}