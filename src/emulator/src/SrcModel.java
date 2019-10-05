package emulator.src;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import emulator.EmulatorMain;
import emulator.src.alu.ALU_B_REGX_MREGY_XX;
import emulator.src.alu.ALU_B_REG_XX;
import emulator.src.alu.ALU_S_REGX_MREGY_XX;
import emulator.src.alu.ALU_S_REG_XX;
import emulator.src.alu.ALU_W_REGX_MREGY_XX;
import emulator.src.alu.ALU_W_REGX_REGY;
import emulator.src.alu.ALU_W_REG_XX;
import emulator.src.alu.NEG_B_MREG_XX;
import emulator.src.alu.NEG_S_MREG_XX;
import emulator.src.alu.NEG_W_MREG_XX;
import emulator.src.alu.NEG_W_REG;
import emulator.src.call.CALLC_XX;
import emulator.src.call.CALLG_XX;
import emulator.src.call.CALLNC_XX;
import emulator.src.call.CALLNO_XX;
import emulator.src.call.CALLNP_XX;
import emulator.src.call.CALLNZ_XX;
import emulator.src.call.CALLO_XX;
import emulator.src.call.CALLP_XX;
import emulator.src.call.CALLSE_XX;
import emulator.src.call.CALLZ_XX;
import emulator.src.call.CALL_XX;
import emulator.src.cmpinv.CMP_B_REGX_MREGY_XX;
import emulator.src.cmpinv.CMP_B_REG_XX;
import emulator.src.cmpinv.CMP_S_REGX_MREGY_XX;
import emulator.src.cmpinv.CMP_S_REG_XX;
import emulator.src.cmpinv.CMP_W_REGX_MREGY_XX;
import emulator.src.cmpinv.CMP_W_REGX_REGY;
import emulator.src.cmpinv.CMP_W_REG_XX;
import emulator.src.cmpinv.INV_B_MREG_XX;
import emulator.src.cmpinv.INV_S_MREG_XX;
import emulator.src.cmpinv.INV_W_MREG_XX;
import emulator.src.cmpinv.INV_W_REG;
import emulator.src.floatingpoint.FLOAT_REGX_REGY;
import emulator.src.incdec.DEC_B_MREG_XX;
import emulator.src.incdec.DEC_S_MREG;
import emulator.src.incdec.DEC_S_MREG_XX;
import emulator.src.incdec.DEC_W_MREG_XX;
import emulator.src.incdec.DEC_W_REG;
import emulator.src.incdec.INC_B_MREG_XX;
import emulator.src.incdec.INC_S_MREG;
import emulator.src.incdec.INC_S_MREG_XX;
import emulator.src.incdec.INC_W_MREG_XX;
import emulator.src.incdec.INC_W_REG;
import emulator.src.jmp.JC_XX;
import emulator.src.jmp.JG_XX;
import emulator.src.jmp.JNC_XX;
import emulator.src.jmp.JNO_XX;
import emulator.src.jmp.JNP_XX;
import emulator.src.jmp.JNZ_XX;
import emulator.src.jmp.JO_XX;
import emulator.src.jmp.JP_XX;
import emulator.src.jmp.JSE_XX;
import emulator.src.jmp.JZ_XX;
import emulator.src.jmp.J_XX;
import emulator.src.loadstore.LD_B_REGX_MREGY;
import emulator.src.loadstore.LD_B_REGX_MREGY_XX;
import emulator.src.loadstore.LD_B_REG_MXX;
import emulator.src.loadstore.LD_S_REGX_MREGY;
import emulator.src.loadstore.LD_S_REGX_MREGY_XX;
import emulator.src.loadstore.LD_S_REG_MXX;
import emulator.src.loadstore.LD_W_REGX_MREGY;
import emulator.src.loadstore.LD_W_REGX_MREGY_XX;
import emulator.src.loadstore.LD_W_REG_MXX;
import emulator.src.loadstore.ST_B_MREGX_REGY;
import emulator.src.loadstore.ST_B_MREGX_XX_REGY;
import emulator.src.loadstore.ST_B_MXX_REG;
import emulator.src.loadstore.ST_S_MREGX_REGY;
import emulator.src.loadstore.ST_S_MREGX_XX_REGY;
import emulator.src.loadstore.ST_S_MXX_REG;
import emulator.src.loadstore.ST_W_MREGX_REGY;
import emulator.src.loadstore.ST_W_MREGX_XX_REGY;
import emulator.src.loadstore.ST_W_MXX_REG;
import emulator.src.nopmovinpushrethaltswap.HALT;
import emulator.src.nopmovinpushrethaltswap.IN_REG_XX;
import emulator.src.nopmovinpushrethaltswap.IRET;
import emulator.src.nopmovinpushrethaltswap.IRQ;
import emulator.src.nopmovinpushrethaltswap.MOV_B_REG_XX;
import emulator.src.nopmovinpushrethaltswap.MOV_S_REG_XX;
import emulator.src.nopmovinpushrethaltswap.MOV_W_REGX_REGY;
import emulator.src.nopmovinpushrethaltswap.MOV_W_REGX_REGY_XX;
import emulator.src.nopmovinpushrethaltswap.MOV_W_REG_XX;
import emulator.src.nopmovinpushrethaltswap.NOP;
import emulator.src.nopmovinpushrethaltswap.OUT_XX_REG;
import emulator.src.nopmovinpushrethaltswap.POP_REG;
import emulator.src.nopmovinpushrethaltswap.PUSH_REG;
import emulator.src.nopmovinpushrethaltswap.PUSH_XX;
import emulator.src.nopmovinpushrethaltswap.RET;
import emulator.src.nopmovinpushrethaltswap.SWAP_REGX_REGY;

public class SrcModel extends AbstractTableModel {
	private static final long serialVersionUID = 8062097745546805165L;

	public String[] columnNames = { "Breakpoint", "Addr", "Content", "Assembler" };
	public List<Instruction> lines = new ArrayList<Instruction>();
	public Instruction[] addr_instr = new Instruction[100000];
	public short[] memory;

	public SrcModel(short[] memory) {
		this.memory = memory;
	}

	public SrcModel(String fileName, short[] memory) {
		this.memory = memory;
		
		if (fileName.endsWith(".bin")) {
			FileInputStream in;
			try {
				in = new FileInputStream(fileName);

				byte[] buffer = new byte[100000];
				in.read(buffer);

				parse(buffer);
				disassm();

				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			BufferedReader in;
			try {
				in = new BufferedReader(new FileReader(fileName));
				// in.readLine(); // skip first line
				String s = in.readLine();
				in.close();
				parse(s);
				disassm();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void parse(byte[] buffer) {
		int t, t1, t2;
		for (int i = 0; i < this.memory.length / 2; i++) {
			t1 = buffer[i * 2];
			if (t1 < 0) {
				t1 = 256 + t1;
			}
			t = t1 << 8;
			t2 = buffer[i * 2 + 1];
			if (t2 < 0) {
				t2 = 256 + t2;
			}
			t += t2;
			this.memory[i] = (short) t;
		}

	}

	private void parse(String s) {
		String[] words = s.split(" ");
		int addr = 0;
		for (int i = 0; i < words.length; i++) {
			String w = words[i];
			memory[addr++] = Short.parseShort(w, 16);
			if (EmulatorMain.DEBUG)
				System.out.println((addr - 1) + ": " + w + " == " + memory[addr - 1]);
		}
	}

	public void disassm() {
		int addr = 0;
		boolean finished = false;
		while (!finished) {
			Instruction instr = getInstruction(memory, addr);
			addr += 2;
			instr.setContent();
			lines.add(instr);
			instr.tableLine = lines.size() - 1;
			if (instr.hasArgument) {
				addr += instr.arglen;
			}
			if (addr == 100000)
				finished = true;
			addr_instr[instr.addr] = instr;
		}
	}

	public Instruction getInstruction(short[] memory, int addr) {
		int ir = (int) memory[addr / 2];
		addr += 2;
		int group = (ir >> 4) & 0xf;
		int src = (ir >> 12) & 0xf;
		int dest = (ir >> 8) & 0xf;
		switch (ir & 0xf) {
		case 0: {
			// NOP/MOV/IN/OUT/PUSH/POP/RET/IRET/HALT GROUP
			switch (group) {
			case 0:
				return new NOP(memory, addr);
			case 1:
				return new MOV_W_REGX_REGY(memory, addr, src, dest);
			case 2:
				return new MOV_S_REG_XX(memory, addr, src, dest);
			case 3:
				return new IN_REG_XX(memory, addr, src, dest);
			case 4:
				return new OUT_XX_REG(memory, addr, src, dest);
			case 5:
				return new PUSH_REG(memory, addr, src, dest);
			case 6:
				return new PUSH_XX(memory, addr, src, dest);
			case 7:
				return new POP_REG(memory, addr, src, dest);
			case 8:
				return new RET(memory, addr, src, dest);
			case 9:
				return new IRET(memory, addr, src, dest);
			case 10:
				return new SWAP_REGX_REGY(memory, addr, src, dest);
			case 11:
				return new MOV_W_REG_XX(memory, addr, src, dest);
			case 12:
				return new IRQ(memory, addr);
			case 13:
				return new MOV_B_REG_XX(memory, addr, src, dest);
			case 14:
				return new MOV_W_REGX_REGY_XX(memory, addr, src, dest);
			case 0xf:
				return new HALT(memory, addr);
			}
		}
		case 1: {
			// JUMP GROUP
			switch (group) {
			case 0:
				return new J_XX(memory, addr, src, dest);
			case 1:
				return new JZ_XX(memory, addr, src, dest);
			case 2:
				return new JNZ_XX(memory, addr, src, dest);
			case 3:
				return new JC_XX(memory, addr, src, dest);
			case 4:
				return new JNC_XX(memory, addr, src, dest);
			case 5:
				return new JO_XX(memory, addr, src, dest);
			case 6:
				return new JNO_XX(memory, addr, src, dest);
			case 7:
				return new JP_XX(memory, addr, src, dest);
			case 8:
				return new JNP_XX(memory, addr, src, dest);
			case 9:
				return new JG_XX(memory, addr, src, dest);
			case 10:
				return new JSE_XX(memory, addr, src, dest);
			}
		}
		case 2: {
			// CALL GROUP
			switch (group) {
			case 0:
				return new CALL_XX(memory, addr, src, dest);
			case 1:
				return new CALLZ_XX(memory, addr, src, dest);
			case 2:
				return new CALLNZ_XX(memory, addr, src, dest);
			case 3:
				return new CALLC_XX(memory, addr, src, dest);
			case 4:
				return new CALLNC_XX(memory, addr, src, dest);
			case 5:
				return new CALLO_XX(memory, addr, src, dest);
			case 6:
				return new CALLNO_XX(memory, addr, src, dest);
			case 7:
				return new CALLP_XX(memory, addr, src, dest);
			case 8:
				return new CALLNP_XX(memory, addr, src, dest);
			case 9:
				return new CALLG_XX(memory, addr, src, dest);
			case 10:
				return new CALLSE_XX(memory, addr, src, dest);
			}
		}
		case 3: {
			// LOAD/STORE GROUP
			switch (group) {
			case 0:
				return new LD_S_REGX_MREGY(memory, addr, src, dest);
			case 1:
				return new LD_S_REG_MXX(memory, addr, src, dest);
			case 2:
				return new LD_S_REGX_MREGY_XX(memory, addr, src, dest);
			case 3:
				return new LD_B_REGX_MREGY(memory, addr, src, dest);
			case 4:
				return new LD_B_REG_MXX(memory, addr, src, dest);
			case 5:
				return new LD_B_REGX_MREGY_XX(memory, addr, src, dest);
			case 8:
				return new ST_S_MREGX_REGY(memory, addr, src, dest);
			case 9:
				return new ST_S_MXX_REG(memory, addr, src, dest);
			case 10:
				return new ST_S_MREGX_XX_REGY(memory, addr, src, dest);
			case 11:
				return new ST_B_MREGX_REGY(memory, addr, src, dest);
			case 12:
				return new ST_B_MXX_REG(memory, addr, src, dest);
			case 13:
				return new ST_B_MREGX_XX_REGY(memory, addr, src, dest);
			}
		}
		case 4: {
			// ADD/SUB GROUP
			switch (group) {
			case 0:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.ADD_W);
			case 1:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.ADD_S);
			case 2:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.ADD_W);
			case 3:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.ADD_S);
			case 4:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.ADD_B);
			/*case 5:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.ADD_W);
			case 6:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.ADD);
			case 7:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.ADD_B);*/
			case 8:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.SUB_W);
			case 9:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.SUB_S);
			case 10:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SUB_W);
			case 11:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SUB_S);
			case 12:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SUB_B);
			/*case 13:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SUB_W);
			case 14:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SUB);
			case 15:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SUB_B);*/
			}
		}
		case 5: {
			// AND/OR GROUP
			switch (group) {
			case 0:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.AND_W);
			case 1:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.AND_S);
			case 2:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.AND_W);
			case 3:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.AND_S);
			case 4:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.AND_B);
			/*case 5:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.AND_W);
			case 6:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.AND);
			case 7:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.AND_B);*/
			case 8:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.OR_W);
			case 9:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.OR_S);
			case 10:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR_W);
			case 11:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR_S);
			case 12:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR_B);
			/*case 13:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR_W);
			case 14:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR);
			case 15:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR_B);*/
			}
		}
		case 6: {
			// XOR/NEG GROUP
			switch (group) {
			case 0:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.XOR_W);
			case 1:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.XOR_S);
			case 2:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.XOR_W);
			case 3:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.XOR_S);
			case 4:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.XOR_B);
			/*case 5:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.XOR_W);
			case 6:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.XOR);
			case 7:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.XOR_B);*/
			case 8:
				return new NEG_W_REG(memory, addr, src, dest);
			case 10:
				return new NEG_W_MREG_XX(memory, addr, src, dest);
			case 11:
				return new NEG_S_MREG_XX(memory, addr, src, dest);
			case 12:
				return new NEG_B_MREG_XX(memory, addr, src, dest);
			/*case 13:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR_W);
			case 14:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR);
			case 15:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.OR_B);*/
			}
		}
		case 7: {
			// SHL/SHR GROUP
			switch (group) {
			case 0:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.SHL_W);
			case 1:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.SHL_S);
			case 2:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHL_W);
			case 3:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHL_S);
			case 4:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHL_B);
			/*case 5:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHL_W);
			case 6:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHL);
			case 7:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHL_B);*/
			case 8:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.SHR_W);
			case 9:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.SHR_S);
			case 10:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHR_W);
			case 11:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHR_S);
			case 12:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHR_B);
			/*case 13:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHR_W);
			case 14:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHR);
			case 15:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.SHR_B);*/
			}
		}
		case 8: {
			// MUL/DIV GROUP
			switch (group) {
			case 0:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.MUL_W);
			case 1:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.MUL_S);
			case 2:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.MUL_W);
			case 3:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.MUL_S);
			case 4:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.MUL_B);
			/*case 5:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.MUL_W);
			case 6:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.MUL);
			case 7:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.MUL_B);*/
			case 8:
				return new ALU_W_REGX_REGY(memory, addr, src, dest, Instruction.DIV_W);
			case 9:
				return new ALU_S_REG_XX(memory, addr, src, dest, Instruction.DIV_S);
			case 10:
				return new ALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.DIV_W);
			case 11:
				return new ALU_S_REGX_MREGY_XX(memory, addr, src, dest, Instruction.DIV_S);
			case 12:
				return new ALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.DIV_B);
			/*case 13:
				return new UALU_W_REGX_MREGY_XX(memory, addr, src, dest, Instruction.DIV_W);
			case 14:
				return new UALU_REGX_MREGY_XX(memory, addr, src, dest, Instruction.DIV);
			case 15:
				return new UALU_B_REGX_MREGY_XX(memory, addr, src, dest, Instruction.DIV_B);*/
			}
		}
		case 9: {
			// INC/DEC GROUP
			switch (group) {
			case 0:
				return new INC_W_REG(memory, addr, src, dest);
			case 1:
				return new INC_S_MREG(memory, addr, src, dest);
			case 2:
				return new INC_W_MREG_XX(memory, addr, src, dest);
			case 3:
				return new INC_S_MREG_XX(memory, addr, src, dest);
			case 4:
				return new INC_B_MREG_XX(memory, addr, src, dest);
			/*case 5:
				return new UINC_W_MREG_XX(memory, addr, src, dest);
			case 6:
				return new UINC_MREG_XX(memory, addr, src, dest);
			case 7:
				return new UINC_B_MREG_XX(memory, addr, src, dest);*/
			case 8:
				return new DEC_W_REG(memory, addr, src, dest);
			case 9:
				return new DEC_S_MREG(memory, addr, src, dest);
			case 10:
				return new DEC_W_MREG_XX(memory, addr, src, dest);
			case 11:
				return new DEC_S_MREG_XX(memory, addr, src, dest);
			case 12:
				return new DEC_B_MREG_XX(memory, addr, src, dest);
			/*case 13:
				return new UDEC_W_MREG_XX(memory, addr, src, dest);
			case 14:
				return new UDEC_MREG_XX(memory, addr, src, dest);
			case 15:
				return new UDEC_B_MREG_XX(memory, addr, src, dest);*/
			}
		}
		case 10: {
			// CMP/INV GROUP
			switch (group) {
			case 0:
				return new CMP_W_REGX_REGY(memory, addr, src, dest);
			case 1:
				return new CMP_S_REG_XX(memory, addr, src, dest);
			case 2:
				return new CMP_W_REGX_MREGY_XX(memory, addr, src, dest);
			case 3:
				return new CMP_S_REGX_MREGY_XX(memory, addr, src, dest);
			case 4:
				return new CMP_B_REGX_MREGY_XX(memory, addr, src, dest);
			/*case 5:
				return new UCMP_W_REGX_MREGY(memory, addr, src, dest);
			case 6:
				return new UCMP_REGX_MREGY_XX(memory, addr, src, dest);
			case 7:
				return new UCMP_B_REGX_MREGY_XX(memory, addr, src, dest);*/
			case 8:
				return new INV_W_REG(memory, addr, src, dest);
			case 10:
				return new INV_W_MREG_XX(memory, addr, src, dest);
			case 11:
				return new INV_S_MREG_XX(memory, addr, src, dest);
			case 12:
				return new INV_B_MREG_XX(memory, addr, src, dest);
			}
		}
		case 11: {
			// FLOATING POINT GROUP
			switch (group) {
			case 2:
				return new FLOAT_REGX_REGY(memory, addr, src, dest, Instruction.FADD);
			case 3:
				return new FLOAT_REGX_REGY(memory, addr, src, dest, Instruction.FSUB);
			case 4:
				return new FLOAT_REGX_REGY(memory, addr, src, dest, Instruction.FMUL);
			case 5:
				return new FLOAT_REGX_REGY(memory, addr, src, dest, Instruction.FDIV);
			}
		}
		case 12: {
			switch (group) {
			case 0:
				return new LD_W_REGX_MREGY(memory, addr, src, dest);
			case 1:
				return new LD_W_REG_MXX(memory, addr, src, dest);
			case 2:
				return new LD_W_REGX_MREGY_XX(memory, addr, src, dest);
			case 8:
				return new ST_W_MREGX_REGY(memory, addr, src, dest);
			case 9:
				return new ST_W_MXX_REG(memory, addr, src, dest);
			case 10:
				return new ST_W_MREGX_XX_REGY(memory, addr, src, dest);
			}
		}
		case 13: {
			switch(group) {
			case 0:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.ADD_W);
			case 1:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.SUB_W);
			case 2:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.AND_W);
			case 3:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.OR_W);
			case 4:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.XOR_W);
			case 5:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.SHL_W);
			case 6:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.SHR_W);
			case 7:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.MUL_W);
			case 8:
				return new ALU_W_REG_XX(memory, addr, src, dest, Instruction.DIV_W);
			case 9:
				return new CMP_W_REG_XX(memory, addr, src, dest);
			}
		}
		case 14: {
			switch (group) {
			case 0:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.ADD_B);
			case 1:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.SUB_B);
			case 2:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.AND_B);
			case 3:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.OR_B);
			case 4:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.XOR_B);
			case 5:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.SHL_B);
			case 6:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.SHR_B);
			case 7:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.MUL_B);
			case 8:
				return new ALU_B_REG_XX(memory, addr, src, dest, Instruction.DIV_B);
			case 9: 
				return new CMP_B_REG_XX(memory, addr, src, dest);
			}
		}
		}
		return new Instruction(memory, addr, 0, 0);
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return lines.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		Instruction i = lines.get(row);
		return i.toCell(col);
	}

	@Override
	/**
	 * Ako se ova metoda ne redefinise, koristi se default renderer/editor za
	 * celiju. To znaci da, ako je kolona tipa boolean, onda ce se u tabeli
	 * prikazati true/false, a ovako ce se za takav tip kolone pojaviti
	 * checkbox.
	 */
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		// Prva kolona moze da se menja
		if (col < 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (EmulatorMain.DEBUG)
			System.out.println("SET BREAKPOINT AT: " + row);
		lines.get(row).breakPoint = (boolean) value;
	}

	public void reset() {
		lines.clear();
		addr_instr = new Instruction[100000];
	}

}
