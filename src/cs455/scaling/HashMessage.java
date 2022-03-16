package cs455.scaling;
import java.math.BigInteger;
import java.util.Random;
import java.security.MessageDigest;


public class HashMessage {

	private byte[] randomBytes = new byte[8 * Constants.KB];
	private String hashedString = "";

	public HashMessage(){
		Random rand = new Random();
		rand.nextBytes(randomBytes);
		hashedString = SHA1FromBytes(randomBytes);
	}

	public HashMessage(byte[] randomBytes){
		this.randomBytes = randomBytes;
		hashedString = SHA1FromBytes(randomBytes);
	}

	public HashMessage(String unHashedString){
		this.randomBytes = unHashedString.getBytes();
		hashedString = SHA1FromBytes(randomBytes);
	}

	public byte[] getByteArray(){
		return randomBytes;
	}

	public String getHashedString(){
		return hashedString;
	}

	public String bytesToString(byte[] inputBytes){
		String byteString = "";
		for (byte b: inputBytes){
			byteString += b;
		}
		return byteString;

	}

	public String SHA1FromBytes(byte[] data) {
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA1"); 
	    		byte[] hash  = digest.digest(data); 
	    		BigInteger hashInt = new BigInteger(1, hash); 
			return hashInt.toString(16); 
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		// Control should not reach here, return null if it does.
		return null;

	}


}

