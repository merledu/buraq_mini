package merl.uit.tilelink
import chisel3._
// This is the Channel D properties that every Slave Interface has to adhere to
trait ChannelD {

  var d_opcode: UInt
  val d_param: UInt = 0.U(2.W)
  val d_size: UInt = 2.U
  var d_source: UInt
  val d_sink: UInt = 0.U
  val d_corrupt: UInt = 0.U
  var d_denied: Bool
  var d_data: UInt
  var d_valid: Bool


}