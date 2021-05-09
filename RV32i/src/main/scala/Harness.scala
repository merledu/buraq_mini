import chisel3._
import _root_.core.Core
import caravan.bus.wishbone.{DummyMemController, WishboneConfig, WishboneDevice, WishboneHost}
import jigsaw.rams.fpga.BlockRam


class Harness(programFile: Option[String])(implicit val config: WishboneConfig) extends Module {
  val io = IO(new Bundle {
  })

  val wb_imem_host = Module(new WishboneHost())
  val wb_imem_slave = Module(new WishboneDevice())
  val wb_dmem_host = Module(new WishboneHost())
  val wb_dmem_slave = Module(new WishboneDevice())
//  val imem_ctrl = Module(new DummyMemController(programFile))
  val imem_ctrl = Module(BlockRam.createNonMaskableRAM(programFile, bus=config, rows=1024))
  val dmem_ctrl = Module(BlockRam.createMaskableRAM(None, bus=config, rows=1024))
  val core = Module(new Core())

  wb_imem_host.io.wbMasterTransmitter <> wb_imem_slave.io.wbMasterReceiver
  wb_imem_slave.io.wbSlaveTransmitter <> wb_imem_host.io.wbSlaveReceiver

  wb_dmem_host.io.wbMasterTransmitter <> wb_dmem_slave.io.wbMasterReceiver
  wb_dmem_slave.io.wbSlaveTransmitter <> wb_dmem_host.io.wbSlaveReceiver

  wb_imem_host.io.reqIn <> core.io.imemReq
  core.io.imemRsp <> wb_imem_host.io.rspOut
  wb_imem_slave.io.reqOut <> imem_ctrl.io.req
  wb_imem_slave.io.rspIn <> imem_ctrl.io.rsp

  wb_dmem_host.io.reqIn <> core.io.dmemReq
  core.io.dmemRsp <> wb_dmem_host.io.rspOut
  wb_dmem_slave.io.reqOut <> dmem_ctrl.io.req
  wb_dmem_slave.io.rspIn <> dmem_ctrl.io.rsp

  core.io.stall_core_i := false.B
  core.io.irq_external_i := false.B


}
