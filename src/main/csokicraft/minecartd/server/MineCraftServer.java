package csokicraft.minecartd.server;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class MineCraftServer{
	public static final String[] EXEC_NAMES=new String[]{"server.sh", "start.sh"};
	
	/** Working directory for the server */
	protected File dir;
	/** The executable for the server, usually ServerDir/server.sh */
	protected File exec;
	/** A process spawned from the server's executable */
	protected Process proc;
	/** Line buffering thread */
	protected OutputLineBuffer lnBuf;
	
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
		ProcessBuilder builder=new ProcessBuilder().redirectErrorStream(true);
		builder.directory(dir).command(exec.getAbsolutePath());
		proc=builder.start();
		lnBuf=new OutputLineBuffer(proc.getInputStream());
		lnBuf.start();
	}
	
	public void killProc(){
		proc.destroyForcibly();
		lnBuf.close();
	}
	
	public boolean isAlive(){
		return proc!=null&&proc.isAlive();
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

	public void waitForStop() throws InterruptedException{
		if(!proc.waitFor(30, TimeUnit.SECONDS))
			killProc();
	}

	public void printLog(PrintWriter out, byte ln){
		String[] lines=lnBuf.retrieveLast(ln);
		for(String line:lines){
			if(line!=null)
				out.println(line);
		}
	}
}
