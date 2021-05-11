// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design internal header
// See VHarness.h for the primary calling header

#ifndef _VHarness___024unit_H_
#define _VHarness___024unit_H_

#include "verilated.h"

class VHarness__Syms;

//----------

VL_MODULE(VHarness___024unit) {
  public:
    
    // PORTS
    
    // LOCAL SIGNALS
    
    // LOCAL VARIABLES
    
    // INTERNAL VARIABLES
  private:
    VHarness__Syms* __VlSymsp;  // Symbol table
  public:
    
    // PARAMETERS
    
    // CONSTRUCTORS
  private:
    VL_UNCOPYABLE(VHarness___024unit);  ///< Copying not allowed
  public:
    VHarness___024unit(const char* name="TOP");
    ~VHarness___024unit();
    
    // API METHODS
    
    // INTERNAL METHODS
    void __Vconfigure(VHarness__Syms* symsp, bool first);
  private:
    void _ctor_var_reset() VL_ATTR_COLD;
} VL_ATTR_ALIGNED(128);

#endif // guard
