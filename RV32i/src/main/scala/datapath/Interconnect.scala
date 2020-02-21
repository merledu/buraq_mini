package datapath
import chisel3._

class Interconnect extends Module {
  val io = IO(new Bundle {
    val inst_out = Output(UInt(32.W))
  })

  val core = Module(new Core)
  val iccmController = Module(new ICCMController)
  val dccmController = Module(new DCCMController)
  val iccm = Module(new InstructionMem)
  val dccm = Module(new DataMem)

//   This register is used to hold the value of stall from staller. It is used to avoid combinational loop in chisel
//   since pc sends output to fetch bus controller which sends it's output to staller which in return sends
//   it's output to pc again, all this combinational logic creates a loop which is not allowed in chisel. So we
//   insert a register to break this combinational loop

//  stallReg := staller.io.stall
//  pc.io.stall := stallReg
//  staller.io.isStall := fetchBusController.io.stall

//  when(fetchBusController.io.inst =/= 0.U) {
//    pc.io.in := pc.io.pc4
//  } .otherwise {
//    pc.io.in := pc.io.out
//  }


  core.io.iccm_d_valid := iccmController.io.d_valid
  core.io.iccm_d_source := iccmController.io.d_source
  core.io.iccm_d_opcode := iccmController.io.d_opcode
  core.io.iccm_d_denied := iccmController.io.d_denied
  core.io.iccm_d_data := iccmController.io.d_data

  core.io.dccm_d_valid := dccmController.io.d_valid
  core.io.dccm_d_source := dccmController.io.d_source
  core.io.dccm_d_opcode := dccmController.io.d_opcode
  core.io.dccm_d_denied := dccmController.io.d_denied
  core.io.dccm_d_data := dccmController.io.d_data

  iccmController.io.a_valid := core.io.iccm_a_valid
  iccmController.io.a_source := core.io.iccm_a_source
  iccmController.io.a_opcode := core.io.iccm_a_opcode
  iccmController.io.a_data := core.io.iccm_a_data
  iccmController.io.a_address := core.io.iccm_a_address

  dccmController.io.a_valid := core.io.dccm_a_valid
  dccmController.io.a_source := core.io.dccm_a_source
  dccmController.io.a_opcode := core.io.dccm_a_opcode
  dccmController.io.a_data := core.io.dccm_a_data
  dccmController.io.a_address := core.io.dccm_a_address

  iccm.io.wrAddr := iccmController.io.addr_out
  iccmController.io.ackDataFromModule := iccm.io.readData
  io.inst_out :=  iccmController.io.ackDataFromModule

  dccm.io.enable := dccmController.io.en
  dccm.io.memAddress := dccmController.io.addr_out
  dccm.io.memData := dccmController.io.data_out.asSInt
  dccmController.io.data_in := dccm.io.memOut.asUInt

}
