package csokicraft.minecartd.locale;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Locales{
	protected Map<String, Locale> locales;
	protected Locale active;
	
	public static Locales inst=new Locales();
	public final Locale EN;
	
	protected Locales(){
		locales=new HashMap<>();
		try{
			EN=Locale.from(ClassLoader.getSystemResourceAsStream("lang/en.lang"));
		}catch (IOException e){
			throw new RuntimeException("Couldn't load locales!", e);
		}
	}
	
	public Locale getLocale(String name){
		if(!locales.containsKey(name)){
			InputStream in=ClassLoader.getSystemResourceAsStream("lang/"+name+".lang");
			try{
				locales.put(name, Locale.from(in));
			}catch(IOException|NullPointerException e){
				System.err.println(getActive().getEntryFormatted("error.locale.notfound", name));
				return EN;
			}
		}
		return locales.get(name);
	}
	
	public Locale getActive(){
		if(active==null) active=EN;
		return active;
	}
	
	public void setActive(String to){
		active = getLocale(to);
		System.out.println(active.getEntryFormatted("msg.host.setlocale", to));
	}
}
