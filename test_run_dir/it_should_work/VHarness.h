// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Primary design header
//
// This header should be included by all source files instantiating the design.
// The class here is then constructed to instantiate the design.
// See the Verilator manual for examples.

#ifndef _VHarness_H_
#define _VHarness_H_

#include "verilated.h"

class VHarness__Syms;

//----------

VL_MODULE(VHarness) {
  public:
    
    // PORTS
    // The application code writes and reads these signals to
    // propagate new values into/out from the Verilated model.
    // Begin mtask footprint  all: 
    VL_IN8(clock,0,0);
    VL_IN8(reset,0,0);
    
    // LOCAL SIGNALS
    // Internals; generally not touched by application code
    // Begin mtask footprint  all: 
    VL_SIG(Harness__DOT__imem_ctrl__DOT__mem[1024],31,0);
    
    // LOCAL VARIABLES
    // Internals; generally not touched by application code
    // Begin mtask footprint  all: 
    VL_SIG8(__Vclklast__TOP__clock,0,0);
    
    // INTERNAL VARIABLES
    // Internals; generally not touched by application code
    VHarness__Syms* __VlSymsp;  // Symbol table
    
    // PARAMETERS
    // Parameters marked /*verilator public*/ for use by application code
    
    // CONSTRUCTORS
  private:
    VL_UNCOPYABLE(VHarness);  ///< Copying not allowed
  public:
    /// Construct the model; called by application code
    /// The special name  may be used to make a wrapper with a
    /// single model invisible with respect to DPI scope names.
    VHarness(const char* name="TOP");
    /// Destroy the model; called (often implicitly) by application code
    ~VHarness();
    
    // API METHODS
    /// Evaluate the model.  Application must call when inputs change.
    void eval();
    /// Simulation complete, run final blocks.  Application must call on completion.
    void final();
    
    // INTERNAL METHODS
  private:
    static void _eval_initial_loop(VHarness__Syms* __restrict vlSymsp);
  public:
    void __Vconfigure(VHarness__Syms* symsp, bool first);
  private:
    static QData _change_request(VHarness__Syms* __restrict vlSymsp);
    void _ctor_var_reset() VL_ATTR_COLD;
  public:
    static void _eval(VHarness__Syms* __restrict vlSymsp);
  private:
#ifdef VL_DEBUG
    void _eval_debug_assertions();
#endif // VL_DEBUG
  public:
    static void _eval_initial(VHarness__Syms* __restrict vlSymsp) VL_ATTR_COLD;
    static void _eval_settle(VHarness__Syms* __restrict vlSymsp) VL_ATTR_COLD;
    static void _initial__TOP__1(VHarness__Syms* __restrict vlSymsp) VL_ATTR_COLD;
} VL_ATTR_ALIGNED(128);

#endif // guard
