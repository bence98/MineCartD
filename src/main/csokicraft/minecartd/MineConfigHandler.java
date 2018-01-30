package csokicraft.minecartd;

import java.io.*;
import java.util.*;

import csokicraft.minecartd.locale.Locales;
import csokicraft.minecartd.server.MineCraftServer;

public class MineConfigHandler{
	public static final String  DEFAULT_CFG="/etc/minecartd.conf",
								DEFAULT_DIR="/var/lib/minecartd/servers",
								DEFAULT_TMP="/tmp/minecartd.tmp";
	public static final int DEFAULT_PORT=40960;
	
	/** Config file */
	protected File cfgFile;
	
	/** Config entry: Servers' directory */
	public File serversDir;
	/** Config entry: The port of the server */
	public int port;
	/** Config entry: A password for the server, or null if it does not exist */
	public String password;
	
	/** Temp file */
	protected File tmpFile;
	
	/** Tempfile entry: last known port of the server. '-1' means unknown */
	protected int lastPort=-1;
	/** Tempfile entry: last known password of the server */
	public String lastPass;
	
	public MineConfigHandler() throws IOException{
		this(new File(DEFAULT_CFG));
	}

	protected MineConfigHandler(File file) throws IOException{
		cfgFile=file;
		readCfg();
	}

	private void genCfg() throws IOException{
		System.out.println(Locales.inst.getActive().getEntryFormatted("msg.host.mkcfg", cfgFile.getAbsolutePath()));
		PrintWriter fout=new PrintWriter(cfgFile);
		fout.println("# This is the default config for MineCartD");
		fout.println();
		fout.println("# The directory for the servers");
		fout.print("dir="); fout.println(DEFAULT_DIR);
		fout.print("port="); fout.println(DEFAULT_PORT);
		fout.println("# The language. The `-l` option overrides it. If not set, will fall back to 'en'");
		fout.println("#lang=en");
		fout.println("# The password for the Command Interface. This does not fully protect from illegal access, you have been warned!");
		fout.println("#pass=Password!");
		fout.close();
	}

	private void readCfg() throws IOException{
		System.out.println(Locales.inst.getActive().getEntryFormatted("msg.host.readcfg", cfgFile.getAbsolutePath()));
		if(!cfgFile.exists())
			genCfg();
		if(!cfgFile.canRead())
			throw new IllegalAccessError(Locales.inst.getActive().getEntryFormatted("error.host.cfg.read", cfgFile.getAbsolutePath()));
		
		port=DEFAULT_PORT;
		BufferedReader fin=new BufferedReader(new FileReader(cfgFile));
		String ln=fin.readLine();
		while(ln!=null){
			if(ln.startsWith("dir="))
				serversDir=new File(ln.substring(4));
			else if(ln.startsWith("port="))
				port=Integer.parseInt(ln.substring(5));
			else if(ln.startsWith("pass="))
				password=ln.substring(5);
			else if(ln.startsWith("lang=")&&!Locales.inst.hasActive())
				Locales.inst.setActive(ln.substring(5));
			else if(!ln.startsWith("#")&&!ln.trim().isEmpty())
				System.err.println(Locales.inst.getActive().getEntryFormatted("error.host.cfg.invalid", cfgFile.getAbsolutePath(), ln));
			ln=fin.readLine();
		}
		fin.close();
		if(serversDir==null)
			serversDir=new File(DEFAULT_DIR);
		System.out.println(Locales.inst.getActive().getEntryFormatted("msg.host.discover.dir", serversDir.getAbsolutePath()));
	}
	
	public List<MineCraftServer> discoverServers(){
		if(!serversDir.exists())
			serversDir.mkdirs();
		if(!serversDir.isDirectory())
			throw new IllegalArgumentException(Locales.inst.getActive().getEntryFormatted("error.host.discover.dir", serversDir.getAbsolutePath()));
		
		List<MineCraftServer> servers=new LinkedList<>();
		for(File srvDir:serversDir.listFiles()){
			if(srvDir.isDirectory())
				servers.add(new MineCraftServer(srvDir));
		}
		System.out.println(Locales.inst.getActive().getEntryFormatted("msg.host.discover.count", servers.size()));
		return servers;
	}

	public void setTempFile(String tmp) throws IOException{
		if(tmp==null){
			tmpFile=null;
			return;
		}
		
		tmpFile=new File(tmp);
		
		//read tempfile
		if(!tmpFile.exists()) return;
		BufferedReader in=new BufferedReader(new FileReader(tmpFile));
		String ln=in.readLine();
		while(ln!=null){
			if(ln.startsWith("port="))
				lastPort=Integer.parseInt(ln.substring(5));
			else if(ln.startsWith("pass="))
				lastPass=ln.substring(5);
			else System.err.println(Locales.inst.getActive().getEntryFormatted("error.host.tmp.invalid", tmpFile.getAbsolutePath(), ln));
			ln=in.readLine();
		}
		in.close();
	}
	
	public void onServerStart() throws IOException{
		if(tmpFile!=null){
			PrintWriter out=new PrintWriter(tmpFile);
			out.println("port="+port);
			if(password!=null)
				out.println("pass="+password);
			out.close();
		}
	}
	
	public void onServerStop(){
		if(tmpFile!=null){
			tmpFile.delete();
			lastPort=-1;
			lastPass=null;
		}
	}
}
