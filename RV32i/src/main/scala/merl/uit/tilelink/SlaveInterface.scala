package merl.uit.tilelink
import chisel3._
import chisel3.util._

class SlaveInterface extends Module with ChannelD {
    val io = IO(new Bundle {
        // The data returned from the calling module in case of a GET request.
        val ackDataFromModule = Input(UInt(32.W))
        // A simple ack signal indicating data is written successfully in a PutFullData request.
        val ackFromModule = Input(UInt(1.W))
        // The Channel A inputs from Master interface
        val a_address = Input(UInt(32.W))
        val a_data = Input(UInt(32.W))
        val a_opcode = Input(UInt(3.W))
        val a_source = Input(UInt(32.W))
        val a_valid = Input(Bool())
        // The Channel D outputs from the Slave interface to the Master interface
        val d_opcode = Output(UInt(3.W))
        val d_source = Output(UInt(32.W))
        val d_denied = Output(Bool())
        val d_valid = Output(Bool())
        val d_data = Output(UInt(32.W))
        // The slave interface output received from Channel A to the calling module
        val addr_out = Output(UInt(32.W))
        val data_out = Output(UInt(32.W))
    })

    var d_opcode = 0.U
    var d_source = io.a_source
    var d_denied = false.B
    var d_data = 0.U
    var d_valid = false.B

    io.d_opcode := 0.U
    io.d_source := 0.U
    io.d_denied := false.B
    io.d_valid := false.B
    io.d_data := 0.U
    io.addr_out := 0.U
    io.data_out := 0.U

    val idle :: get_transaction :: putFullData_transaction :: Nil = Enum(3)
    val stateReg = RegInit(idle)
    switch(stateReg) {
      is (idle) {
        when(io.a_opcode === 4.U && io.a_valid === 0.U && d_source === io.a_source) {
          stateReg := get_transaction
        } .elsewhen(io.a_opcode === 0.U && io.a_valid === true.B && io.a_source === d_source) {
          stateReg := putFullData_transaction
        }
      }
      is (get_transaction) {
        io.addr_out := io.a_address
        io.data_out := 0.U
        when(io.ackDataFromModule =/= 0.U) {
          d_opcode = 1.U
          d_source = io.a_source
          d_denied = false.B
          d_valid = true.B
          d_data = io.ackDataFromModule
          io.d_opcode := d_opcode
          io.d_source := d_source
          io.d_denied := d_denied
          io.d_valid := d_valid
          io.d_data := d_data
          stateReg := idle
        } .otherwise {
          d_opcode = 1.U
          d_source = io.a_source
          d_denied = true.B
          d_valid = false.B
          d_data = 0.U
          io.d_opcode := d_opcode
          io.d_source := d_source
          io.d_denied := d_denied
          io.d_valid := d_valid
          io.d_data := d_data
          stateReg := idle
        }
      }
      is (putFullData_transaction) {
        io.addr_out := io.a_address
        io.data_out := io.a_data
        when(io.ackFromModule === 1.U) {
          d_opcode = 0.U
          d_source = io.a_source
          d_denied = false.B
          d_valid = false.B
          d_data = 0.U
          io.d_opcode := d_opcode
          io.d_source := d_source
          io.d_denied := d_denied
          io.d_valid := d_valid
          io.d_data := d_data
          stateReg := idle
        }

      }
    }




}