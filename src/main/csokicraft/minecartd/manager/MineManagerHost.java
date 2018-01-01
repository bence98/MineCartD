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
		cfg.onServerStart();
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
		
		System.out.println("Command: {"+cmd+"} {"+srv+"} {"+par+"}");
		
		if("help".equals(cmd)){
			out.println("Available commands:");
			out.println(" help");
			out.println(" list");
			out.println(" STOP");
			out.println(" start <server>");
			out.println(" kill <server>");
			out.println(" cmd <server> <command>");
			out.println(" log <server> [maxlines]");
			return;
		}
		if("list".equals(cmd)){
			out.println("Loaded servers:");
			for(String name:servers.keySet()){
				out.print(' ');out.print(name);out.print('\t');
				if(servers.get(name).isAlive())
					out.println("*Online*");
				else
					out.println("*Offline*");
			}
			out.println("To start a server, use the 'start <name>' command");
			return;
		}
		if("STOP".equals(cmd)){
			for(MineCraftServer server:servers.values()){
				if(server.isAlive())
					server.sendCmd("stop");
			}
			try{
				Thread.sleep(1000);
				waitForServersToStop();
			}catch(InterruptedException ex){
				ex.printStackTrace();
			}
			alive=false;
			return;
		}
		MineCraftServer server=servers.get(srv);
		if(server==null) out.println("No server named '"+srv+"'!");
		else switch(cmd){
		case "start":
			if(server.isAlive()){
				out.println(srv+" is already running!");
			}else{
				server.startProc();
				out.println("Started "+srv);
			}
			return;
		case "kill":
			if(server.isAlive()){
				server.killProc();
				out.println("Killed "+srv);
			}else{
				out.println(srv+" is offline!");
			}
			return;
		case "cmd":
			server.sendCmd(par);
			out.println("Sent '"+par+"' to "+srv);
			return;
		case "log":
			if(server.isAlive()){
				byte ln=10;
				if(par!=null) ln=Byte.parseByte(par);
				server.printLog(out, ln);
				out.println();
			}else{
				out.println(srv+" is offline!");
			}
		}
	}

	private void waitForServersToStop() throws InterruptedException{
		for(MineCraftServer server:servers.values()){
			if(server.isAlive())
				server.waitForStop();
		}
	}

	public boolean isAlive(){
		return alive;
	}
}
