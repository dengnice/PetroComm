import java.util.concurrent.LinkedBlockingQueue;

/**
 *	
 * 	@author 
 */
public class ReceiveMessageQueue {
	
	private static LinkedBlockingQueue<Object> lbq = new LinkedBlockingQueue<Object>(200);
	/**
	 *	
	 * 	@author 
	 *  @param String message(接收到的信息)
	 *
	 */
	public static void addMessage(Object robj){
		
		try{
			
			lbq.put(robj);
		}catch(InterruptedException e){
			
			e.printStackTrace();
		}catch(Exception ex){
			
			ex.printStackTrace();
		}
	}
	
	public static Object takeMessage() throws InterruptedException{
		Object obj = null;
         try{
			
        	 obj = lbq.poll();
		}catch(Exception ex){
			
			ex.printStackTrace();
		}
        return obj;
	}
	
	public static int sizeMessage(){
		
		int elmn = lbq.size();
		return elmn;
	}
	
	public static Object getElements(){
		
		Object to = lbq.element();
		return to;
	}

}
