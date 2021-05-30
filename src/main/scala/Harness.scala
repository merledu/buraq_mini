import chisel3._
import buraq_mini.core.Core
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}
import caravan.bus.native.{NativeConfig, NativeRequest, NativeResponse}
import chisel3.stage.ChiselStage
import jigsaw.rams.fpga.BlockRam


class Harness[A <: AbstrRequest, B <: AbstrResponse, T <: BusConfig]
             (gen: A, gen1: B, programFile: Option[String])
             (implicit val config: T) extends Module {
  val io = IO(new Bundle {
  })

  val imem = Module(BlockRam.createNonMaskableRAM(programFile, bus=config, rows=1024))
  val dmem = Module(BlockRam.createMaskableRAM(bus=config, rows=1024))
  val core = Module(new Core(gen, gen1))

  imem.io.req <> core.io.imemReq
  core.io.imemRsp <> imem.io.rsp

  dmem.io.req <> core.io.dmemReq
  dmem.io.rsp <> core.io.dmemRsp

  core.io.stall_core_i := false.B
  core.io.irq_external_i := false.B


}

object HarnessDriver extends App {
  implicit val config = NativeConfig(32, 32)
  println((new ChiselStage).emitVerilog(new Harness(new NativeRequest(), new NativeResponse(), None)))
}
