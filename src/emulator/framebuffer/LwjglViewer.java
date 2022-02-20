package emulator.framebuffer;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VERSION_UNAVAILABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowAspectRatio;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.glfw.GLFW.nglfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glTexSubImage2D;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import emulator.EmulatorMain;
import emulator.engine.CpuContext;
import emulator.engine.Engine;
import emulator.src.Instruction;

public class LwjglViewer implements IFBViewer {

	/**
	 * framebuffer mode: 2 - graphics mode, 640x480x2; 1 - graphics mode, 320x240 pixels; 0 - text mode, 80x60
	 * characters
	 */
	private int mode = TEXT_MODE;
	private boolean inverse = false;

	private CpuContext ctx;

	private FBModel memMdl;

	private Color[] backgroundColors;
	private Color[] foregroundColors;

	private SpriteDef[] spriteDef;
	
	// ####################### OPENGL stuff ###########################

	ByteBuffer framebuffer = BufferUtils.createByteBuffer(640 * 480 * 3);
	ByteBuffer pixelframebuffer = BufferUtils.createByteBuffer(640 * 480 * 3);

	private long window;
	public int width = 1024;
	public int height = 768;
	public int x;
	public int y;

	GLFWErrorCallback errCallback;
	GLFWKeyCallback keyCallback;
	GLFWMouseButtonCallback mouseButtonCallback;
	GLFWCursorPosCallback cursorPosCallback; 
	GLFWFramebufferSizeCallback fbCallback;
	Callback debugProc;
	int[] graphicsModeTextureId = new int[10];

	int[] graphicsModeVAO = new int[10];// IntBuffer.allocate(1);
	int[] graphicsModeVBO = new int[10];// IntBuffer.allocate(1);
	int[] graphicsModeEBO = new int[10];// IntBuffer.allocate(1);

	Shader graphicsModeShader;
	String graphicsShaderVertSource = "#version 330 core\nlayout (location = 0) in vec3 aPos; layout (location = 1) in vec2 aTexCoord; out vec2 TexCoord; void main() { gl_Position = vec4(aPos, 1.0); TexCoord = aTexCoord; }";
	String graphicsShaderFragSource = "#version 330 core\nout vec4 FragColor; in vec2 TexCoord; uniform sampler2D tex; void main() { FragColor = texture(tex, TexCoord); }";
	
	Object lock = new Object();
	// ######################## END OF OPENGL STUFF
	// ###################################
	static {
		reverse8(font_8x8);
	}
	public LwjglViewer(GraphicsConfiguration conf, CpuContext ctx, Engine eng, Rectangle r) {
		this.ctx = ctx;
		this.memMdl = new FBModel(ctx);
		this.backgroundColors = new Color[160 * 60];
		this.foregroundColors = new Color[160 * 60];
		
		
		spriteDef = new SpriteDef[SPRITE_COUNT];
		for (int i = 0; i < SPRITE_COUNT; i++) {
			spriteDef[i] = new SpriteDef();
		}
		
		this.width = r.width;
		this.height = r.height;
		this.x = r.x;
		this.y = r.y;
		
		Thread t = new Thread() {
			public void run() {
				try {
					init();
					glfwSetWindowPos(window, r.x, r.y);
					loop();
					
					errCallback.free();
					keyCallback.free();
					mouseButtonCallback.free();
					cursorPosCallback.free();
					fbCallback.free();
					if (debugProc != null)
						debugProc.free();
					glfwDestroyWindow(window);
				} catch (Throwable t) {
					t.printStackTrace();
				} finally {
					glfwTerminate();
					synchronized(lock) {
						lock.notify();
					}
				}
			}
		};
		t.start();
		
	}

	@Override
	public void updateCell(int addr, short cont) {
//		for (int i = 50000; i < 51000; i++) {
//			setFourPixels(i, 0x5555);
//		}
		int content = cont & 0xFFFF;
		if (this.mode == TEXT_MODE) {
			if ((addr >= Engine.VIDEO_OFFS) && (addr < (Engine.VIDEO_OFFS + 160 * 60))) {
				int row = (addr - Engine.VIDEO_OFFS) / 160;
				Color foregroundColor = getTextColor((int) (~((content >>> 12) & 15)));
				Color backgroundColor = getTextColor((int) ((content >>> 8) & 15));
				int fixed_addr = ((addr & 0xfffffffe) - Engine.VIDEO_OFFS);
				int col = (fixed_addr % 160) / 2;
				int c = 127 - (int) (content & 0xff);
				int lineBytes = 640 * 3 * scale;
				int offset = (59 - row) * 8 * lineBytes + (col) * 8 * 3;
				if ((c < 0) || (c > 127)) return;
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						if ((font_8x8[c * 8 + i] & (1 << j)) != 0) {
							putPixel(framebuffer, offset + i * lineBytes + (7 - j) * 3, foregroundColor);
						} else {
							putPixel(framebuffer, offset + i * lineBytes + (7 - j) * 3, backgroundColor);
						}
					}
				}

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + col + ", " + row + "): " + String.format("%c", c));
					System.out.println("Foreground Color: " + this.foregroundColors[addr - Engine.VIDEO_OFFS]);
					System.out.println("Background Color: " + this.backgroundColors[addr - Engine.VIDEO_OFFS]);
				}
			}
		} else if (this.mode == GRAPHICS_MODE_320_240) {
			if (addr >= Engine.VIDEO_OFFS && addr < (Engine.VIDEO_OFFS + (320 * 240) / 2)) {
				Color[] color = new Color[4];
				if ((addr & 1) == 0) {
					color[0] = getColor((int) ((content >> 12) & 15));
					color[1] = getColor((int) ((content >> 8) & 15));
					color[2] = getColor((int) ((content >> 4) & 15));
					color[3] = getColor((int) ((content) & 15));
				} else {
					addr -= 1;
					color[0] = getColor((int) ((content >> 12) & 15));
					color[1] = getColor((int) ((content >> 8) & 15));
					color[2] = getColor((int) ((content >> 4) & 15));
					color[3] = getColor((int) ((content) & 15));
				}
				Insets pixel = getCoordinate(addr);
				int lineBytes = 640 * 3 * scale;
				int offset = ((479 - pixel.top * 2)) * lineBytes + (pixel.left * 2 * 3);
				for (int i = 0; i < 4; i++) {
					putPixel(framebuffer, offset + i * 6, color[i]);
					putPixel(framebuffer, offset + i * 6 + 3, color[i]);
					putPixel(framebuffer, offset + i * 6 - lineBytes, color[i]);
					putPixel(framebuffer, offset + i * 6 - lineBytes + 3, color[i]);
				}

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + (pixel.left + 0) + ", " + (pixel.top) + "): " + color[0]);
					System.out.println("(" + (pixel.left + 1) + ", " + (pixel.top) + "): " + color[1]);
					System.out.println("(" + (pixel.left + 2) + ", " + (pixel.top) + "): " + color[2]);
					System.out.println("(" + (pixel.left + 3) + ", " + (pixel.top) + "): " + color[3]);
				}
			}
		} else if (this.mode == GRAPHICS_MODE_640_480) {
			if (addr >= Engine.VIDEO_OFFS && addr < (Engine.VIDEO_OFFS + (640 * 480) / 8)) {
				Color[] color = new Color[16];
				color[0] = getColor2(content & 0x8000);
				color[1] = getColor2(content & 0x4000);
				color[2] = getColor2(content & 0x2000);
				color[3] = getColor2(content & 0x1000);
				color[4] = getColor2(content & 0x0800);
				color[5] = getColor2(content & 0x0400);
				color[6] = getColor2(content & 0x0200);
				color[7] = getColor2(content & 0x0100);
				color[8] = getColor2(content & 0x0080);
				color[9] = getColor2(content & 0x0040);
				color[10] = getColor2(content & 0x0020);
				color[11] = getColor2(content & 0x0010);
				color[12] = getColor2(content & 0x0008);
				color[13] = getColor2(content & 0x0004);
				color[14] = getColor2(content & 0x0002);
				color[15] = getColor2(content & 1);
				Insets pixel = getCoordinate(addr);
				int lineBytes = 640 * 3 * scale;
				// int offset = (59 - row) * 8 * 640 * 3 + (col) * 8 * 3;
				int offset = (479 - pixel.top) * lineBytes + (pixel.left * 3);
				for (int i = 0; i < 16; i++) {
					putPixel(framebuffer, offset + i * 3, color[i]);
				}

				if (EmulatorMain.DEBUG) {
					System.out.println("(" + (pixel.left + 0) + ", " + (pixel.top) + "): " + color[0]);
					System.out.println("(" + (pixel.left + 1) + ", " + (pixel.top) + "): " + color[1]);
					System.out.println("(" + (pixel.left + 2) + ", " + (pixel.top) + "): " + color[2]);
					System.out.println("(" + (pixel.left + 3) + ", " + (pixel.top) + "): " + color[3]);
					System.out.println("(" + (pixel.left + 4) + ", " + (pixel.top) + "): " + color[4]);
					System.out.println("(" + (pixel.left + 5) + ", " + (pixel.top) + "): " + color[5]);
					System.out.println("(" + (pixel.left + 6) + ", " + (pixel.top) + "): " + color[6]);
					System.out.println("(" + (pixel.left + 7) + ", " + (pixel.top) + "): " + color[7]);
					System.out.println("(" + (pixel.left + 8) + ", " + (pixel.top) + "): " + color[8]);
					System.out.println("(" + (pixel.left + 9) + ", " + (pixel.top) + "): " + color[9]);
					System.out.println("(" + (pixel.left + 10) + ", " + (pixel.top) + "): " + color[10]);
					System.out.println("(" + (pixel.left + 11) + ", " + (pixel.top) + "): " + color[11]);
					System.out.println("(" + (pixel.left + 12) + ", " + (pixel.top) + "): " + color[12]);
					System.out.println("(" + (pixel.left + 13) + ", " + (pixel.top) + "): " + color[13]);
					System.out.println("(" + (pixel.left + 14) + ", " + (pixel.top) + "): " + color[14]);
					System.out.println("(" + (pixel.left + 15) + ", " + (pixel.top) + "): " + color[15]);
				}
			}
		}

	}

	private int scale = 1;

	private void putPixel(ByteBuffer buff, int addr, Color foregroundColor) {
		int hOffset = 0;
		int row = (addr / 3) / 640;
		int col = ((addr - (row * 640 * 3)) / 3);
//		System.out.printf("addr: %d, row: %d, col: %d\n", addr, row, col);
		// row = 479 - row;
		if (scale == 1) {
			buff.put(addr + 0, (byte) foregroundColor.getRed());
			buff.put(addr + 1, (byte) foregroundColor.getGreen());
			buff.put(addr + 2, (byte) foregroundColor.getBlue());
		} else {
			try {
				addr = (row * 640 * 3 * scale) + (col * 3 * scale);
				buff.put(addr + 0 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 1 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 2 + hOffset, (byte) foregroundColor.getBlue());

				buff.put(addr + 3 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 4 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 5 + hOffset, (byte) foregroundColor.getBlue());

				buff.put(addr + 640 * 3 * scale + 0 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 640 * 3 * scale + 1 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 640 * 3 * scale + 2 + hOffset, (byte) foregroundColor.getBlue());

				buff.put(addr + 640 * 3 * scale + 3 + hOffset, (byte) foregroundColor.getRed());
				buff.put(addr + 640 * 3 * scale + 4 + hOffset, (byte) foregroundColor.getGreen());
				buff.put(addr + 640 * 3 * scale + 5 + hOffset, (byte) foregroundColor.getBlue());

			} catch (Exception ex) {
				System.out.printf("addr: %d, row: %d, col: %d\n", addr, row, col);
			}
		}
	}

	private static final int ONE = 198;

	private Color getTextColor(int col) {
		int c = col & 15;
		switch (c) {
		case 0:
			if (this.inverse)
				return Color.white;
			else
				return Color.black;
		case 9:
			if (this.inverse)
				return new Color(255, 255, 0);
			else
				return Color.blue;
		case 10:
			if (this.inverse)
				return new Color(255, 0, 255);
			else
				return Color.green;
		case 11:
			if (this.inverse)
				return new Color(255, 255, 0);
			else
				return new Color(0, 255, 255);
		case 12:
			if (this.inverse)
				return new Color(0, 255, 255);
			else
				return Color.red;
		case 13:
			if (this.inverse)
				return new Color(0, 255, 0);
			else
				return new Color(255, 0, 255);
		case 14:
			if (this.inverse)
				return new Color(0, 0, 255);
			else
				return new Color(255, 255, 0);
		case 15:
			if (this.inverse)
				return Color.black;
			else
				return Color.white;
		case 8:
			if (this.inverse)
				return new Color(128, 128, 128);
			else
				return new Color(128, 128, 128);
		case 1:
			if (this.inverse)
				return new Color(128, 128, 0);
			else
				return new Color(0, 0, ONE);
		case 2:
			if (this.inverse)
				return new Color(128, 0, 128);
			else
				return new Color(0, ONE, 0);
		case 3:
			if (this.inverse)
				return new Color(128, 0, 0);
			else
				return new Color(0, ONE, ONE);
		case 4:
			if (this.inverse)
				return new Color(0, 128, 128);
			else
				return new Color(ONE, 0, 0);
		case 5:
			if (this.inverse)
				return new Color(0, 128, 0);
			else
				return new Color(ONE, 0, ONE);
		case 6:
			if (this.inverse)
				return new Color(0, 0, 128);
			else
				return new Color(ONE, ONE, 0);
		case 7:
			if (this.inverse)
				return new Color(128, 128, 128);
			else
				return new Color(ONE, ONE, ONE);
		default:
			return Color.black;
		}
	}

	@Override
	public Color getColor(int col) {
		int c = col & 15;
		switch (c) {
		case 0:
			return Color.black;
		case 9:
			return Color.blue;
		case 10:
			return Color.green;
		case 11:
			return new Color(0, 255, 255);
		case 12:
			return Color.red;
		case 13:
			return new Color(255, 0, 255);
		case 14:
			return new Color(255, 255, 0);
		case 15:
			return Color.white;
		case 8:
			return Color.gray;
		case 1:
			return new Color(0, 0, ONE);
		case 2:
			return new Color(0, ONE, 0);
		case 3:
			return new Color(0, ONE, ONE);
		case 4:
			return new Color(ONE, 0, 0);
		case 5:
			return new Color(ONE, 0, ONE);
		case 6:
			return new Color(ONE, ONE, 0);
		case 7:
			return new Color(ONE, ONE, ONE);
		default:
			return Color.black;
		}
	}

	private Color getColor2(int s) {
		if (inverse) {
			if (s == 0)
				return Color.WHITE;
			else
				return Color.BLACK;
		} else {
			if (s == 0)
				return Color.BLACK;
			else
				return Color.WHITE;
		}
	}

	private Insets getCoordinate(int addr) {
		if (mode == GRAPHICS_MODE_320_240) {
			int start = addr - Engine.VIDEO_OFFS;
			int row = start / 160;
			int col = start % 160;
			return new Insets(row, col * 2, 0, 0);
		} else if (mode == GRAPHICS_MODE_640_480) {
			int start = (addr & 0xFFFE) - Engine.VIDEO_OFFS;
			int row = start / 80;
			int col = start % 80;
			return new Insets(row, col * 8, 0, 0);
		}
		return new Insets(0, 0, 0, 0);
	}

	private static void reverse8(byte[] buff) {
		// 0 -> 23, 1 -> 22, 2 -> 21
		byte t;
		for (int i = 0; i < buff.length / 2; i++) {
			t = buff[i];
			buff[i] = buff[buff.length - i - 1];
			buff[buff.length - i - 1] = t;
		}
	}

	@Override
	public void reset() {
		memMdl.fireTableDataChanged();
		this.backgroundColors = new Color[160 * 60];
		this.foregroundColors = new Color[160 * 60];
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public void setInverse(boolean b) {
		this.inverse = b;
	}

	@Override
	public JFrame getFrame() {
		int[] w = new int[1];
		int[] h = new int[1];
		glfwGetWindowSize(window, w, h);
		this.width = w[0];
		this.height = h[0];
		
		int[] xx = new int[1];
		int[] yy = new int[1];
		glfwGetWindowPos(window, xx, yy);
		this.x = xx[0];
		this.y = yy[0];
		return null;
	}

	@Override
	public int getMode() {
		return this.mode;
	}

	// ############################### OPENGL STUFF
	// ##########################################
	private void init() throws IOException {
		glfwSetErrorCallback(errCallback = new GLFWErrorCallback() {
			private GLFWErrorCallback delegate = GLFWErrorCallback.createPrint(System.err);

			@Override
			public void invoke(int error, long description) {
				if (error == GLFW_VERSION_UNAVAILABLE)
					System.err.println("This demo requires OpenGL 3.0 or higher.");
				delegate.invoke(error, description);
			}

			@Override
			public void free() {
				delegate.free();
			}
		});

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		try {
			if (width == 0) width = 100;
			if (height == 0) height = 100;
			window = glfwCreateWindow(width, height, "FBViewer", NULL, NULL);
		} catch (Exception ex) {
			JOptionPane.showConfirmDialog(null, "Framebuffer window failed to open. Cause: " + ex.getMessage());
		}
		if (window == NULL) {
			throw new AssertionError("Failed to create the GLFW window");
		}
		
		glfwSetWindowAspectRatio(window, 4, 3);
		
		glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);

		glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				if (width > 0 && height > 0 && (LwjglViewer.this.width != width || LwjglViewer.this.height != height)) {
					LwjglViewer.this.width = width;
					LwjglViewer.this.height = height;
				}
			}
		});
		
		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
					if (ctx.memory[Engine.IRQ2_PRESSED_ADDR / 2] == 0)
						return;
					ctx.memory[24] = VkToFpga(key, scancode, mods);
					Engine.irq2_pressed = true;
					Engine.irq2_released = false;
					
					try {
						ctx.engine.rl.lock();
						ctx.engine.cond.await(1000, TimeUnit.MILLISECONDS);
						ctx.engine.rl.unlock();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} else if (action == GLFW_RELEASE) {
					if (ctx.memory[Engine.IRQ2_RELEASED_ADDR / 2] == 0)
						return;
					ctx.memory[24] = VkToFpga(key, scancode, mods);
					Engine.irq2_pressed = false;
					Engine.irq2_released = true;
					
					try {
						ctx.engine.rl.lock();
						ctx.engine.cond.await(1000, TimeUnit.MILLISECONDS);
						ctx.engine.rl.unlock();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
		    @Override
		    public void invoke(long window, int button, int action, int mods) {
	    		double[] xpos = new double[1];
	    		double[] ypos = new double[1];
	    		glfwGetCursorPos(window, xpos, ypos);

		    	int x = (int) (xpos[0] / (scale * 2));
				int y = (int) (ypos[0] / (scale * 2)); // (scale == 1?2:4)
				if (x > 303)
					x = 303;
				if (y > 223)
					y = 223;

				if (action == GLFW.GLFW_PRESS) {
					mouseIrq((short) 9, (short) x, (short) y, (short) 1);
		    	} else {
		    		mouseIrq((short) 8, (short) x, (short) y, (short) 1);
		    	}
		    }
		});
		
		glfwSetCursorPosCallback(window, cursorPosCallback = new GLFWCursorPosCallback() {
			int lastX = 0;
			int lastY = 0;

		    @Override
		    public void invoke(long window, double xpos, double ypos) {
		    	int x = (int) (xpos / (scale * 2));
				int y = (int) (ypos / (scale * 2)); // (scale == 1?2:4)
				if (x > 303)
					x = 303;
				if (y > 223)
					y = 223;
				short button = 0;
				if ((x != lastX) || (y != lastY)) {
					button = 0;
					if (x < lastX) {
						button += 8;
					} else {
						button += 16;
					}
					if (y < lastY) {
						button += 0;
					} else {
						button += 32;
					}

				}
				mouseIrq(button, (short) x, (short) y, (short) 1);
				//System.out.println("MOUSE MOVE " + x + ", " + y );
				lastX = x;
				lastY = y;
		    }
		});

		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);
		glfwShowWindow(window);

		try (MemoryStack frame = MemoryStack.stackPush()) {
			IntBuffer framebufferSize = frame.mallocInt(2);
			nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
			width = framebufferSize.get(0);
			height = framebufferSize.get(1);
		}

		GL.createCapabilities();
		debugProc = GLUtil.setupDebugMessageCallback();

		// ##################### STEFANOV KOD ##################################
		glGenTextures(graphicsModeTextureId);
		glBindTexture(GL_TEXTURE_2D, graphicsModeTextureId[0]);
		// Blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 640, 480, 0, GL_RGB, GL_UNSIGNED_BYTE, pixelframebuffer);
		glGenerateMipmap(GL_TEXTURE_2D);

		float vertices[] = {
				// positions // texture coords
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, // top right
				-1.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top left
				1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // bottom right
				-1.0f, -1.0f, 0.0f, 0.0f, 0.0f // bottom left
		};

		int indices[] = { 0, 1, 2, 1, 2, 3 };

		glGenVertexArrays(graphicsModeVAO);
		glGenBuffers(graphicsModeVBO);
		glGenBuffers(graphicsModeEBO);

		glBindVertexArray(graphicsModeVAO[0]);

		// Graphics VBO
		glBindBuffer(GL_ARRAY_BUFFER, graphicsModeVBO[0]);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

		// Graphics EBO
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, graphicsModeEBO[0]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0L); // Vertex position
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4L); // Texture coords
		glEnableVertexAttribArray(1);

		// Shaders
		try {
			graphicsModeShader = new Shader(graphicsShaderVertSource, graphicsShaderFragSource);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ##################### STEFANOV KOD ##################################

	}

	private void mouseIrq(short key, short x, short y, short status) {
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 0] = x;
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 1] = y;
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 2] = key;
		ctx.memory[this.ctx.mouse_struct_addr / 2  + 3] = status;
	}
	
	private short VkToFpga(int keyCode, int scanCode, int mods) {
		
		if (keyCode == 96 && scanCode == 41) {
//			case KeyEvent.VK_BACKQUOTE		:	return 96   ; `
			return 96;
		} else if (keyCode == 91 && scanCode == 26) {
//			case KeyEvent.VK_BRACE_LEFT		:		return 91   ;  [
			return 91;
		} else if (keyCode == 93 && scanCode == 27) {
//			case KeyEvent.VK_BRACE_RIGHT		:	return 93   ;	]
			return 93;
		} else if (keyCode == 59 && scanCode == 39) {
//			case KeyEvent.VK_SEMICOLON			:	return 59   ;	;
			return 59;
		} else if (keyCode == 39 && scanCode == 40) {
//		case KeyEvent.VK_QUOTE					:	return 39   ;	'
			return 39;
		} else if (keyCode == 92 && scanCode == 43) {
//			case KeyEvent.VK_BACK_SLASH		:		return 92   ;  \
			return 92;
		} else if (keyCode == 47 && scanCode == 53) {
//			case KeyEvent.VK_SLASH		:		return 47   ;  /
			return 47;
		} else if (keyCode == 45 && scanCode == 12) {
			return 45; // - top row, left to the =
		}
		
		if (keyCode == 340 && scanCode == 42) {
//		case KeyEvent.VK_LEFT_SHIFT		:		return 501  ;
			return 501;
		} else if (keyCode == 344 && scanCode == 54) {
//		case KeyEvent.VK_RIGHT_SHIFT		:	return 502  ;
			return 502;
		} else if (keyCode == 342 && scanCode == 56) {
//		case KeyEvent.VK_LEFT_ALT			:		return 401  ;
			return 401;
		} else if (keyCode == 346 && scanCode == 312) {
//		case KeyEvent.VK_RIGHT_ALT			:	return 402  ;
			return 402;
		} else if (keyCode == 341 && scanCode == 29) {
//		case KeyEvent.VK_LEFT_CONTROL	:		return 601  ;
			return 601;
		} else if (keyCode == 345 && scanCode == 285) {
//		case KeyEvent.VK_RIGHT_CONTROL	:	return 602  ;
			return 602;
		} else if (keyCode == 343 && scanCode == 347) {
//		case KeyEvent.VK_LEFT_WINDOWS	:		return 1001 ;
			return 1001;
		} else if (keyCode == 347 && scanCode == 348) {
//		case KeyEvent.VK_RIGHT_WINDOWS	:	return 1002 ;
			return 1002;
		}

		if (keyCode == 335 && scanCode == 284) {
//		case KeyEvent.VK_NUMPAD_ENTER 	:	return 5013 ;
			return 5013;
		}

		switch (keyCode) {
		case KeyEvent.VK_0:
			return 48;
		case KeyEvent.VK_1:
			return 49;
		case KeyEvent.VK_2:
			return 50;
		case KeyEvent.VK_3:
			return 51;
		case KeyEvent.VK_4:
			return 52;
		case KeyEvent.VK_5:
			return 53;
		case KeyEvent.VK_6:
			return 54;
		case KeyEvent.VK_7:
			return 55;
		case KeyEvent.VK_8:
			return 56;
		case KeyEvent.VK_9:
			return 57;

		case KeyEvent.VK_SPACE:
			return 32;
		case KeyEvent.VK_A:
			return 65;
		case KeyEvent.VK_B:
			return 66;
		case KeyEvent.VK_C:
			return 67;
		case KeyEvent.VK_D:
			return 68;
		case KeyEvent.VK_E:
			return 69;
		case KeyEvent.VK_F:
			return 70;
		case KeyEvent.VK_G:
			return 71;
		case KeyEvent.VK_H:
			return 72;
		case KeyEvent.VK_I:
			return 73;
		case KeyEvent.VK_J:
			return 74;
		case KeyEvent.VK_K:
			return 75;
		case KeyEvent.VK_L:
			return 76;
		case KeyEvent.VK_M:
			return 77;
		case KeyEvent.VK_N:
			return 78;
		case KeyEvent.VK_O:
			return 79;
		case KeyEvent.VK_P:
			return 80;
		case KeyEvent.VK_Q:
			return 81;
		case KeyEvent.VK_R:
			return 82;
		case KeyEvent.VK_S:
			return 83;
		case KeyEvent.VK_T:
			return 84;
		case KeyEvent.VK_U:
			return 85;
		case KeyEvent.VK_V:
			return 86;
		case KeyEvent.VK_W:
			return 87;
		case KeyEvent.VK_X:
			return 88;
		case KeyEvent.VK_Z:
			return 90;
		case KeyEvent.VK_Y:
			return 89;

		case KeyEvent.VK_BACK_QUOTE://$$
			return 96;
		case KeyEvent.VK_SLASH:// $$
			return 47;
		case KeyEvent.VK_BACK_SLASH://$$
			return 92;
		case KeyEvent.VK_OPEN_BRACKET://$$
			return 91;
		case KeyEvent.VK_CLOSE_BRACKET://$$
			return 93;
		case KeyEvent.VK_EQUALS:
			return 61;
		case KeyEvent.VK_PLUS: //$$
			return 61;
		case KeyEvent.VK_MINUS: // $$
			return 45;
		case KeyEvent.VK_SEMICOLON:// $$
			return 59;
		case KeyEvent.VK_PERIOD:
			return 46;
		case KeyEvent.VK_COMMA:
			return 44;
		case 162:
			return 60;

		case 290:		// F1
			return 301;
		case 291:
			return 302;
		case 292:
			return 303;
		case 293:
			return 304;
		case 294:
			return 305;
		case 295:
			return 306;
		case 296:
			return 307;
		case 297:
			return 308;
		case 298:
			return 309;
		case 299:
			return 310;
		case 300:
			return 311;
		case 301: // F12
			return 312;
		case 280: // VL_CAPS_LOCK:
			return 800;
		case 282: // VK_NUM_LOCK:
			return 801;
		case 281: // VK_SCROLL_LOCK:
			return 802;

		case 348: // VK_CONTEXT_MENU:
			return 2000;

		case 258: // VK_TAB:
			return 9;
		case 257: // VK_ENTER:
			return 13;
		case 256: // VK_ESCAPE:
			return 27;
		case 259: // VK_BACK_SPACE:
			return 8;

		case 262: // VK_RIGHT:
			return 4003;
		case 263: // VK_LEFT:
			return 4001;
		case 265: // VK_UP:
			return 4000;
		case 264: // VK_DOWN:
			return 4002;

		case 266: // VK_PAGE_UP:
			return 3002;
		case 267: // VK_PAGE_DOWN:
			return 3005;
		case 268: // VK_HOME:
			return 3001;
		case 269: // VK_END:
			return 3004;
		case 260: // VK_INSERT:
			return 3000;
		case 261: //VK_DELETE:
			return 3003;

		case 320: // VK_NUMPAD0:
			return 5048;
		case 321: // VK_NUMPAD1:
			return 5049;
		case 322: // KeyEvent.VK_NUMPAD2:
			return 5050;
		case 323: // KeyEvent.VK_NUMPAD3:
			return 5051;
		case 324: // KeyEvent.VK_NUMPAD4:
			return 5052;
		case 325: // KeyEvent.VK_NUMPAD5:
			return 5053;
		case 326: // KeyEvent.VK_NUMPAD6:
			return 5054;
		case 327: // KeyEvent.VK_NUMPAD7:
			return 5055;
		case 328: // KeyEvent.VK_NUMPAD8:
			return 5056;
		case 329: // KeyEvent.VK_NUMPAD9:
			return 5057;
		case 334: // KeyEvent.VK_ADD:
			return 5043;
		case 333: // KeyEvent.VK_SUBTRACT:
			return 5045;
		case 331: // KeyEvent.VK_DIVIDE:
			return 5047;
		case 332: // KeyEvent.VK_MULTIPLY:
			return 5042;
		case 330: // KeyEvent.VK_DECIMAL:
			return 5046;

		case 283: // KeyEvent.VK_PRINTSCREEN:
			return 10000;
		}
		return 0;

	}

	private void loop() {
		while (!glfwWindowShouldClose(window)) {
			glfwPollEvents();
			glViewport(0, 0, width, height);

			render();
			
			glfwSwapBuffers(window);
		}
	}
	
	private boolean between(int x1, int x2) {
		return Math.abs(x1 - x2) <= 15;
	}
	
	class Overlap {
		int x1, y1;
		int w1, h1;
		int x2, y2;
		int w2, h2;
	}
	private Overlap getOverlap(int i, int j) {
		if (spriteDef[i].spriteAddr > 0 && spriteDef[j].spriteAddr > 0) {
			int x1 = spriteDef[i].x, x2 = spriteDef[j].x;
			int y1 = spriteDef[i].y, y2 = spriteDef[j].y;
			if (between(x1, x2) && between(y1, y2)) {
				Overlap retVal = new Overlap();
				if (x1 <= x2) {
					retVal.x1 = x2 - x1;
					retVal.x2 = 0;
					retVal.w1 = 16 - retVal.x1;
					retVal.w2 = retVal.w1;
				} else {
					retVal.x1 = 0;
					retVal.x2 = x1 - x2;
					retVal.w2 = 16 - retVal.x2;
					retVal.w1 = retVal.w2;
				}
				if (y1 <= y2) {
					retVal.y1 = y2 - y1;
					retVal.y2 = 0;
					retVal.h1 = 16 - retVal.y1;
					retVal.h2 = retVal.h1;
				} else {
					retVal.y1 = 0;
					retVal.y2 = y1 - y2;
					retVal.h2 = 16 - retVal.y2;
					retVal.h1 = retVal.h2;
				}
				return retVal;
			}
		}
		return null;
	}
	
	private void render() {
		// copy the framebuffer to the actual buffer to be drawn
		pixelframebuffer.position(0);
		framebuffer.position(0);
		for (int i = 0; i < framebuffer.limit(); i++) {
			pixelframebuffer.put(framebuffer.get());
		}
		
		// copy the sprite data into the actual buffer to be drawn
		if (this.mode == GRAPHICS_MODE_320_240) {
			for (int i = SPRITE_COUNT - 1; i >= 0; i--) {
				if (ctx.memory[(SPRITE_DEF_START + i * 8) / 2] != 0) {
					spriteDef[i].spriteAddr = Instruction.fix(ctx.memory[(SPRITE_DEF_START + i * 8) / 2]);
					spriteDef[i].x = ctx.memory[(SPRITE_DEF_START + i * 8 + 2) / 2];
					spriteDef[i].y = ctx.memory[(SPRITE_DEF_START + i * 8 + 4) / 2];
					spriteDef[i].transparentColor = getColor(ctx.memory[(SPRITE_DEF_START + i * 8 + 6) / 2]);
					//fillSprite(pixelframebuffer, spriteDef[i], ctx);
					spriteDef[i].fillBuff(ctx, this);
				}
			}
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!
			for (int i = 0; i < SPRITE_COUNT; i++) {
				for (int j = i+2; j < SPRITE_COUNT; j++) {
					Overlap overlap = getOverlap(i, j);
					if (overlap != null) {
						for (int i1 = overlap.x2; i1 < overlap.x2 + overlap.w2; i1++) {
							for (int j1 = overlap.y2; j1 < overlap.y2 + overlap.h2; j1++) {
								spriteDef[j].buff[i1][j1] = spriteDef[i].transparentColor;
							}
						}
					}
				}
			}
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!
			for (int i = SPRITE_COUNT - 1; i >= 0; i--) {
				if (ctx.memory[(SPRITE_DEF_START + i * 8) / 2] != 0) {
					putSprite(pixelframebuffer, spriteDef[i]);
				}
			}
		}
		pixelframebuffer.position(0);
		
		// ################## STEFANOV KOD ##########################
		graphicsModeShader.use();
		graphicsModeShader.setInt("tex", 0);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 640, 480, GL_RGB, GL_UNSIGNED_BYTE, pixelframebuffer);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, graphicsModeTextureId[0]);

		glBindVertexArray(graphicsModeVAO[0]);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		// ################## STEFANOV KOD ##########################

		glBindVertexArray(0);
		glUseProgram(0);
	}

	private void putSprite(ByteBuffer buff, SpriteDef sp) {
		if (sp.spriteAddr > 0) {
			Color[] pixels = new Color[4];
			int lineBytes = 640*3 * scale;
			int faddr = (479 - sp.y * 2) * lineBytes + (sp.x * 2) * 3;
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 4; j++) {
					pixels[0] = sp.buff[j*4 + 0][i];
					pixels[1] = sp.buff[j*4 + 1][i];
					pixels[2] = sp.buff[j*4 + 2][i];
					pixels[3] = sp.buff[j*4 + 3][i];
					for (int k = 0; k < 4; k++) {
						int faddr2 = faddr - (i * 2 * lineBytes) + j * 12 * 2 + k * 6;
						if (!pixels[k].equals(sp.transparentColor)) {
							// non-transparent color
							putPixel(buff, faddr2 + 0, pixels[k]);
							putPixel(buff, faddr2 + 3, pixels[k]);
							putPixel(buff, faddr2 - lineBytes + 0, pixels[k]);
							putPixel(buff, faddr2 - lineBytes + 3, pixels[k]);
						}
					}
				}
			}
		}
	}

	protected void fillSprite(ByteBuffer buff, SpriteDef sp, CpuContext ctx) {
		Color[] pixels = new Color[4];
		int lineBytes = 640*3 * scale;
		int faddr = (479 - sp.y * 2) * lineBytes + (sp.x * 2) * 3;
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 4; j++) {
				int addr = sp.spriteAddr + i * 8 + j * 2;
				short content = ctx.memory[addr / 2];
				pixels[0] = getColor((int) ((content >> 12) & 7));
				pixels[1] = getColor((int) ((content >> 8) & 7));
				pixels[2] = getColor((int) ((content >> 4) & 7));
				pixels[3] = getColor((int) ((content) & 7));
				for (int k = 0; k < 4; k++) {
					int faddr2 = faddr - (i * 2 * lineBytes) + j * 12 * 2 + k * 6;
					if (!pixels[k].equals(sp.transparentColor)) {
						// non-transparent color
						putPixel(buff, faddr2 + 0, pixels[k]);
						putPixel(buff, faddr2 + 3, pixels[k]);
						putPixel(buff, faddr2 - lineBytes + 0, pixels[k]);
						putPixel(buff, faddr2 - lineBytes + 3, pixels[k]);
					}
				}
			}
		}

	}
	
	@Override
	public void dispose() {
		glfwSetWindowShouldClose(window, true);
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
