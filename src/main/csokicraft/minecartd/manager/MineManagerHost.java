package csokicraft.minecartd.manager;

import java.io.*;
import java.net.*;
import java.util.*;

import csokicraft.minecartd.MineConfigHandler;
import csokicraft.minecartd.server.MineCraftServer;

public class MineManagerHost{
	
	protected Map<String, MineCraftServer> servers;
	protected ServerSocket srvSock;
	/** Holds the workers. Always has exactly 1 worker in 'new' state */
	protected List<MineManagerWorker> workers;
	private boolean alive=true;
	
	public MineManagerHost(List<MineCraftServer> l, int port) throws IOException{
		servers=new HashMap<>();
		for(MineCraftServer srv:l){
			servers.put(srv.getName(), srv);
		}
		srvSock=new ServerSocket(port);
		workers=new LinkedList<>();
		newWorker();
	}
	
	public MineManagerHost(MineConfigHandler cfg) throws IOException{
		this(cfg.discoverServers(), cfg.port);
	}
	
	public synchronized void newWorker() throws IOException{
		MineManagerWorker worker=new MineManagerWorker(this);
		worker.start();
		workers.add(worker);
	}
	
	public synchronized void removeWorker(MineManagerWorker w){
		workers.remove(w);
	}

	public void process(String s, PrintWriter out) throws IOException{
		int pos1=s.indexOf(' ');
		String cmd, srv=null, par=null;
		if(pos1==-1)
			cmd=s;
		else{
			cmd=s.substring(0, pos1);
			srv=s.substring(pos1+1);
			int pos2=srv.indexOf(' ');
			if(pos2!=-1){
				par=srv.substring(pos2+1);
				srv=srv.substring(0, pos2);
			}
		}
		if("help".equals(cmd)){
			out.println("Available commands:");
			out.println(" help");
			out.println(" list");
			out.println(" start <server>");
			out.println(" kill <server>");
			out.println(" cmd <server> <command>");
//			out.println(" log <server> [maxlines]");
			return;
		}
		if("list".equals(cmd)){
			out.println("Loaded servers:");
			for(String name:servers.keySet()){
				out.print(' ');out.println(name);
			}
			out.println("To start a server, use the 'start <name>' command");
			return;
		}
		MineCraftServer server=servers.get(srv);
		if(server==null) out.println("No server named '"+srv+"'!");
		else switch(cmd){
		case "start":
			server.startProc();
			out.println("Started "+srv);
			return;
		case "kill":
			server.killProc();
			out.println("Killed "+srv);
			return;
		case "cmd":
			server.sendCmd(par);
			out.println("Sent '"+par+"' to "+srv);
			return;
		}
	}

	public boolean isAlive(){
		return alive;
	}
}
