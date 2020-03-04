package datapath
import chisel3._
import chisel3.util._
import merl.uit.tilelink.MasterInterface

class LoadStoreBusControllerIO extends Bundle {
  // The inputs coming from the memory stage
  val isLoad = Input(Bool())
  val isStore = Input(Bool())
  val rs2 = Input(UInt(32.W))
  // The address is now 12 bits. The same 10 bits are used for addressing and the 2 LSB bits are used to indicate
  // which GPIO needs to be read in case of a read operation (Store instruction)
  // 00 -> gpio pin 1
  // 01 -> gpio pin2
  // 10 -> gpio pin3
  // 11 -> gpio pin 4
  val addr = Input(UInt(12.W))

  // The GPIO values coming from the gpio 1,2,3,4 input pins to the core and then to the load store bus controller
  val GPIO_values = Input(UInt(4.W))

  // Channel A properties going out to the slave interface inside DCCM Controller
  val a_address = Output(UInt(32.W))
  val a_data = Output(UInt(32.W))
  val a_opcode = Output(UInt(3.W))
  val a_source = Output(UInt(32.W))
  val a_valid = Output(Bool())

  // Channel D properties coming in from the slave interface inside DCCM Controller
  val d_opcode = Input(UInt(3.W))
  val d_source = Input(UInt(32.W))
  val d_denied = Input(Bool())
  val d_valid = Input(Bool())
  val d_data = Input(UInt(32.W))

  // The resultant output of the module.
  val data = Output(UInt(32.W))

  // The GPIO control signals setting the control of gpio controller, indicating what tasks need to be done
  val readGPIO = Output(UInt(1.W))
  val setGPIO = Output(UInt(1.W))
  // The data that needs to be set on the GPIO pins
  val setGPIOData = Output(UInt(32.W))
  // This signal is fed to the staller which is used for stalling the pipeline
  val stallForMMIO = Output(UInt(1.W))
}

class LoadStoreBusController extends Module {
  val io = IO(new LoadStoreBusControllerIO)
  val master = Module(new MasterInterface(sourceId = 2.U, forFetch = false))
  // Extracting the same 10 bits from the address bits for memory addressing
  val address = io.addr(11,2)
  // Extracting the two LSB bits for finding out which GPIO pins to set
  val gpioPin = io.addr(1,0)
  // We have set the 128th memory row for gpio in pins. The data stored here will be for the gpio in pins.
  when(address === 128.U) {     // GPIO in pins address map in memory
    // Setting the default values for tilelink master interface
    master.io.memRd := 0.U
    master.io.memWrt := 0.U
    master.io.addr_in := 0.U
    master.io.data_in := 0.U
    master.io.d_valid := 0.U
    master.io.d_source := 0.U
    master.io.d_opcode := 0.U
    master.io.d_denied := 0.U
    master.io.d_data := 0.U

    io.a_address := 0.U
    io.a_data := 0.U
    io.a_opcode := 0.U
    io.a_source := 0.U
    io.a_valid := 0.U

    io.stallForMMIO := 0.U
    io.readGPIO := 0.U
    io.setGPIO := 0.U
    io.setGPIOData := 0.U
    io.data := 0.U

    val idle :: writeGPIOValueToMemory :: readGPIOValueFromMemory :: Nil = Enum(3)
    val stateReg = RegInit(idle)
    switch(stateReg) {
      is(idle) {
        when(io.isLoad === 1.U && io.isStore === 0.U) {
          // isLoad indicates the instruction is load instruction which needs to read value from gpio pins
          master.io.memRd := 0.U
          master.io.memWrt := 0.U
          master.io.addr_in := 0.U
          master.io.data_in := 0.U
          master.io.d_valid := 0.U
          master.io.d_source := 0.U
          master.io.d_opcode := 0.U
          master.io.d_denied := 0.U
          master.io.d_data := 0.U

          io.a_address := 0.U
          io.a_data := 0.U
          io.a_opcode := 0.U
          io.a_source := 0.U
          io.a_valid := 0.U

          // Setting the staller to stall the pipeline
          io.stallForMMIO := 1.U
          // Indicating gpio controller to read from gpio registers
          io.readGPIO := 1.U
          io.setGPIO := 0.U
          io.setGPIOData := 0.U
          io.data := 0.U
          stateReg := writeGPIOValueToMemory
        }
          .otherwise {
          master.io.memRd := 0.U
          master.io.memWrt := 0.U
          master.io.addr_in := 0.U
          master.io.data_in := 0.U
          master.io.d_valid := 0.U
          master.io.d_source := 0.U
          master.io.d_opcode := 0.U
          master.io.d_denied := 0.U
          master.io.d_data := 0.U

          io.a_address := 0.U
          io.a_data := 0.U
          io.a_opcode := 0.U
          io.a_source := 0.U
          io.a_valid := 0.U

          io.stallForMMIO := 0.U
          io.readGPIO := 0.U
          io.setGPIO := 0.U
          io.setGPIOData := 0.U
          io.data := 0.U
          stateReg := idle
        }
      }

      is(writeGPIOValueToMemory) {
        // In this state we read the values from the GPIO pins and write them into memory
        // We also keep the pipeline stalled at the same time.
        io.stallForMMIO := 1.U
        io.readGPIO := 1.U
        io.setGPIO := 0.U

        master.io.memRd := 0.U
        master.io.memWrt := 1.U
        master.io.addr_in := address
        master.io.data_in := io.GPIO_values

        io.a_address := master.io.a_address
        io.a_data := master.io.a_data
        io.a_opcode := master.io.a_opcode
        io.a_source := master.io.a_source
        io.a_valid := master.io.a_valid

        master.io.d_valid := io.d_valid
        master.io.d_source := io.d_source
        master.io.d_opcode := io.d_opcode
        master.io.d_denied := io.d_denied
        master.io.d_data := io.d_data
        io.data := master.io.data
        io.setGPIOData := 0.U
        stateReg := readGPIOValueFromMemory
      }
      is(readGPIOValueFromMemory) {
        // After writing to memory, now we read from the memory and send the data to the memory stage.
        io.stallForMMIO := 0.U
        io.readGPIO := 0.U
        io.setGPIO := 0.U

        master.io.memRd := 1.U
        master.io.memWrt := 0.U
        master.io.addr_in := address
        master.io.data_in := 0.U

        io.a_address := master.io.a_address
        io.a_data := master.io.a_data
        io.a_opcode := master.io.a_opcode
        io.a_source := master.io.a_source
        io.a_valid := master.io.a_valid

        master.io.d_valid := io.d_valid
        master.io.d_source := io.d_source
        master.io.d_opcode := io.d_opcode
        master.io.d_denied := io.d_denied
        master.io.d_data := io.d_data
        // Here we get the complete data of GPIO pins from the memory. We then extract which bit we want
        // according to the two bits in the address field.
        switch(gpioPin) {
          is("b00".U) {
            // reading 1st bit of data since address shows to read from 1st pin of gpio
            io.data := master.io.data(0)
          }
          is("b01".U) {
            // reading 2nd bit of data since address shows to read from 2nd pin of gpio
            io.data := master.io.data(1)
          }
          is("b10".U) {
            // reading 3rd bit of data since address shows to read from 3rd pin of gpio
            io.data := master.io.data(2)
          }
          is("b11".U) {
            // reading 4th bit of data since address shows to read from 4th pin of gpio
            io.data := master.io.data(3)
          }
        }

        io.setGPIOData := 0.U
        stateReg := idle
      }

    }
  } .elsewhen(address === 129.U) {
    // The 129th row in memory is set for gpio pin out data
    master.io.memRd := 0.U
    master.io.memWrt := 0.U
    master.io.addr_in := 0.U
    master.io.data_in := 0.U
    master.io.d_valid := 0.U
    master.io.d_source := 0.U
    master.io.d_opcode := 0.U
    master.io.d_denied := 0.U
    master.io.d_data := 0.U

    io.a_address := 0.U
    io.a_data := 0.U
    io.a_opcode := 0.U
    io.a_source := 0.U
    io.a_valid := 0.U

    io.stallForMMIO := 0.U
    io.readGPIO := 0.U
    io.setGPIO := 0.U
    io.setGPIOData := 0.U
    io.data := 0.U
    val idle :: writeDataToGPIOMemory :: readFromMemoryAndSetGPIO :: Nil = Enum(3)
    val stateReg = RegInit(idle)
    switch(stateReg) {
      is(idle) {
        when(io.isLoad === 0.U && io.isStore === 1.U) {
          master.io.memRd := 0.U
          master.io.memWrt := 0.U
          master.io.addr_in := 0.U
          master.io.data_in := 0.U
          master.io.d_valid := 0.U
          master.io.d_source := 0.U
          master.io.d_opcode := 0.U
          master.io.d_denied := 0.U
          master.io.d_data := 0.U

          io.a_address := 0.U
          io.a_data := 0.U
          io.a_opcode := 0.U
          io.a_source := 0.U
          io.a_valid := 0.U

          io.stallForMMIO := 1.U
          io.readGPIO := 0.U
          io.setGPIO := 1.U
          io.setGPIOData := 0.U
          io.data := 0.U
          stateReg := writeDataToGPIOMemory
        }
      }
      is(writeDataToGPIOMemory) {
        // Writing the data received from EX/MEM pipeline register to the memory
        io.stallForMMIO := 1.U
        io.readGPIO := 0.U
        io.setGPIO := 1.U

        master.io.memRd := 0.U
        master.io.memWrt := 1.U
        master.io.addr_in := address
        master.io.data_in := io.rs2

        io.a_address := master.io.a_address
        io.a_data := master.io.a_data
        io.a_opcode := master.io.a_opcode
        io.a_source := master.io.a_source
        io.a_valid := master.io.a_valid

        master.io.d_valid := io.d_valid
        master.io.d_source := io.d_source
        master.io.d_opcode := io.d_opcode
        master.io.d_denied := io.d_denied
        master.io.d_data := io.d_data
        io.data := master.io.data
        io.setGPIOData := 0.U
        stateReg := readFromMemoryAndSetGPIO
      }
      is(readFromMemoryAndSetGPIO) {
        // After writing the data to memory we read it and then set it on GPIO pins
        io.stallForMMIO := 0.U
        io.readGPIO := 0.U
        io.setGPIO := 1.U

        master.io.memRd := 1.U
        master.io.memWrt := 0.U
        master.io.addr_in := address
        master.io.data_in := 0.U

        io.a_address := master.io.a_address
        io.a_data := master.io.a_data
        io.a_opcode := master.io.a_opcode
        io.a_source := master.io.a_source
        io.a_valid := master.io.a_valid

        master.io.d_valid := io.d_valid
        master.io.d_source := io.d_source
        master.io.d_opcode := io.d_opcode
        master.io.d_denied := io.d_denied
        master.io.d_data := io.d_data
        io.data := 0.U
        io.setGPIOData := master.io.data
        stateReg := idle
      }
    }
  }
    .otherwise {
        // This is for normal load and stores instruction
        io.stallForMMIO := 0.U
        io.readGPIO := 0.U
        io.setGPIO := 0.U

          master.io.memRd := io.isLoad
          master.io.memWrt := io.isStore
          master.io.addr_in := address
          master.io.data_in := io.rs2

        io.a_address := master.io.a_address
        io.a_data := master.io.a_data
        io.a_opcode := master.io.a_opcode
        io.a_source := master.io.a_source
        io.a_valid := master.io.a_valid

        master.io.d_valid := io.d_valid
        master.io.d_source := io.d_source
        master.io.d_opcode := io.d_opcode
        master.io.d_denied := io.d_denied
        master.io.d_data := io.d_data
        io.data := master.io.data
        io.setGPIOData := 0.U
      }

}
