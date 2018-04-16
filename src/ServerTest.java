import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ServerTest {

	private static String logPath = "D:\\serverlog.txt";
	private int port;
	private volatile boolean running = false;
	private long receiveTimeDelay = 3000;
	private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class,ObjectAction>();
	private Thread connWatchDog;
	private ThreadPoolExecutor executor;
	
	public ServerTest(int port, ThreadPoolExecutor executor) {
		this.port = port;
		this.executor = executor;
	}
	
	public ServerTest(int port) {
		this.port = port;
	}
	
	/**
	 * 要处理客户端发来的对象，并返回一个对象，可实现该接口。
	 */
	public interface ObjectAction{
		Object doAction(Object rev, ServerTest server);
	}
	
	
	public void start(){
		if(running) return;
		running = true;
		connWatchDog = new Thread(new ConnWatchDog());
		connWatchDog.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stop(){
		if(running) running = false;
		if(connWatchDog != null) connWatchDog.stop();
	}
	
	public static final class DefaultObjectAction implements ObjectAction{
		public Object doAction(Object rev,ServerTest server) {
//			System.out.println("处理并返回："+rev);
			try {
				PrintLog.printFile(logPath, "处理并返回：" + rev);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return rev;
		}
	}
	
	class ConnWatchDog implements Runnable{
		public void run(){
			try {
				ServerSocket ss = new ServerSocket(port,5);
				while(running){
					Socket s = ss.accept();
					new Thread(new SocketAction(s)).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				ServerTest.this.stop();
			}
			
		}
	}
	class ProcessMsg implements Runnable{
		private Object obj;
		public ProcessMsg(Object obj) {
			this.obj = obj;
		}
		public void run(){
			try {
				PrintLog.printFile(logPath, "处理消息：\t" + obj.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	class SocketAction implements Runnable{
		Socket s;
		boolean run=true;
		long lastReceiveTime = System.currentTimeMillis();
		public SocketAction(Socket s) {
			this.s = s;
		}
		public void run() {
			while(running && run){
				if(System.currentTimeMillis() - lastReceiveTime > receiveTimeDelay){
					overThis();
				}else{
					try {
						InputStream in = s.getInputStream();
						if(in.available() > 0){
							//接收消息头，按照长度截取消息
							ObjectInputStream ois = new ObjectInputStream(in);
							Object obj = ois.readObject();
							lastReceiveTime = System.currentTimeMillis();
							PrintLog.printFile(logPath, "接收的消息为：\t" + obj);
							
	//						ProcessMsg pMsg = new ProcessMsg(obj);
//							Thread thread = new Thread(new ProcessMsg(obj));
//							thread.start();
							new Thread(new ProcessMsg(obj)).start();
//							executor.execute(pMsg);
							
//							
//							Thread thread = new Thread(new ProcessMsg(obj));
//							thread.start();
//							ObjectAction oa = actionMapping.get(obj.getClass());
//							oa = oa==null?new DefaultObjectAction():oa;
//							Object out = oa.doAction(obj, ServerTest.this);
//							if(out != null){
//								ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
//								oos.writeObject(out);
//								oos.flush();
//							}
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
			if(run)run=false;
			if(s!=null){
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				PrintLog.printFile(logPath, "关闭："+s.getRemoteSocketAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
//			System.out.println("关闭："+s.getRemoteSocketAddress());
		}
		
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int port = 65433;
		boolean running = true;
		//线程池调度启动新线程
		//int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 200, 500, TimeUnit.MILLISECONDS,
         		new LinkedBlockingQueue<Runnable>(100));
		/*
		for(int i=0;i<105;i++){
			ProcessMsg pMsg = new ProcessMsg(i); 
			executor.execute(pMsg);
			PrintLog.printFile(logPath, "线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+
			executor.getQueue().size()+"，已执行完别的任务数目："+executor.getCompletedTaskCount());
		}
		ServerSocket ss = new ServerSocket(port);
		Socket s = ss.accept();
		InputStream in = s.getInputStream();
		*/
		
//		ServerTest server = new ServerTest(port, executor);
		ServerTest server = new ServerTest(port);
		server.start();
		
		executor.shutdown();
	}
}
