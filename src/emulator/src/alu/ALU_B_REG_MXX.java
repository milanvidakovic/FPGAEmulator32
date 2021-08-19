package emulator.src.alu;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ALU_B_REG_MXX extends Instruction {
	int type;

	public ALU_B_REG_MXX(short[] memory, int addr, int src, int dest, int type) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler(Instruction.getTypeStr(type) + this.sdest + ", [0x%08x]");
		this.type = type;
	}
	
	@Override
	public void exec(CpuContext ctx) {
		int old_a = ctx.getReg(this.dest).val;
		long res = 0;

		int fixedAddr = fix(this.argument);
		short operand;
		if ((fixedAddr & 1) == 0)
			operand = (byte)((ctx.memory[fixedAddr / 2] >> 8) & 0xFF);
		else
			operand = (byte)((ctx.memory[fixedAddr / 2] & 255) & 0xFF);
		
		switch (type) {
		case ADD_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) + (operand & 0xffffffffL)); break;
		case SUB_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) + (-operand & 0xffffffffL)); break;
		case AND_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) & (operand & 0xffffffffL)); break;
		case OR_B : res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) | (operand & 0xffffffffL)); break;
		case XOR_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) ^ (operand & 0xffffffffL)); break;
		case SHL_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) << (operand & 0xffffffffL)); break;
		case SHR_B: res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) >>> (operand & 0xffffffffL)); break;
		case MUL_B:	res = (byte)((ctx.getReg(this.dest).val & 0xffffffffL) * (operand & 0xffffffffL)); 
					ctx.h.val = (byte)((res & 0xffffffff00000000L) >> 32);
					break;
		case DIV_B: res = (byte)(ctx.getReg(this.dest).val / operand ); 
					ctx.h.val = (byte)(ctx.getReg(this.dest).val % operand);
					break;
		default: throw new RuntimeException("Unsupported operation type: " + type);
		}
		
		ctx.getReg(this.dest).val = (int)res;
		markFlags(res, ctx.getReg(this.dest).val, ctx);
		markOverflow(old_a, operand, ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 6;
	}
}
