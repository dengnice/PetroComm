import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


public class PetroTest {

	private static Logger logger = Logger.getLogger(PetroComm.class.getName()); 
	private String petroServerIp;
	private int abcListenport;
	private int petroListenPort;
	private volatile boolean running = false;
	private long receiveTimeDelay = 3000;
	private Thread connWatchDog;
	private Socket retSocket;
//	private BlockingQueue<>
	
	
	public PetroTest(String petroServerIp, int abcListenport, int petroListenPort) {
		this.petroServerIp = petroServerIp;
		this.abcListenport = abcListenport;
		this.petroListenPort = petroListenPort;
	}
	
	/**
	 * 要处理客户端发来的对象，并返回一个对象，可实现该接口。
	 */
	public interface ObjectAction{
		Object doAction(Object rev, PetroComm server);
	}
	
	
	public void start() throws UnknownHostException, IOException{
		if(running) return;
		running = true;
		retSocket = new Socket(petroServerIp, petroListenPort);
		connWatchDog = new Thread(new ConnWatchDog());
		connWatchDog.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stop(){
		if(running) running = false;
		if(connWatchDog != null) connWatchDog.stop();
	}
	
	public static final class DefaultObjectAction implements ObjectAction{
		public Object doAction(Object rev,PetroComm server) {
			logger.debug("测试 - 处理并返回：" + rev);
			return rev;
		}
	}
	
	class ConnWatchDog implements Runnable{
		public void run(){
			try {
				ServerSocket ss = new ServerSocket(abcListenport,5);
				while(running){
					Socket s = ss.accept();
					new Thread(new SocketAction(s)).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				PetroTest.this.stop();
			}
			
		}
	}
	
	
	public static String printHexString( byte[] b) {
		String a = "";
		for (int i = 0; i < b.length; i++) { 
			String hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			}
			a = a+hex;
		} 

		return a;
	}
	class SocketAction implements Runnable{
		Socket socket;
		boolean run = true;
		long lastReceiveTime = System.currentTimeMillis();
		public SocketAction(Socket socket) {
			this.socket = socket;
		}
		public void sendMsgByByte(byte[] b) throws IOException {
			OutputStream out = retSocket.getOutputStream();
			out.write(b);
			out.flush();
			logger.debug("发送报文为：\t" + new String(b));
		}
		public void run() {
			while(running && run){
				if(System.currentTimeMillis() - lastReceiveTime > receiveTimeDelay){
					overThis();
				}else{
					try {
						InputStream bis = socket.getInputStream();
						if(bis.available() > 0){
							lastReceiveTime = System.currentTimeMillis();
			                byte[] bytes = new byte[4]; // 读取报文长度
			                //TODO 增加空包的判断
			                bis.read(bytes);
		        			logger.debug( "从中石油接收的报文头为 = " + printHexString(bytes));
			                int i = 3;
			        		int length = bytes[i];
			        		int flag = 256;
			        		while(i-- > 0) {
			        			length += bytes[i] * flag;
			        			flag *= 256;
			        		}
			        		if(length > 0) {
			        			byte[] msg = new byte[length];
			        			bis.read(msg);
			        			logger.debug( "从中石油接收的报文内容为\t" + printHexString(msg));
			        			byte[] retMsg = new byte[length];
				        		System.arraycopy(bytes, 0, retMsg, 0, 4);
				        		System.arraycopy(msg, 0, retMsg, 4, msg.length);  
			        			sendMsgByByte(retMsg);
			        			logger.debug("向中石油发送的报文为\t" + printHexString(retMsg));
			        		}
						}else{
							Thread.sleep(10);
						}
					} catch (Exception e) {
						e.printStackTrace();
						overThis();
					} 
				}
			}
		}
		
		
		private void overThis() {
			if(run) run = false;
			if(socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("server socket close error");
				}
			}
			logger.debug("关闭：" + socket.getRemoteSocketAddress());
		}
		
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int abcListenPort = 20309;
		int petroListenPort = 6000;
		String petroServerIp = "10.30.100.23";
		boolean running = true;
		PetroComm server = new PetroComm(petroServerIp, abcListenPort, petroListenPort);
		server.start();
	}
}
