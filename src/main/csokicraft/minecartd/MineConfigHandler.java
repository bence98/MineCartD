package csokicraft.minecartd;

import java.io.*;
import java.util.*;

import csokicraft.minecartd.server.MineCraftServer;

public class MineConfigHandler{
	public static final String  DEFAULT_CFG="/etc/minecartd.conf",
								DEFAULT_DIR="/var/lib/minecartd/servers";
	public static final int DEFAULT_PORT=40960;
	
	protected File cfgFile;
	
	public File serversDir;
	public int port;
	
	public MineConfigHandler() throws IOException{
		this(new File(DEFAULT_CFG));
	}

	protected MineConfigHandler(File file) throws IOException{
		cfgFile=file;
		readCfg();
	}

	private void genCfg() throws IOException{
		System.out.println("Creating config at "+cfgFile.getAbsolutePath());
		PrintWriter fout=new PrintWriter(cfgFile);
		fout.println("# This is the default config for MineCartD");
		fout.println();
		fout.println("# The directory for the servers");
		fout.print("dir="); fout.println(DEFAULT_DIR);
		fout.print("port="); fout.println(DEFAULT_PORT);
		fout.close();
	}

	private void readCfg() throws IOException{
		System.out.println("Reading config at "+cfgFile.getAbsolutePath());
		if(!cfgFile.exists())
			genCfg();
		if(!cfgFile.canRead())
			throw new IllegalAccessError(cfgFile.getAbsolutePath()+" could not be read!");
		
		port=DEFAULT_PORT;
		BufferedReader fin=new BufferedReader(new FileReader(cfgFile));
		String ln=fin.readLine();
		while(ln!=null){
			if(ln.startsWith("dir="))
				serversDir=new File(ln.substring(4));
			else if(ln.startsWith("port="))
				port=Integer.parseInt(ln.substring(5));
			else if(!ln.startsWith("#")&&!ln.trim().isEmpty())
				System.err.println("Unrecognised option in config file ("+cfgFile.getAbsolutePath()+"): "+ln);
			ln=fin.readLine();
		}
		fin.close();
		if(serversDir==null)
			serversDir=new File(DEFAULT_DIR);
		System.out.println("Servers' directory is "+serversDir.getAbsolutePath());
	}
	
	public List<MineCraftServer> discoverServers(){
		if(!serversDir.exists())
			serversDir.mkdirs();
		if(!serversDir.isDirectory())
			throw new IllegalArgumentException(serversDir.getAbsolutePath()+" is not a directory!");
		
		List<MineCraftServer> servers=new LinkedList<>();
		for(File srvDir:serversDir.listFiles()){
			if(srvDir.isDirectory())
				servers.add(new MineCraftServer(srvDir));
		}
		System.out.println("Found "+servers.size()+" servers");
		return servers;
	}
}
