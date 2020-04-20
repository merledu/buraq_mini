package merl.uit.tilelink
import chisel3._
import chisel3.util._
/**
* Usually this module is used by the Agent who wants to communicate with another
  Agent over tilelink bus protocol and is a Slave.
*/

/** 
* This is the slave interface according to the Tilelink specification
*/

class SlaveInterface(forFetch: Boolean) extends Module with ChannelD {
    val io = IO(new Bundle {
        // The data provided by the parent module (usually by accessing memory)
        // This will be the data which is then sent through the d_data of Channel D
        val ackDataFromModule = Input(UInt(32.W))
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
        // The addr received from Channel A sent to the parent module (usually for accessing memory)
        val addr_out = Output(UInt(32.W))
        // The data received from Channel A sent to the parent module (usually for writing to the memory)
        val data_out = Output(UInt(32.W))
        // The enable signal for the data memory incase of a store instruction
        val en = Output(UInt(1.W))
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
    io.en := 0.U
  if (!forFetch) {
//    val idle :: get_transaction :: putFullData_transaction :: Nil = Enum(3)
//    val stateReg = RegInit(idle)
//    switch(stateReg) {
//      is(idle) {
//        when(io.a_opcode === 4.U && io.a_valid === 0.U && d_source === io.a_source) {
//          stateReg := get_transaction
//        }.elsewhen(io.a_opcode === 0.U && io.a_valid === true.B && io.a_source === d_source) {
//          stateReg := putFullData_transaction
//        }
//      }
//      is(get_transaction) {
//        io.addr_out := io.a_address
//        io.data_out := 0.U
//        when(io.ackDataFromModule =/= 0.U) {
//          d_opcode = 1.U
//          d_source = io.a_source
//          d_denied = false.B
//          d_valid = true.B
//          d_data = io.ackDataFromModule
//          io.d_opcode := d_opcode
//          io.d_source := d_source
//          io.d_denied := d_denied
//          io.d_valid := d_valid
//          io.d_data := d_data
//          stateReg := idle
//        }.otherwise {
//          d_opcode = 1.U
//          d_source = io.a_source
//          d_denied = true.B
//          d_valid = false.B
//          d_data = 0.U
//          io.d_opcode := d_opcode
//          io.d_source := d_source
//          io.d_denied := d_denied
//          io.d_valid := d_valid
//          io.d_data := d_data
//          stateReg := idle
//        }
//      }
//      is(putFullData_transaction) {
//        io.addr_out := io.a_address
//        io.data_out := io.a_data
//        when(io.ackFromModule === 1.U) {
//          d_opcode = 0.U
//          d_source = io.a_source
//          d_denied = false.B
//          d_valid = false.B
//          d_data = 0.U
//          io.d_opcode := d_opcode
//          io.d_source := d_source
//          io.d_denied := d_denied
//          io.d_valid := d_valid
//          io.d_data := d_data
//          stateReg := idle
//        }
//
//      }
//    }
    // Checking the a_opcode for finding out what the master wants the slave to do
    // a_opcode -> 4 means there is a Get from master which means the slave has
    // to access the memory and return the data with the signal AccessAckData
    when(io.a_opcode === 4.U && io.a_valid === 0.U && d_source === io.a_source) {
      // GET
      // setting the output pins to be used by the parent module for accessing memory
      io.addr_out := io.a_address
      io.data_out := 0.U
      io.en := 0.U
      // checking if the data is available by the parent module after accessing memory
      when(io.ackDataFromModule =/= 0.U) {
        // setting the opcode for AccessAckData
        d_opcode = 1.U
        // sending the same source back to identify that the communication is between the agents that they intend to be
        d_source = io.a_source
        // setting denied false since the request was not denied and the slave is sending the data
        d_denied = false.B
        // setting valid true since data is present
        d_valid = true.B
        // setting the data to be what was received from the parent
        d_data = io.ackDataFromModule
        // setting the output pins according to the channel D properties
        io.d_opcode := d_opcode
        io.d_source := d_source
        io.d_denied := d_denied
        io.d_valid := d_valid
        io.d_data := d_data
      }.otherwise {
        // it means the data was not present since we got 0 from the parent
        d_opcode = 1.U
        d_source = io.a_source
        d_denied = true.B
        // setting the d_valid false since there is no valid data on the channel now
        d_valid = false.B
        // setting the data to be 0
        d_data = 0.U
        io.d_opcode := d_opcode
        io.d_source := d_source
        io.d_denied := d_denied
        io.d_valid := d_valid
        io.d_data := d_data
      }

    }.elsewhen(io.a_opcode === 0.U && io.a_valid === true.B && io.a_source === d_source) {
      // PUT
      // setting the output pins to be used by the parent module for accessing memory
      io.addr_out := io.a_address
      io.data_out := io.a_data
      io.en := 1.U
      // we got 0 from the parent here, it means that data was written into the memory
      when(io.ackDataFromModule === 0.U) {
        // setting the opcode of AccessAck
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
      }
    }

  } else {
        // If the slave is used for responding to the fetch master interface
        io.addr_out := io.a_address
        io.data_out := 0.U
        io.en := 0.U
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


    }
  }
}