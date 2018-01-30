package csokicraft.minecartd.server;

import java.io.*;
import java.util.concurrent.TimeUnit;

import csokicraft.minecartd.locale.Locales;

public class MineCraftServer{
	public static final String[] EXEC_NAMES=new String[]{"server.sh", "start.sh"};
	public static final String PROP_NAME="minecartd.properties";
	
	/** Working directory for the server */
	protected File dir;
	/** The executable for the server, usually ServerDir/server.sh */
	protected File exec;
	/** A process spawned from the server's executable */
	protected Process proc;
	/** Whether this server should auto-start */
	protected boolean autostart=false;
	/** Line buffering thread */
	protected OutputLineBuffer lnBuf;
	
	public MineCraftServer(File srvDir){
		dir=srvDir;
		discoverExec();
	}
	
	protected void discoverExec(){
		for(File f:dir.listFiles()){
			if(exec==null) for(String fName:EXEC_NAMES)
				if(fName.equals(f.getName())){
					exec=f;
				}
			if(PROP_NAME.equals(f.getName()))
				try{
					readPropFile(f);
				}catch (IOException e){
					System.err.println(Locales.inst.getActive().getEntryFormatted("error.server.propfile.read", getName()));
					e.printStackTrace();
				}
		}
		if(exec==null)
			System.err.println(Locales.inst.getActive().getEntryFormatted("error.server.nostart", getName()));
	}
	
	private void readPropFile(File f) throws IOException{
		BufferedReader fin=new BufferedReader(new FileReader(f));
		String ln=fin.readLine();
		while(ln!=null){
			if(ln.equals("autostart=1")||ln.equals("autostart=on")||ln.equals("autostart=true")||ln.equals("autostart=enable"))
				autostart=true;
			else if(ln.equals("autostart=0")||ln.equals("autostart=off")||ln.equals("autostart=false")||ln.equals("autostart=disable"))
				autostart=false;
			else
				System.err.println(Locales.inst.getActive().getEntryFormatted("error.server.propfile.invalid", getName(), ln));
			ln=fin.readLine();
		}
		fin.close();
	}

	private void startProc() throws IOException{
		ProcessBuilder builder=new ProcessBuilder().redirectErrorStream(true);
		builder.directory(dir).command(exec.getAbsolutePath());
		proc=builder.start();
		lnBuf=new OutputLineBuffer(proc.getInputStream());
		lnBuf.start();
	}
	
	public boolean startProc(PrintWriter err){
		try{
			startProc();
			return true;
		}catch (Exception e){
			err.println(Locales.inst.getActive().getEntryFormatted("error.server.start", getName()));
			e.printStackTrace(err);
			return false;
		}
	}
	
	public void killProc(){
		proc.destroyForcibly();
		lnBuf.close();
	}
	
	public boolean isAlive(){
		return proc!=null&&proc.isAlive();
	}
	
	private void sendCmd(String str) throws IOException{
		OutputStream os=proc.getOutputStream();
		os.write(str.getBytes());
		os.write(System.lineSeparator().getBytes());
		os.flush();
	}
	
	public boolean sendCmd(String str, PrintWriter err){
		try{
			sendCmd(str);
			return true;
		}catch(Exception e){
			err.println(Locales.inst.getActive().getEntryFormatted("error.server.cmd", getName()));
			e.printStackTrace(err);
			return false;
		}
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

	public void onHostLoaded(){
		if(autostart)
			startProc(new PrintWriter(System.err));
	}
}
