import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


public class PetroComm {

	private static Logger logger = Logger.getLogger(PetroComm.class.getName()); 
	private static String logPath = "D:\\serverlog.txt";
	private String petroServerIp;
	private int abcListenPort;
	private int petroListenPort;
	private volatile boolean running = false;
	private long receiveTimeDelay = 3000;
	private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class,ObjectAction>();
	private Thread connWatchDog;
	private Socket retSocket;
	
	
	public PetroComm(String petroServerIp, int abcListenPort, int petroListenPort) {
		this.petroServerIp = petroServerIp;
		this.abcListenPort = abcListenPort;
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
//		retSocket = new Socket(petroServerIp, petroListenPort);
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
				ServerSocket ss = new ServerSocket(abcListenPort,5);
				while(running){
					Socket s = ss.accept();
					new Thread(new SocketAction(s)).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				PetroComm.this.stop();
			}
			
		}
	}
	
	class ProcessMsg implements Runnable{
		private byte[] msg;
		public ProcessMsg(byte[] msg) {
			this.msg = msg;
		}
		//字节数组转换为String
	    public String bytesToString(byte [] bytes,int start,int end){
	        String str = "";
	        if(bytes.length < end - start){
	            return str;
	        }
	        byte [] bs = new byte[end - start];
	        for(int i = 0;i < end - start;i++){
	            bs[i] = bytes[start++];
	        }
	        str = new String(bs);
	        return str;
	    }
	    
	    //hexString -> byte
	    public byte[] hexStringToBytes(String hexString) {
			if (hexString == null || hexString.equals("")) {
				return null;
			}
			hexString = hexString.toUpperCase();
			int length = hexString.length() / 2;
			char[] hexChars = hexString.toCharArray();
			byte[] d = new byte[length];
			for (int i = 0; i < length; i++) {
				int pos = i * 2;
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			}
			return d;
		}
		private byte charToByte(char c) {
			return (byte) "0123456789ABCDEF".indexOf(c);
		}
		
		public void sendMsgByByte(byte[] b) throws IOException {
			OutputStream out = retSocket.getOutputStream();
			out.write(b);
			out.flush();
			logger.debug("发送报文为：\t" + new String(b));
		}
		public void run(){
			
			try {
				//向太行发送报文
				Socket thSocket = new Socket("10.237.176.91",19122);
				OutputStream out = thSocket.getOutputStream();
				out.write(msg);
				out.flush();
				logger.debug("向太行发送报文为：\t" + new String(msg));
				//接收卡受理的返回, 关键问题：需要不需要按报文头内容读对应长度数据，以下按String读取
				/*
				BufferedReader in = new BufferedReader(new InputStreamReader(thSocket.getInputStream()));
				String retMsg = in.readLine();
				*/
				
				//按bytes及长度读取
				InputStream ris = thSocket.getInputStream();
				if(ris.available() > 0){
					byte[] tmpLength = new byte[2]; // 读取报文长度
	                ris.read(tmpLength);
	                int i = 3;
	        		int length = tmpLength[i];
	        		int flag = 256;
	        		while(i-- > 0) {
	        			length += tmpLength[i] * flag;
	        			flag *= 256;
	        		}
	        		logger.debug("太行返回数据长度为\t" + length);
	        		byte[] tmpMsg = new byte[length];
	        		if(length > 0) {
	        			ris.read(tmpMsg);
	        			logger.debug("太行返回报文内容为 = " + printHexString(tmpMsg));
	        		}
	        		
	        		//读到信息组合，计算出报文长度，长度转换为HexString的byte，然后加上报文头
	        		/*
	        		 * src:源数组；	srcPos:源数组要复制的起始位置；
						dest:目的数组；	destPos:目的数组放置的起始位置；	length:复制的长度。
	        		 */
	        		byte[] retMsg = new byte[4 + tmpLength.length + tmpMsg.length];
	        		int retMsgLen = tmpLength.length + tmpMsg.length;
	        		byte[] retMsgLenHex = hexStringToBytes(Integer.toHexString(i));
	        		System.arraycopy(retMsgLenHex, 0, retMsg, 0, 4);
	        		System.arraycopy(tmpLength, 0, retMsg, 4, tmpLength.length);  
	        	    System.arraycopy(tmpMsg, 0, retMsg, 4 + tmpLength.length, tmpMsg.length);
	        	    //向中石油发送报文
	        	    logger.debug("返回中石油的报文为\t" + printHexString(retMsg));
	        	    sendMsgByByte(retMsgLenHex);
				}
				thSocket.close();
			} catch (IOException e) {
				logger.error("ProcessMsg thread run error");
			} finally {
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
			        			logger.debug( "从中石油接收的报文内容为 = " + printHexString(msg));
//			        			new Thread(new ProcessMsg(msg)).start();
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
		//线程池调度启动新线程
		//int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue
//		ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 200, 500, TimeUnit.MILLISECONDS,
//         		new LinkedBlockingQueue<Runnable>(100));
		PetroComm server = new PetroComm(petroServerIp, abcListenPort, petroListenPort);
		server.start();
		
//		executor.shutdown();
	}
}
