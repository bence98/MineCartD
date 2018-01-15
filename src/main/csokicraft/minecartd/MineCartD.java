package csokicraft.minecartd;

import java.io.*;
import java.net.Socket;

import csokicraft.minecartd.manager.MineManagerHost;

public class MineCartD{
	public static final String APP_NAME_VER="MineCartD v1.1"; 

	public static void main(String[] args) throws IOException{
		if(args.length>0&&("--help".equals(args[0])||"-h".equals(args[0]))){
			displayHelp();
			System.exit(0);
		}
		
		System.out.println("Loading "+APP_NAME_VER+", by CsokiCraft");
		boolean discover=true, stop=false;
		String cfg=null, tmpf=MineConfigHandler.DEFAULT_TMP;
		for(int i=0;i<args.length;i++){
			if("--gen-cfg".equals(args[i])||"-C".equals(args[i]))
				discover=false;
			if("--cfgfile".equals(args[i])||"-f".equals(args[i]))
				cfg=args[++i];
			if("--stop".equals(args[i])||"-S".equals(args[i]))
				stop=true;
			if("--tmpfile".equals(args[i])||"-t".equals(args[i]))
				tmpf=args[++i];
			if("--no-tmpfile".equals(args[i])||"-T".equals(args[i]))
				tmpf=null;
		}
		
		MineConfigHandler cfgMan;
		if(cfg==null)
			cfgMan=new MineConfigHandler();
		else
			cfgMan=new MineConfigHandler(new File(cfg));
		cfgMan.setTempFile(tmpf);
		
		if(stop){
			int port=cfgMan.lastPort;
			if(port==-1) port=cfgMan.port;
			Socket sock=new Socket("localhost", port);
			PrintWriter out=new PrintWriter(sock.getOutputStream());
			if(cfgMan.lastPass!=null)
				out.println(cfgMan.lastPass);
			out.println("STOP");
			out.flush();
			sock.close();
		}else if(discover){
			MineManagerHost host=new MineManagerHost(cfgMan);
			while(host.isAlive())
				Thread.yield();
			cfgMan.onServerStop();
		}
	}

	private static void displayHelp(){
		System.out.println("Usage:");
		System.out.println(" minecartd --help|-h OR");
		System.out.println(" minecartd [options]");
		System.out.println("Options:");
		System.out.println(" --gen-cfg|-C : generate config and quit");
		System.out.println(" --cfgfile|-f <file> : use this config file");
	}
}
