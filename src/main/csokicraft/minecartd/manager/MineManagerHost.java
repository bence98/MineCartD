package csokicraft.minecartd.manager;

import java.io.*;
import java.net.*;
import java.util.*;

import csokicraft.minecartd.MineConfigHandler;
import csokicraft.minecartd.locale.Locales;
import csokicraft.minecartd.server.MineCraftServer;

public class MineManagerHost{
	
	protected Map<String, MineCraftServer> servers;
	protected ServerSocket srvSock;
	/** Holds the workers. Always has exactly 1 worker in 'new' state */
	protected List<MineManagerWorker> workers;
	protected String password;
	private boolean alive=true;
	
	public MineManagerHost(List<MineCraftServer> l, int port, String pass) throws IOException{
		servers=new HashMap<>();
		for(MineCraftServer srv:l){
			servers.put(srv.getName(), srv);
			srv.onHostLoaded();
		}
		password=pass;
		srvSock=new ServerSocket(port);
		workers=new LinkedList<>();
		newWorker();
	}
	
	public MineManagerHost(MineConfigHandler cfg) throws IOException{
		this(cfg.discoverServers(), cfg.port, cfg.password);
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
		
		System.out.println(Locales.inst.getActive().getEntryFormatted("msg.host.incmd", cmd, srv, par));
		
		if("help".equals(cmd)){
			out.println(Locales.inst.getActive().getEntry("msg.ci.ack.help"));
			out.println(Locales.inst.getActive().getEntry("msg.ci.help.1"));
			out.println(Locales.inst.getActive().getEntry("msg.ci.help.2"));
			out.println(Locales.inst.getActive().getEntry("msg.ci.help.3"));
			out.println(Locales.inst.getActive().getEntry("msg.ci.help.4"));
			out.println(Locales.inst.getActive().getEntry("msg.ci.help.5"));
			out.println(Locales.inst.getActive().getEntry("msg.ci.help.6"));
			out.println(Locales.inst.getActive().getEntry("msg.ci.help.7"));
			return;
		}
		if("list".equals(cmd)){
			out.println(Locales.inst.getActive().getEntry("msg.ci.ack.list"));
			for(String name:servers.keySet()){
				out.print(' ');out.print(name);out.print('\t');
				if(servers.get(name).isAlive())
					out.println(Locales.inst.getActive().getEntry("msg.ci.list.on"));
				else
					out.println(Locales.inst.getActive().getEntry("msg.ci.list.off"));
			}
			out.println(Locales.inst.getActive().getEntry("msg.ci.tail.list"));
			return;
		}
		if("STOP".equals(cmd)){
			out.println(Locales.inst.getActive().getEntry("msg.ci.ack.stop"));
			out.flush();
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
		if(server==null) out.println(Locales.inst.getActive().getEntryFormatted("error.ci.nosrv", srv));
		else switch(cmd){
		case "start":
			if(server.isAlive()){
				out.println(Locales.inst.getActive().getEntryFormatted("error.ci.srvon", srv));
			}else{
				server.startProc();
				out.println(Locales.inst.getActive().getEntryFormatted("msg.ci.ack.start", srv));
			}
			return;
		case "kill":
			if(server.isAlive()){
				server.killProc();
				out.println(Locales.inst.getActive().getEntryFormatted("msg.ci.ack.kill", srv));
			}else{
				out.println(Locales.inst.getActive().getEntryFormatted("error.ci.srvoff", srv));
			}
			return;
		case "cmd":
			server.sendCmd(par);
			out.println(Locales.inst.getActive().getEntryFormatted("msg.ci.ack.cmd", par, srv));
			return;
		case "log":
			if(server.isAlive()){
				byte ln=10;
				if(par!=null) ln=Byte.parseByte(par);
				server.printLog(out, ln);
				out.println();
			}else{
				out.println(Locales.inst.getActive().getEntryFormatted("error.ci.srvoff", srv));
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
