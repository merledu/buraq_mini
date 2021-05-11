// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See VHarness.h for the primary calling header

#include "VHarness.h"
#include "VHarness__Syms.h"


//--------------------
// STATIC VARIABLES


//--------------------

VL_CTOR_IMP(VHarness) {
    VHarness__Syms* __restrict vlSymsp = __VlSymsp = new VHarness__Syms(this, name());
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Reset internal values
    
    // Reset structure values
    _ctor_var_reset();
}

void VHarness::__Vconfigure(VHarness__Syms* vlSymsp, bool first) {
    if (0 && first) {}  // Prevent unused
    this->__VlSymsp = vlSymsp;
}

VHarness::~VHarness() {
    delete __VlSymsp; __VlSymsp=NULL;
}

//--------------------


void VHarness::eval() {
    VL_DEBUG_IF(VL_DBG_MSGF("+++++TOP Evaluate VHarness::eval\n"); );
    VHarness__Syms* __restrict vlSymsp = this->__VlSymsp;  // Setup global symbol table
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
#ifdef VL_DEBUG
    // Debug assertions
    _eval_debug_assertions();
#endif  // VL_DEBUG
    // Initialize
    if (VL_UNLIKELY(!vlSymsp->__Vm_didInit)) _eval_initial_loop(vlSymsp);
    // Evaluate till stable
    int __VclockLoop = 0;
    QData __Vchange = 1;
    do {
	VL_DEBUG_IF(VL_DBG_MSGF("+ Clock loop\n"););
	_eval(vlSymsp);
	if (VL_UNLIKELY(++__VclockLoop > 100)) {
	    // About to fail, so enable debug to see what's not settling.
	    // Note you must run make with OPT=-DVL_DEBUG for debug prints.
	    int __Vsaved_debug = Verilated::debug();
	    Verilated::debug(1);
	    __Vchange = _change_request(vlSymsp);
	    Verilated::debug(__Vsaved_debug);
	    VL_FATAL_MT(__FILE__,__LINE__,__FILE__,"Verilated model didn't converge");
	} else {
	    __Vchange = _change_request(vlSymsp);
	}
    } while (VL_UNLIKELY(__Vchange));
}

void VHarness::_eval_initial_loop(VHarness__Syms* __restrict vlSymsp) {
    vlSymsp->__Vm_didInit = true;
    _eval_initial(vlSymsp);
    // Evaluate till stable
    int __VclockLoop = 0;
    QData __Vchange = 1;
    do {
	_eval_settle(vlSymsp);
	_eval(vlSymsp);
	if (VL_UNLIKELY(++__VclockLoop > 100)) {
	    // About to fail, so enable debug to see what's not settling.
	    // Note you must run make with OPT=-DVL_DEBUG for debug prints.
	    int __Vsaved_debug = Verilated::debug();
	    Verilated::debug(1);
	    __Vchange = _change_request(vlSymsp);
	    Verilated::debug(__Vsaved_debug);
	    VL_FATAL_MT(__FILE__,__LINE__,__FILE__,"Verilated model didn't DC converge");
	} else {
	    __Vchange = _change_request(vlSymsp);
	}
    } while (VL_UNLIKELY(__Vchange));
}

//--------------------
// Internal Methods

void VHarness::_initial__TOP__1(VHarness__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::_initial__TOP__1\n"); );
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Variables
    // Begin mtask footprint  all: 
    VL_SIGW(__Vtemp1,223,0,7);
    // Body
    // INITIAL at /Users/mbp/Desktop/buraq-latest/test_run_dir/it_should_work/Harness.BlockRamWithoutMasking.mem.v:13
    __Vtemp1[0U] = 0x2e747874U;
    __Vtemp1[1U] = 0x6d656d31U;
    __Vtemp1[2U] = 0x746f702fU;
    __Vtemp1[3U] = 0x4465736bU;
    __Vtemp1[4U] = 0x6d62702fU;
    __Vtemp1[5U] = 0x6572732fU;
    __Vtemp1[6U] = 0x2f5573U;
    VL_READMEM_W(true,32,1024, 0,7, __Vtemp1, vlTOPp->Harness__DOT__imem_ctrl__DOT__mem
		 ,0,~0);
}

void VHarness::_eval(VHarness__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::_eval\n"); );
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Final
    vlTOPp->__Vclklast__TOP__clock = vlTOPp->clock;
}

void VHarness::_eval_initial(VHarness__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::_eval_initial\n"); );
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    vlTOPp->_initial__TOP__1(vlSymsp);
    vlTOPp->__Vclklast__TOP__clock = vlTOPp->clock;
}

void VHarness::final() {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::final\n"); );
    // Variables
    VHarness__Syms* __restrict vlSymsp = this->__VlSymsp;
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
}

void VHarness::_eval_settle(VHarness__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::_eval_settle\n"); );
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
}

VL_INLINE_OPT QData VHarness::_change_request(VHarness__Syms* __restrict vlSymsp) {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::_change_request\n"); );
    VHarness* __restrict vlTOPp VL_ATTR_UNUSED = vlSymsp->TOPp;
    // Body
    // Change detection
    QData __req = false;  // Logically a bool
    return __req;
}

#ifdef VL_DEBUG
void VHarness::_eval_debug_assertions() {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::_eval_debug_assertions\n"); );
    // Body
    if (VL_UNLIKELY((clock & 0xfeU))) {
	Verilated::overWidthError("clock");}
    if (VL_UNLIKELY((reset & 0xfeU))) {
	Verilated::overWidthError("reset");}
}
#endif // VL_DEBUG

void VHarness::_ctor_var_reset() {
    VL_DEBUG_IF(VL_DBG_MSGF("+    VHarness::_ctor_var_reset\n"); );
    // Body
    clock = VL_RAND_RESET_I(1);
    reset = VL_RAND_RESET_I(1);
    { int __Vi0=0; for (; __Vi0<1024; ++__Vi0) {
	    Harness__DOT__imem_ctrl__DOT__mem[__Vi0] = VL_RAND_RESET_I(32);
    }}
}
