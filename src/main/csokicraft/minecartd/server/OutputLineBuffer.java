package csokicraft.minecartd.server;

import java.io.*;
import java.util.*;

public class OutputLineBuffer extends Thread implements Closeable{
	protected BufferedReader reader;
	protected boolean closed=false;
	/** Lines are ordered from newest to oldest! */
	protected LinkedList<String> buf;

	public OutputLineBuffer(InputStream inputStream){
		setDaemon(true);
		reader=new BufferedReader(new InputStreamReader(inputStream));
		buf=new LinkedList<>();
	}

	@Override
	public void run(){
		try{
			while(!closed){
				String ln=reader.readLine();
				if(ln==null) close();
				else synchronized(buf){
					buf.addFirst(ln);
					if(buf.size()>127)
						buf.removeLast();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void close(){
		closed=true;
	}
	
	/** @param i The max number of iterations */
	public String[] retrieveLast(byte i){
		String[] ret=new String[i];
		synchronized(buf){
			for(String ln:buf){
				i--;
				if(i<0) break;
				ret[i]=ln;
			}
		}
		return ret;
	}
}
