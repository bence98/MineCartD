package csokicraft.minecartd.manager;

import java.io.*;
import java.net.*;

import csokicraft.minecartd.MineCartD;
import csokicraft.minecartd.locale.Locales;

public class MineManagerWorker extends Thread{
	protected MineManagerHost host;

	public MineManagerWorker(MineManagerHost inst){
		setDaemon(true);
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
			out.println(Locales.inst.getActive().getEntryFormatted("msg.ci.motd", MineCartD.APP_NAME_VER));
			out.flush();
			if(host.password!=null)
				checkPass(in, out);
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

	private void checkPass(BufferedReader in, PrintWriter out) throws IOException{
		String ln;
		do{
			out.print(Locales.inst.getActive().getEntry("prompt.ci.pass"));
			out.flush();
			ln=in.readLine();
		}while(ln!=null&&!ln.equals(host.password));
		if(ln==null)
			return;
		out.println(Locales.inst.getActive().getEntry("msg.ci.auth_ok"));
		out.flush();
	}
}
