

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

/**
 *	C/S架构的客户端对象，持有该对象，可以随时向服务端发送消息。
 * 	@author denglz
 */
public class ClientTest {

	private static String logPath = "D:\\clientlog.txt";
	/**
	 * 处理服务端发回的对象，可实现该接口。
	 */
	public static interface ObjectAction{
		void doAction(Object obj,ClientTest client);
	}
	
	public static final class DefaultObjectAction implements ObjectAction{
		public void doAction(Object obj,ClientTest client) {
//			System.out.println("处理：\t"+obj.toString());
			try {
				PrintLog.printFile(logPath, "处理：\t"+obj.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		String serverIp = "127.0.0.1";
		int port = 65433;
		ClientTest client = new ClientTest(serverIp,port);
		client.start();
	}
	
	private String serverIp;
	private int port;
	private Socket socket;
	private boolean running = false; //连接状态
	
	private long lastSendTime; //最后一次发送数据的时间
	
	//用于保存接收消息对象类型及该类型消息处理的对象
	private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class,ObjectAction>();
	
	public ClientTest(String serverIp, int port) {
		this.serverIp=serverIp;
		this.port=port;
	}
	
	public void start() throws UnknownHostException, IOException {
		if(running)return;
		socket = new Socket(serverIp,port);
//		System.out.println("本地端口："+socket.getLocalPort());
		PrintLog.printFile(logPath, "本地端口："+socket.getLocalPort());
		lastSendTime=System.currentTimeMillis();
		running=true;
		new Thread(new KeepAliveWatchDog()).start();  //保持长连接的线程，每隔2秒向服务器发一个一个保持连接的心跳消息
		new Thread(new ReceiveWatchDog()).start();    //接受消息的线程，处理消息
	}
	
	public void stop(){
		if(running)running=false;
	}
	
	/**
	 * 添加接收对象的处理对象。
	 * @param cls 待处理的对象，其所属的类。
	 * @param action 处理过程对象。
	 */
	public void addActionMap(Class<Object> cls,ObjectAction action){
		actionMapping.put(cls, action);
	}

	public void sendObject(Object obj) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(obj);
//		System.out.println("发送：\t"+obj);
		PrintLog.printFile(logPath, "发送：\t"+obj);
		oos.flush();
	}
	/**
	 * 维持长连接
	 * @author denglz
	 *
	 */
	class KeepAliveWatchDog implements Runnable{
		long checkDelay = 10;
		long keepAliveDelay = 2000;
		public void run() {
			while(running){
				if(System.currentTimeMillis()-lastSendTime>keepAliveDelay){
					try {
						ClientTest.this.sendObject(new KeepAlive());
					} catch (IOException e) {
						e.printStackTrace();
						ClientTest.this.stop();
					}
					lastSendTime = System.currentTimeMillis();
				}else{
					try {
						Thread.sleep(checkDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
						ClientTest.this.stop();
					}
				}
			}
		}
	}
	
	/**
	 * 接收返回
	 * @author denglz
	 *
	 */
	class ReceiveWatchDog implements Runnable{
		public void run() {
			while(running){
				try {
					InputStream in = socket.getInputStream();
					if(in.available() > 0){
						ObjectInputStream ois = new ObjectInputStream(in);
						Object obj = ois.readObject();
//						System.out.println("接收：\t"+obj);
						PrintLog.printFile(logPath, "接收：\t"+obj);
						ObjectAction oa = actionMapping.get(obj.getClass());
						oa = oa==null?new DefaultObjectAction():oa;
						oa.doAction(obj, ClientTest.this);
					}else{
						Thread.sleep(10);
					}
				} catch (Exception e) {
					e.printStackTrace();
					ClientTest.this.stop();
				} 
			}
		}
	}
	
}
