import caravan.bus.native.{NativeConfig, NativeRequest, NativeResponse}
import caravan.bus.wishbone.WishboneConfig
import chisel3._
import org.scalatest._
import chiseltest._
import chiseltest.ChiselScalatestTester
import chiseltest.internal.VerilatorBackendAnnotation
import chiseltest.experimental.TestOptionBuilder._
import org.scalatest.FreeSpec
class HarnessTester extends FreeSpec with ChiselScalatestTester {
  def getFile: Option[String] = {
    if (scalaTestContext.value.get.configMap.contains("memFile")) {
      Some(scalaTestContext.value.get.configMap("memFile").toString)
    } else {
      None
    }
  }
  "it should work" in {
    implicit val config = NativeConfig(32,32)
    val programFile = getFile
    test(new Harness(new NativeRequest(), new NativeResponse(), programFile)).withAnnotations(Seq(VerilatorBackendAnnotation)) {c =>
      c.clock.step(100)
    }
  }
}
