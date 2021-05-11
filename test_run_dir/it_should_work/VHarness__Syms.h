// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Symbol table internal header
//
// Internal details; most calling programs do not need this header,
// unless using verilator public meta comments.

#ifndef _VHarness__Syms_H_
#define _VHarness__Syms_H_

#include "verilated.h"

// INCLUDE MODULE CLASSES
#include "VHarness.h"
#include "VHarness___024unit.h"

// SYMS CLASS
class VHarness__Syms : public VerilatedSyms {
  public:
    
    // LOCAL STATE
    const char* __Vm_namep;
    bool __Vm_didInit;
    
    // SUBCELL STATE
    VHarness*                      TOPp;
    
    // CREATORS
    VHarness__Syms(VHarness* topp, const char* namep);
    ~VHarness__Syms() {}
    
    // METHODS
    inline const char* name() { return __Vm_namep; }
    
} VL_ATTR_ALIGNED(64);

#endif  // guard
