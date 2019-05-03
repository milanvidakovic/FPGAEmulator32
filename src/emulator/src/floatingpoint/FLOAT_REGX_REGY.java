package emulator.src.floatingpoint;

import java.nio.ByteBuffer;

import emulator.engine.CpuContext;
import emulator.src.Instruction;

public class FLOAT_REGX_REGY extends Instruction {
	int type;

	public FLOAT_REGX_REGY(short[] memory, int addr, int src, int dest, int type) {
		super(memory, addr, src, dest);
		super.setAssembler(Instruction.getTypeStr(type) + this.sdest + ", " + this.ssrc);
		this.type = type;
	}

	@Override
	public void exec(CpuContext ctx) {
//		short old_a = ctx.getReg(this.dest).val;
		float res = 0;
		switch (type) {
		case FADD:
			res = int2float(ctx.getReg(this.dest).val) + int2float(ctx.getReg(this.src).val); 
			break;
		case FSUB:
			res = int2float(ctx.getReg(this.dest).val) - int2float(ctx.getReg(this.src).val); 
			break;
		case FMUL:
			res = int2float(ctx.getReg(this.dest).val) * int2float(ctx.getReg(this.src).val); 
			break;
		case FDIV:
			res = int2float(ctx.getReg(this.dest).val) / int2float(ctx.getReg(this.src).val); 
			break;
		default:
			throw new RuntimeException("Unsupported operation type: " + type);
		}
		ctx.getReg(this.dest).val = (int)float2int(res);
//		markFlags(res, ctx.getReg(this.dest).val, ctx);
//		markOverflow(old_a, ctx.getReg(this.src).val, ctx.getReg(this.dest).val, ctx);
		ctx.pc.val += 2;
	}

	public float int2float(int i) {
		byte[] b = int2bytes(i);
 	    ByteBuffer buf = ByteBuffer.wrap(b);
	    return buf.getFloat();
	}
	
	private byte[] int2bytes(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) (i >>> 24);
		b[1] = (byte) ((i & 0x00ff0000) >>> 16);
		b[2] = (byte) ((i & 0x0000ff00) >>> 8);
		b[3] = (byte) (i & 0xff);
		return b;
	}
	
	private long float2int(float f) {
		byte[] b = float2bytes(f);
		return bytes2int(b);
	}
	private long bytes2int(byte[] b) {
		long r = fixByte(b[0]) << 24L;
		r |= fixByte(b[1]) << 16L;
		r |= fixByte(b[2]) << 8L; 
		r |= fixByte(b[3]);
		return r;
	}

	private int fixByte(byte b) {
		if (b < 0) return 256 + b;
		return b;
	}
	private byte[] float2bytes(float value) {
	    int intBits =  Float.floatToIntBits(value);
	    return new byte[] {
	      (byte) (intBits >>> 24), (byte) (intBits >>> 16), (byte) (intBits >>> 8), (byte) (intBits) };
	}
}
