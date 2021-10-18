package emulator.src.floatingpoint;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class PIX extends Instruction {
	public PIX(short[] memory, int addr, int src, int dest) {
		super(memory, addr, src, dest);
		super.setAssembler("pix ");
	}

	@Override
	public void exec(CpuContext ctx) {
		int _x = ctx.getReg(0).val;
		int _y = ctx.getReg(1).val;
		short _color = (short)ctx.getReg(2).val;
		int _dest = ctx.getReg(3).val;
		int _w = ctx.getReg(4).val;
		int _h = ctx.getReg(5).val;

		if(_x < 0 || _y < 0 || _x >= _w * 2 || _y >= _h)
		{
			return;
		}
		int location = (_x >> 2)*2 + _y * _w;
		int p = _dest + location;
		short xOffset = (short)((_x % 4) * 4);
		short xOffsetMask =(short)(0xF000 >> xOffset);
		short pColor = (short) ((_color << (12-xOffset)) & xOffsetMask);
		short bgColor = (short)((~xOffsetMask) & ctx.memory[(p) / 2]);
		ctx.memory[(p)/2] = (short)(pColor | bgColor);
		updateViewer(ctx, p, (short)(pColor | bgColor));
		
		ctx.pc.val += 2;
	}
}
