package merl.uit.tilelink
import chisel3._
import chisel3.util._
/**
* Usually this module is used by the Agent who wants to communicate with another
  Agent over tilelink bus protocol and is a Master.
*/

/** 
* * This is the master interface according to the Tilelink specification
*/

/** 
* * MasterInterfaceIO is the bundle class which declares the input/output for the master interface module
*/
class MasterInterfaceIO extends Bundle {
  // State control inputs. This tells the master interface either the module wants to perform
  // a Get or a PullFullData operation.
  val memRd = Input(UInt(1.W))
  val memWrt = Input(UInt(1.W))

  // Inputs from the parent module
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
  // Data output to be used by the parent module
  val data = Output(UInt(32.W))
}

class MasterInterface(sourceId: UInt, forFetch: Boolean) extends Module with ChannelA {
  val io = IO(new MasterInterfaceIO)

  // Initializing default values for the channel properties
  var a_opcode = 0.U
  var a_source = sourceId
  var a_address = 0.U
  var a_data = 0.U
  var a_ready = false.B
  var a_valid = false.B

  // Initializing output pins of this module
  io.a_address := 0.U
  io.a_data := 0.U
  io.a_opcode := 0.U
  io.a_source := 0.U
  io.a_valid := false.B
  io.data := 0.U

  // Checking if the parent Agent instantiating this master interface is for the fetch purpose 
  if (forFetch) {
    // setting the GET opcode
    a_opcode = 4.U
    // source id for this agent
    a_source = sourceId
    // the address through which the parent wants to fetch new instruction
    a_address = io.addr_in
    // data on channel A will be 0 because for fetch no data payload is required
    a_data = 0.U
    // setting the ready to be true, which means the channel is ready for accepting response
    a_ready = true.B
    // valid is false since there is no meaning data on the channel since there is no data payload
    a_valid = false.B
    // setting the output pins of this interface according to the channel properties
    io.a_address := a_address
    io.a_data := a_data
    io.a_opcode := a_opcode
    io.a_source := a_source
    io.a_valid := a_valid

    // checking for d_opcode, d_denied and d_valid coming from the slave interface of another Agent
    // d_opcode = 1 reflects that there is a data in response of the GET from master
    // d_denied = false shows that the request was not denied
    // d_valid = true shows that the data on the channel is valid and can be read by the master.
    when(io.d_opcode === 1.U && io.d_denied === false.B && io.d_valid === true.B) {
      // setting the data pin of the master interface with the data from the channel D to
      // be used by the parent module i.e the fetched instruction.
      io.data := io.d_data
    }
  } else {
    // If we get here it means that the parent is using this interface not for fetching purpose
    when(io.memRd === 1.U && io.memWrt === 0.U) {
      // GET request
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
      when(io.d_opcode === 1.U && io.d_denied === false.B && io.d_valid === true.B && a_source === io.d_source) {
        io.data := io.d_data
      }
    } .elsewhen(io.memRd === 0.U && io.memWrt === 1.U) {
      // PUT request same as PutFullData in the SiFive doc
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
      // If the Channel D has a opcode of 0 it indicates the acknowledge for access.
      // d_denied = false shows that the slave was able to work on the request
      // a_source = d_source validates if the request is from the agent through which it intends to talk
      when(io.d_opcode === 0.U && io.d_denied === false.B && a_source === io.d_source) {
        // setting the channel D data on the output pin for the use of the parent
        // though this value can be ignored since d_opcode = 0 shows AccessAck which does not hold 
        // any relevant data
        io.data := io.d_data
      }
    }
  }

//  if(!forFetch) {
//
//    val idle :: get_transaction :: put_transaction :: ack_wait :: ackWithData_wait :: Nil  = Enum(5)
//    val stateReg = RegInit(idle)
//
//
//    switch(stateReg) {
//        is (idle) {
//        when(io.memRd === 1.U && io.memWrt === 0.U && io.uartEn === 0.U) {
//            stateReg := get_transaction
//        } .elsewhen((io.memRd === 0.U) && (io.memWrt === 1.U || io.uartEn === 1.U)) {
//            stateReg := put_transaction
//        }
//        }
//        is (get_transaction) {
//        a_opcode = 4.U
//        a_source = sourceId
//        a_address = io.addr_in
//        a_data = 0.U
//        a_ready = true.B
//        a_valid = false.B
//        io.a_address := a_address
//        io.a_data := a_data
//        io.a_opcode := a_opcode
//        io.a_source := a_source
//        io.a_valid := a_valid
//        stateReg := ackWithData_wait
//        }
//        is (ackWithData_wait) {
//        when(io.d_opcode === 1.U && io.d_denied === false.B && io.d_valid === true.B && a_source === io.d_source) {
//            io.data := io.d_data
//            stateReg := idle
//        }
//        }
//        is (put_transaction) {
//        a_opcode = 0.U
//        a_source = sourceId
//        a_address = io.addr_in
//        a_data = io.data_in
//        a_ready = true.B
//        a_valid = true.B
//        io.a_address := a_address
//        io.a_data := a_data
//        io.a_opcode := a_opcode
//        io.a_source := a_source
//        io.a_valid := a_valid
//        stateReg := ack_wait
//        }
//        is (ack_wait) {
//        when(io.d_opcode === 0.U && io.d_denied === false.B && a_source === io.d_source) {
//            io.data := 1.U  // sending 1 as output as an Ack signal which means the data is written successfully
//            stateReg := idle
//        }
//        }
//    }
//
//  } else {
//
//
//
//        a_opcode = 4.U
//        a_source = sourceId
//        a_address = io.addr_in
//        a_data = 0.U
//        a_ready = true.B
//        a_valid = false.B
//        io.a_address := a_address
//        io.a_data := a_data
//        io.a_opcode := a_opcode
//        io.a_source := a_source
//        io.a_valid := a_valid
//
//
//
//
//        when(io.d_opcode === 1.U && io.d_denied === false.B && io.d_valid === true.B) {
//          io.data := io.d_data
//
//        }
//
//  }

}