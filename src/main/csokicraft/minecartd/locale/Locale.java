package csokicraft.minecartd.locale;

import java.io.*;
import java.util.*;

public class Locale{
	protected Map<String, String> entries;
	
	public Locale(){
		entries=new HashMap<>();
	}
	
	public static Locale from(InputStream in) throws IOException{
		BufferedReader br=new BufferedReader(new InputStreamReader(in));
		Locale ret=new Locale();
		String ln=br.readLine();
		while(ln!=null){
			if(!ln.isEmpty()&&ln.charAt(0)!='#'){
				String[] arr=ln.split("=");
				ret.addEntry(arr[0], arr[1]);
			}
			ln=br.readLine();
		}
		br.close();
		return ret;
	}
	
	protected void addEntry(String key, String val){
		entries.put(key, val);
	}
	
	public String getEntry(String key){
		return entries.get(key);
	}
	
	public String getEntryFormatted(String key, Object... args){
		return String.format(getEntry(key), args);
	}
}
