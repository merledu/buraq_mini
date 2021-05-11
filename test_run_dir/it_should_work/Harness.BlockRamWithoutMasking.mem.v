module BindsTo_0_BlockRamWithoutMasking(
  input         clock,
  input         reset,
  output        io_req_ready,
  input         io_req_valid,
  input  [31:0] io_req_bits_addrRequest,
  input  [31:0] io_req_bits_dataRequest,
  input         io_req_bits_isWrite,
  output        io_rsp_valid,
  output [31:0] io_rsp_bits_dataResponse
);

initial begin
  $readmemh("/Users/mbp/Desktop/mem1.txt", BlockRamWithoutMasking.mem);
end
                      endmodule

bind BlockRamWithoutMasking BindsTo_0_BlockRamWithoutMasking BindsTo_0_BlockRamWithoutMasking_Inst(.*);