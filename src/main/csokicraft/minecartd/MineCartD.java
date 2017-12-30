package csokicraft.minecartd;

import java.io.*;

import csokicraft.minecartd.manager.MineManagerHost;

public class MineCartD{

	public static void main(String[] args) throws IOException{
		if(args.length>0&&("--help".equals(args[0])||"-h".equals(args[0]))){
			displayHelp();
			System.exit(0);
		}
		
		System.out.println("Loading MineCartD v1.0, by CsokiCraft");
		boolean discover=true;
		String cfg=null;
		for(int i=0;i<args.length;i++){
			if("--gen-cfg".equals(args[i])||"-C".equals(args[i]))
				discover=false;
			if("--cfgfile".equals(args[i])||"-f".equals(args[i]))
				cfg=args[++i];
		}
		
		MineConfigHandler cfgMan;
		if(cfg==null)
			cfgMan=new MineConfigHandler();
		else
			cfgMan=new MineConfigHandler(new File(cfg));
		
		if(discover){
			MineManagerHost host=new MineManagerHost(cfgMan);
			while(host.isAlive());
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
