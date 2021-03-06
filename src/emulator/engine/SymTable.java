package emulator.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymTable {
	public HashMap<Integer, List<String>> sym;
	public static Map<String, Integer> addresses;
	
	public SymTable(String fileName) {
		sym = new HashMap<Integer, List<String>>();
		addresses = new HashMap<String, Integer>();
		fileName = fixExt(fileName, ".sym");
//		System.out.println(fileName);
		if (fileName == null)
			return;
		File f = new File (fileName);
		if (f.exists() && f.isFile()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String s;
				while ((s = in.readLine()) != null) {
//					System.out.println(s);
					s = s.trim();
					String[] tokens = s.split("=");
					if (tokens.length == 2) {
						try {
							String val = tokens[1].trim();
							if (val.startsWith("-")) {
								val = "-" + val.substring(3); 
							} else {
								val = val.substring(2);
							}
							putInMap(Integer.parseInt(val, 16), tokens[0].trim());
							addresses.put(tokens[0].trim(), Integer.parseInt(val, 16));
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void putInMap(int num, String s) {
		List<String> l = this.sym.get(num);
		if (l != null) {
			l.add(s);
		} else {
			l = new ArrayList<String>();
			l.add(s);
			this.sym.put(num, l);
		}
		
	}

	public static String fixExt(String fileName, String ext) {
		int idx = fileName.lastIndexOf('.');
		if (idx != -1) {
			return fileName.substring(0, idx) + ext;
		}
		return null;
	}
}
