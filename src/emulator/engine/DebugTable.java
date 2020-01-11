package emulator.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class DebugTable {

	public static Map<Integer, String> addresses = new HashMap<Integer, String>(); 
	
	public DebugTable(String fileName) {
		fileName = SymTable.fixExt(fileName, ".dbg");
		if (fileName == null)
			return;
		File f = new File (fileName);
		if (f.exists() && f.isFile()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String s;
				while ((s = in.readLine()) != null) {
					System.out.println(s);
					if (s.trim().startsWith("#"))
						continue;
					s = s.trim();
					String[] tokens = s.split(";");
					String addressStr = tokens[0].trim();
					Integer address = SymTable.addresses.get(addressStr);
					if (address != null) {
						int range = Integer.parseInt(tokens[1].trim());
						for (int addr = address; addr < address + range -1; addr += 1) {
							DebugTable.addresses.put(addr, "");
							System.out.println(addr);
						}
					}
				}
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
