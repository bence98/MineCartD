package csokicraft.minecartd.server;

import java.io.*;

public class MineCraftServer{
	public static final String[] EXEC_NAMES=new String[]{"server.sh", "start.sh"};
	
	protected File dir;
	/** The executable for the server, usually ServerDir/server.sh */
	protected File exec;
	/** A process spawned from the server's executable */
	Process proc;
	
	public MineCraftServer(File srvDir){
		dir=srvDir;
		discoverExec();
	}
	
	protected void discoverExec(){
		for(File f:dir.listFiles()){
			for(String fName:EXEC_NAMES)
				if(fName.equals(f.getName())){
					exec=f;
					return;
				}
		}
		if(exec==null)
			System.err.println("Server '"+getName()+"' doesn't have a 'server.sh' script");
	}
	
	public void startProc() throws IOException{
		ProcessBuilder builder=new ProcessBuilder();
		builder.directory(dir).command(exec.getAbsolutePath());
		proc=builder.start();
	}
	
	public void killProc(){
		proc.destroyForcibly();
	}
	
	public void sendCmd(String str) throws IOException{
		OutputStream os=proc.getOutputStream();
		os.write(str.getBytes());
		os.write(System.lineSeparator().getBytes());
		os.flush();
	}

	public String getName(){
		return dir.getName();
	}
}
