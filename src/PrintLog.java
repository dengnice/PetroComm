import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * 写入文件
 * @throws IOException 
 */
public class PrintLog implements Serializable{

	private static final long serialVersionUID = -5752322023690048832L;
	
	public static void printFile(String filePath, String content) throws IOException{
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath),true));
	        writer.write("\n"+content);
	        writer.close();
		}catch(Exception e){
			
		}
	}
}
