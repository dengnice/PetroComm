public class TestForwarder {	
	
	private static String logPath = "E:\\forwarderlog.txt";
	
	public static void main(String[] args) {
		
		while(true){
			try {
				PrintLog.printFile(logPath,"取出前队列长度为 ：\t" + ReceiveMessageQueue.sizeMessage());
				Object sobj  = ReceiveMessageQueue.takeMessage();
				PrintLog.printFile(logPath,"取出后队列长度为 ：\t" + ReceiveMessageQueue.sizeMessage());
				/*PrintLog.printFile(logPath,"取到的消息类型是：\t" + sobj.getClass());
				PrintLog.printFile(logPath,"取到的消息是：\t" + sobj);
				
				TestObject robj = new TestObject("hello","201802");
				HandledMessageQueue.addMessage(robj);
				PrintLog.printFile(logPath, "添加的消息内容是：\t"+HandledMessageQueue.getElements()+"  "+"长度：\t"+HandledMessageQueue.sizeMessage());
				*/
			}catch (Exception e) {
				e.printStackTrace();
			}
			
//			TestObject sobj;
//			try {
//				sobj = HandledMessageQueue.takeMessage();
//				PrintLog.printFile(logPath,"取到的消息是：\t" + sobj.toString());
//
//			} catch (InterruptedException | IOException e) {
//				
//				e.printStackTrace();
//			}
		}
	}

}
