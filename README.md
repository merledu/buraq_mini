# Buraq-Mini: An In-Order Fully Bypassed RISC-V Core

Buraq-Mini is a 5 stage pipelined processor which is made in Chisel from scratch and is used inside the "Ibtida SoC" which is selected for the first open-source ASIC tapeout in the Google + Efabless Open MPW Shuttle program.
\
\
![block diagram](https://github.com/merledu/Buraq-mini/blob/master/docs/buraq-mini.png)
\
\
First of all get started by cloning this repository on your machine.  
```bash
git clone -b decoupled-interface https://github.com/merledu/Buraq-mini.git
```
Then initialise Caravan as a submodule. This respository does not compile standalone. Caravan is used to provide a bus interface to the core for talking
with the memories.
```bash
cd Buraq-mini
git submodule init
git submodule update
```
After initialising the submodule go inside the `RV32i` folder and run all the tests to check whether everything is properly setup or not.
```bash
cd RV32i
sbt "test"
```
If there are no errors then Buraq-mini is correctly setup on your machine.
