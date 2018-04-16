public class TestObject {
	
	private String merchname;
	private String transdate;
	
	public TestObject(String merchname,String transdate){
		
		this.merchname = merchname;
		this.transdate = transdate;
	}
	
	public String toString(){
		
		String str = merchname+transdate;
		return str;
	}

}
