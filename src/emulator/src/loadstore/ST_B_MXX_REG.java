package emulator.src.loadstore;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class ST_B_MXX_REG extends Instruction {
	public ST_B_MXX_REG(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setArgument32();
		super.setAssembler("st.b [0x%08x]" + ", " + this.sdest);
	}

	@Override
	public void exec(CpuContext ctx) {
		int fixedAddr = fix(this.argument);
		short content = ctx.memory[fixedAddr / 2];
		if ((fixedAddr & 1) == 0) {
			content &= 0x00ff; 
			content |= ctx.getReg(this.dest).val << 8;
		} else {
			content &= 0xff00; 
			content |= ctx.getReg(this.dest).val & 255;
		}
		ctx.memory[fixedAddr / 2] = content;
		
		ctx.pc.val += 6;
		updateViewer(ctx, fix(this.argument), content);
	}
}
