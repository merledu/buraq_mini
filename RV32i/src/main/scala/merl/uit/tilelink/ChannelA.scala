package merl.uit.tilelink
import chisel3._

trait ChannelA {
  var a_opcode: UInt
  val a_param: UInt = 0.U(3.W)
  // a_size represents the size of the resulting AccessAckData response message.
  val a_size: UInt = 2.U      // width in 2^width bytes. Width of 2 will give 2^2 = 4 bytes -> 32 bits.
  var a_source: UInt
  // the address to get from in bytes.
  var a_address: UInt
  // a_mask represents the byte lanes to read from. 0xf -> (1111 in binary) sets all the byte lanes to high resulting in 32 bits to read.
  val a_mask: UInt = 0xf.U(4.W)
  val a_corrupt: UInt = 0.U
  var a_data: UInt
  // receivers raise the channel ready signal to indicate their ability to accept a beat.
  var a_ready: Bool
  // the sender of a beat raises the channel valid signal to offer the availability of the beat on the channel.
  var a_valid: Bool

//  def sendGetRequest(a_addr: UInt) = {
//    a_opcode = 4.U
//    a_address = a_addr
//    a_ready = true
//    a_valid = true
//    val chnlD = new ChannelD
//    chnlD.setDReady
//    chnlD.parseGetFromChannelA(a_opcode, a_address, a_valid)
//
//  }

}