package emulator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class IniFile {
	/**
	 * Hash mapa koja sadrzi kategorije (sekcije). Hash kljuc je naziv
	 * kategorije (string), a vrednost je hash mapa koja sadrzi parove
	 * (parametar, vrednost).
	 */
	private Hashtable<String, Hashtable<String, String>> categories = new Hashtable<String, Hashtable<String, String>>();

	private String filename = null;

	/**
	 * Konstruise objekat sa datim parametrima.
	 * 
	 * @param filename
	 *            Naziv lokalnog INI fajla.
	 */
	public IniFile(String filename) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));
			readINI(in);
			in.close();
			this.filename = filename;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Konstruise objekat sa datim parametrima.
	 * 
	 * @param url
	 *            URL INI fajla.
	 */
	public IniFile(URL url) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			readINI(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readINI(BufferedReader in) {
		String line, key, value;
		StringTokenizer st;
		String currentCategory = "default";
		Hashtable<String, String> currentMap = new Hashtable<String, String>();
		categories.put(currentCategory, currentMap);
		try {
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.equals("") || line.indexOf(';') == 0)
					continue;
				if (line.charAt(0) == '[') {
					currentCategory = line.substring(1, line.length() - 1);
					currentMap = new Hashtable<String, String>();
					categories.put(currentCategory, currentMap);
				} else {
					st = new StringTokenizer(line, "=");
					if (st.hasMoreTokens()) {
						key = st.nextToken().trim();
						boolean hastoks = false;
						value = "";
						while (st.hasMoreTokens()) {
							value += (hastoks ? "=" : "")
									+ st.nextToken().trim();
							hastoks = true;
						}
						currentMap.put(key, value);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Vraca vrednost datog parametra u obliku stringa.
	 * 
	 * @param category
	 *            Kategorija (sekcija) u kojoj se nalazi parametar
	 * @param key
	 *            Naziv parametra
	 * @return String koji sadrzi vrednost parametra
	 */
	public String getString(String category, String key, String def) {
		Hashtable<String, String> hm = categories.get(category);
		if (hm == null) {
			hm = new Hashtable<String, String>();
			categories.put(category, hm);
			return def;
		} else {
			String ret = (String) hm.get(key); 
			if (ret == null) {
				hm.put(key, def);
				return def;
			}
			return ret;
		}
	}

	/**
	 * Vraca vrednost datog parametra u obliku integera.
	 * 
	 * @param category
	 *            Kategorija (sekcija) u kojoj se nalazi parametar
	 * @param key
	 *            Naziv parametra
	 * @return Integer vrednost parametra
	 */
	public int getInt(String category, String key, int defaultValue) {
    Hashtable <String, String> hm = categories.get(category);
    if (hm == null) {
    	hm = new Hashtable<String, String>();
		categories.put(category, hm);
      return defaultValue;
    } else {
    	String value = hm.get(key);
    	if (value != null) {
    		return Integer.parseInt(value);
    	} else {
    		hm.put(key, "" + defaultValue);
    		return defaultValue;
    	}
    }
  }

	public void setString(String category, String key, String value) {
		Hashtable<String, String> hm = categories.get(category);
		if (hm == null) {
			Hashtable<String, String> currentMap = new Hashtable<String, String>();
			currentMap.put(key, value);
			categories.put(category, currentMap);
		} else {
			hm.put(key, value);
		}
	}

	public void setInt(String category, String key, int value) {
		Hashtable<String, String> hm = categories.get(category);
		if (hm == null) {
			Hashtable<String, String> currentMap = new Hashtable<String, String>();
			currentMap.put(key, "" + value);
			categories.put(category, currentMap);
		} else {
			hm.put(key, "" + value);
		}
	}
	
	public Hashtable<String, Hashtable<String, String>> getCategories() {
		return categories;
	}
	public boolean removeCategory(String category) {
	    Hashtable <String, String> hm = categories.get(category);
	    if (hm == null)
	      return false;
	    else {
	    	categories.remove(category);
	    	return true;
	    }
	}
	public boolean removeItem(String category, String key) {
	    Hashtable <String, String> hm = categories.get(category);
	    if (hm == null)
	      return false;
	    else {
	    	String value = hm.get(key);
	    	if (value != null) {
	    		hm.remove(key);
	    		return true;
	    	} else 
	    		return false;
	    }
	}

	public void saveINI() {
		if (filename != null) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter(filename + ".txt"));
				Enumeration<String> enumi = categories.keys();
				while (enumi.hasMoreElements()) {
					String categoryName = enumi.nextElement();
					if (categoryName.equals("default"))
						continue;
					out.println("[" + categoryName + "]");
					Hashtable<String, String> category = categories
							.get(categoryName);
					if (category != null) {
						Enumeration<String> cKeys = category.keys();
						while (cKeys.hasMoreElements()) {
							String itemName = cKeys.nextElement();
							String value = category.get(itemName);
							if (value != null)
								out.println(itemName + "=" + value);
						}
					}
				}
				out.close();
				File f = new File(filename);
				f.delete();
				f = new File(filename + ".txt");
				f.renameTo(new File(filename));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String toString() {
		return categories.toString();
	}
}
