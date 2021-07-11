package emulator.raspbootin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import emulator.engine.Engine;

public class Raspbootin64Client extends Thread {
	
	private PipedOutputStream serialPort;
	private PipedInputStream emulator;
	
	private static final int SLEEP_TIME = 3;
	
	public static ReentrantLock rl = new ReentrantLock();
	public static Condition cond = rl.newCondition();
	
	public Raspbootin64Client(PipedOutputStream serialPort, PipedInputStream emulator ) {
		this.serialPort = serialPort;
		this.emulator = emulator;
	}
	public void run() {
		try {
			System.out.println("Connecting to the Raspbootin...");
			while (true) {
				System.out.println("Waiting bytes from FPGA...");
				// Read 8+3 bytes from the serial port (name + load_ready)
				byte[] buffer = new byte[10];
				String s = "";
				do {
					emulator.read(buffer, 0, 1);
					if (buffer[0] != '\r' && buffer[0] != '\n' && buffer[0] != 3) {
						s += new String(buffer);
					}
				} while (buffer[0] != 3);

				Thread.sleep(SLEEP_TIME);

				emulator.read(buffer, 0, 2);
				System.out.println(Arrays.toString(buffer));
				System.out.println("\nRaspbootin ready: [" + s + "]");

				Thread.sleep(SLEEP_TIME);

				if(buffer[0] == 'f' ) {
					System.out.println("\nFILE MANAGEMENT COMMAND");
					System.out.println("FILE COMMAND: " + buffer[1]);
					// file management
					switch (buffer[1])  {
					case 'r':
						// READ FILE COMMAND
						StringBuilder sb = new StringBuilder();
						while(true) {
							emulator.read(buffer, 0, 1);
							System.out.printf("%d, %02X, %c ", buffer[0], buffer[0], buffer[0]);
							if (buffer[0] == 0)
								break;
							sb.append((char)buffer[0]);
						}
						String str = sb.toString();
						System.out.println("\nREAD FILE: '" + str + "'");
						File currFolder = new File(".");
						currFolder = new File(currFolder.getCanonicalPath() + "/../");
						System.out.println(currFolder.getCanonicalPath());
						File file = new File(currFolder, str);
						System.out.println(file.getCanonicalPath());
						if (file.exists() && file.isFile()) {
							System.out.println("FILE EXISTS, size: " + file.length());
							serialPort.write((byte) 1);  // OK
							waitUntilConsumed();
							long size = file.length();
							serialPort.write((byte) (size & 255));
							waitUntilConsumed();
							size >>= 8;
							serialPort.write((byte) (size & 255));
							waitUntilConsumed();
							size >>= 8;
							serialPort.write((byte) (size & 255));
							waitUntilConsumed();
							size >>= 8;
							serialPort.write((byte) (size & 255));
							serialPort.flush();
							waitUntilConsumed();
							
							FileInputStream fin = new FileInputStream(file);
							buffer = new byte[1024];
							int read;
							byte last = 0;
							while ((read = fin.read(buffer)) != -1) {
								System.out.print("#");
								for (int k = 0; k < read; k++) {
									serialPort.write(buffer[k]);
									last = buffer[k];
									serialPort.flush();
									waitUntilConsumed();
//									System.out.print(".");
								}
							}
							fin.close();
							dump(serialPort, last);
							System.out.println();
						} else {
							// Error with file to be read
							serialPort.write((byte) 10);  // ERROR
							waitUntilConsumed();
						}
						break;
					case 'w':
						// write file
						Thread.sleep(SLEEP_TIME);
						System.out.println("\nWRITE FILE");
						// file name
						sb = new StringBuilder();
						do {
							emulator.read(buffer, 0, 1);
							//System.out.print(buffer[0]);
							if (buffer[0] != 0)
								sb.append((char)buffer[0]);
						} while (buffer[0] != 0);
						String name = sb.toString();
						
						// file size
						emulator.read(buffer, 0, 1);
						int B1 = buffer[0] < 0 ? 256 + buffer[0] : buffer[0];
						System.out.println(B1);
						int size = B1;
						emulator.read(buffer, 0, 1);
						B1 = buffer[0] < 0 ? 256 + buffer[0] : buffer[0];
						System.out.println(B1);
						size += (B1 << 8);
						emulator.read(buffer, 0, 1);
						B1 = buffer[0] < 0 ? 256 + buffer[0] : buffer[0];
						System.out.println(B1);
						size += (B1 << 16);
						emulator.read(buffer, 0, 1);
						B1 = buffer[0] < 0 ? 256 + buffer[0] : buffer[0];
						System.out.println(B1);
						size += (B1 << 24);
						System.out.printf("File name: %s, file size: %d\n", name, size);
						
						currFolder = new File(".");
						currFolder = new File(currFolder.getCanonicalPath() + "/../");
						System.out.println(currFolder.getCanonicalPath());
						file = new File(currFolder, name);
						FileOutputStream fout = new FileOutputStream(file);
						for (int i = 0; i < size; i++) {
							emulator.read(buffer, 0, 1);
							fout.write(buffer[0]);
							//System.out.println((int)buffer[0]);
							if (i % 1024 == 0)
								System.out.print("#");

						}
						fout.close();
						System.out.println();
						break;
					case 'd':
						// list files
						Thread.sleep(SLEEP_TIME*3);
						System.out.println("\nLIST FILES");
						File currFile = new File(".");
						currFile = new File(currFile.getCanonicalPath() + "/../");
						File dir = currFile;
						System.out.println("Current folder: " + dir.getCanonicalPath());
						File[] files = dir.listFiles();
						sb = new StringBuilder();
						for (File f : files) {
							if (f.isFile()) {
								sb.append(f.getName());
								sb.append("\n");
							}
						}
						str = sb.toString();
						System.out.println(String.format("Response length: %04X", str.length()));
						size = str.length();
						serialPort.write((byte) (size & 255));
						waitUntilConsumed();
						size >>= 8;
						serialPort.write((byte) (size & 255));
						waitUntilConsumed();
						size >>= 8;
						serialPort.write((byte) (size & 255));
						waitUntilConsumed();
						size >>= 8;
						serialPort.write((byte) (size & 255));
						waitUntilConsumed();
						byte last = 0;
						for (int i = 0; i < str.length(); i++) {
							char c = str.charAt(i);
							serialPort.write((byte) c);
							last = (byte)c;
							waitUntilConsumed();
							if (i % 1024 == 0)
								System.out.print("#");
						}
						System.out.println();
						dump(serialPort, last);
						break;
					case 'f':
						// list folders
						Thread.sleep(SLEEP_TIME*3);
						System.out.println("\nLIST FOLDERS");
						file = new File(".");
						file = new File(file.getCanonicalPath() + "/../");
						dir = file;
						System.out.println("Current folder: " + dir.getCanonicalPath());
						files = dir.listFiles();
						sb = new StringBuilder();
						for (File f : files) {
							if (f.isDirectory()) {
								sb.append("<" + f.getName() + ">");
								sb.append("\n");
							}
						}
						str = sb.toString();
						System.out.println(String.format("Response length: %04X", str.length()));
						size = str.length();
						serialPort.write((byte) (size & 255));
						waitUntilConsumed();
						size >>= 8;
						serialPort.write((byte) (size & 255));
						waitUntilConsumed();
						size >>= 8;
						serialPort.write((byte) (size & 255));
						waitUntilConsumed();
						size >>= 8;
						waitUntilConsumed();
						Thread.sleep(SLEEP_TIME);
						last = 0;
						for (int i = 0; i < str.length(); i++) {
							char c = str.charAt(i);
							serialPort.write((byte) c);
							last = (byte)c;
							waitUntilConsumed();
							if (i % 1024 == 0)
								System.out.print("#");
						}
						dump(serialPort, last);
						System.out.println();
						break;
					} 
				} else {
					Thread.sleep(SLEEP_TIME);
					System.out.println("\nERROR, got wrong bytes from FPGA Raspbootin: [" + new String(buffer) + "]");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private void dump(PipedOutputStream serialPort, byte last) throws IOException {
		for (int i = 0; i < 5; i++)
			serialPort.write(last);
	}
	private void waitUntilConsumed() throws Exception {
		Engine.irq1 = true;
		try {
			rl.lock();
			cond.await(1000, TimeUnit.MILLISECONDS);
			rl.unlock();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
//		synchronized(sync) {
//			sync.wait();
//		}
	}
	
	public static void unblock() {
		rl.lock();
		cond.signalAll();
		rl.unlock();
	}

}
