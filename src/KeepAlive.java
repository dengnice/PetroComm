import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * 维持连接的消息对象（心跳对象）
 */
public class KeepAlive implements Serializable{

	private static final long serialVersionUID = -2813120366138988480L;
	private static byte[] b;
	private String hexString = "000000040000012d";
	/* 覆盖该方法，仅用于测试使用。
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		return "0000012c012a062781206108002301085020c000c19000100211000001313030303030303031303"
//				+ "2000037202020204368616756783531304320202031373031313530303136303532303030303"
//				+ "530333132303630343134303032323030323132303630343134303032320148930131a10f303"
//				+ "030303030303030303030303030a20c313931313135303130313032ab01315b1e31303030303"
//				+ "03030303030303030303030303131202020202020202020205c1e30303030303030303031313"
//				+ "03031312020202020202020202020202020205d04313120207f0010462d504f534320332e363"
//				+ "62e30322e307f0213202020202020203238333632383739353035370037462d504f534120412"
//				+ "e502e372e3531363035323030303035303430383031323330303031342b2dfd86d7ad1e80";
		return "test";
	}
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
	public byte[] toByte() {
		this.b = hexStringToBytes(this.hexString);
		return this.b;
	}
}

