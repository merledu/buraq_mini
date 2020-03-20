package core
import chisel3.iotesters.PeekPokeTester
import peripherals.UartController
class UartControllerTests(c: UartController) extends PeekPokeTester (c){

    poke(c.io.isStalled, true)
    poke(c.io.rxd, 1)
    step(10)
    // 32 bit data payload
    pokeUart(0x13.toInt)
    pokeUart(0x01.toInt)
    pokeUart(0xf0.toInt)
    pokeUart(0x00.toInt)

    // Another 32-bit data payload
    pokeUart(0x23.toInt)
    pokeUart(0x22.toInt)
    pokeUart(0x20.toInt)
    pokeUart(0x20.toInt)

    // The data payload which tells the uart that program to be loaded is finished
    pokeUart(0xff.toInt)
    pokeUart(0x0f.toInt)
    pokeUart(0x00.toInt)
    pokeUart(0x00.toInt)


    step(5)



    def pokeUart(value: Int): Unit = {

        // start bit
        poke(c.io.rxd, 0)
        step(3)
        // 8 data bits
        for (i <- 0 until 8) {
            poke(c.io.rxd, (value >> i) & 0x01)
            step(3)
        }
        // stop bit
        poke(c.io.rxd, 1)
        step(2)

        
    }


}
