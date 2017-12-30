package csokicraft.minecartd.manager;

import java.io.*;
import java.net.*;

public class MineManagerWorker extends Thread{
	protected MineManagerHost host;

	public MineManagerWorker(MineManagerHost inst){
		host=inst;
	}

	@Override
	public void run(){
		try{
			//worker state: new
			Socket sock=host.srvSock.accept();
			//worker state: active -> need a new worker
			host.newWorker();
			BufferedReader in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintWriter out=new PrintWriter(sock.getOutputStream());
			out.println("MineCartD v1.0 Command Interface. Type 'help' for a list of commands");
			out.flush();
			String cmd=in.readLine();
			while(cmd!=null){
				host.process(cmd, out);
				out.flush();
				cmd=in.readLine();
			}
			//worker state: finished
		}catch(Exception e){
			e.printStackTrace();
		}
		host.removeWorker(this);
		//worker state: deleted
	}
}
