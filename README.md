# Buraq-Mini: An In-Order Fully Bypassed RISC-V Core

Buraq-Mini is a 5 stage pipelined processor which is made in Chisel from scratch and is used inside the "Ibtida SoC" which is selected for the first open-source ASIC tapeout in the Google + Efabless Open MPW Shuttle program.
\
\
![block diagram](https://github.com/merledu/Buraq-mini/blob/master/docs/buraq-mini.png)
\
\
First of all get started by cloning this repository on your machine.  
```bash
git clone -b decoupled-interface https://github.com/merledu/buraq-mini.git
```
There is a _harness_ in the project: `src/main/scala/Harness.scala` which is used for testing the core. It instantiates the core, uses [Jigsaw](https://github.com/merledu/jigsaw) to create block rams that act as instruction memory and data memory, and use [Caravan](https://github.com/merledu/caravan) to create a Wishbone bus interconnect for communication between the core and these memories.

In order to properly build the project you need to recursively clone the nested projects _Caravan_ and _Jigsaw_ which are integrated inside as Git Submodules. 
Here are the steps for properly cloning the submodules and verify if everything is running fine:

```bash
cd buraq_mini
git submodule update --init --recursive
sbt test
```
If there are no errors then Buraq-mini is correctly setup on your machine.
