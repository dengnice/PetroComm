public class ProcessMsg {
	
	/**
	 * HexString -> bytes
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
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
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	/** 
	 * bytes -> String
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	/**
	 * byte -> HexString
	 * @param b
	 * @return
	 */
	public String printHexString( byte[] b) {
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
    
	public static void main(String[] args) {
		/*
		String hexString = "0000012c";
		byte[] tmpByte = hexStringToBytes(hexString);
		int i = 3;
		int length = tmpByte[i];
		int flag = 256;
		while(i-- > 0) {
			length += tmpByte[i] * flag;
			flag *= 256;
		}
		System.err.println("tmpByte = " + length);
		String tmpStr = bytesToHexString(tmpByte);
		System.err.println("tmpStr = " + tmpStr);
		
		System.err.println(Integer.toHexString(300));*/
		int i = 256;
		
        System.out.println("2.10进制转字节:" + hexStringToBytes(Integer.toHexString(i)));
        
		
	}
}
