import caravan.bus.wishbone.WishboneConfig
import chisel3.iotesters.{Driver, TesterOptionsManager}
import csrs.CsrRegisterFileTests
import main.scala.core.csrs.CsrRegisterFile
import utils.TutorialRunner

object Launcher {
  val examples = Map(

    "Harness" -> { (manager: TesterOptionsManager) => {
      implicit val config = WishboneConfig(10, 32)
      Driver.execute(() => new Harness(), manager) {
        (c) => new HarnessTester(c)
      }
    }
    }
  )
  def main(args: Array[String]): Unit = {
    TutorialRunner("examples", examples, args)
  }
}
