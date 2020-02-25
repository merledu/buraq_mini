package datapath
import chisel3._
import merl.uit.tilelink.MasterInterface

class LoadStoreBusControllerIO extends Bundle {
  // The inputs coming from the memory stage
  val isLoad = Input(Bool())
  val isStore = Input(Bool())
  val rs2 = Input(UInt(32.W))
  val addr = Input(UInt(32.W))

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

  val readGPIO = Output(UInt(1.W))
  val setGPIO = Output(UInt(1.W))
  val setGPIOData = Output(UInt(32.W))
  val stallForMMIO = Output(UInt(1.W))
}

class LoadStoreBusController extends Module {
  val io = IO(new LoadStoreBusControllerIO)

  when(io.addr === "h200".U) {
    // MMI/O operation
    when(io.isLoad === 1.U && io.isStore === 0.U) {
      // ********** GPIO READ OPERATION ***************** //
      val reg = RegInit(0.U(1.W))
      io.readGPIO := 1.U
      io.setGPIO := 0.U
      io.stallForMMIO := 1.U
      val master = Module(new MasterInterface(sourceId = 2.U, forFetch = false))
      master.io.memRd := 0.U
      master.io.memWrt := 1.U
      master.io.addr_in := io.addr
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
      io.data := 0.U
      reg := 1.U
      when(reg === 1.U) {
        master.io.memRd := 1.U
        master.io.memWrt := 0.U
        master.io.addr_in := io.addr
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
        io.data := master.io.data
      }

    } .elsewhen(io.isLoad === 0.U && io.isStore === 1.U) {
      // ********** GPIO WRITE OPERATION ***************** //

      io.readGPIO := 0.U
      io.setGPIO := 1.U
      io.setGPIOData := io.rs2
      io.stallForMMIO := 1.U
      val master = Module(new MasterInterface(sourceId = 2.U, forFetch = false))
      master.io.memRd := 0.U
      master.io.memWrt := 1.U
      master.io.addr_in := io.addr
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
    }
  } .otherwise {
    io.stallForMMIO := 0.U
    val master = Module(new MasterInterface(sourceId = 3.U, forFetch = false))
    master.io.memRd := io.isLoad
    master.io.memWrt := io.isStore
    master.io.addr_in := io.addr
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

    io.readGPIO := 0.U
    io.setGPIO := 0.U
    io.setGPIOData := 0.U
    io.data := master.io.data
  }

}
