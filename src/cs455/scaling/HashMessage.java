package cs455.scaling;
import java.math.BigInteger;
import java.util.Random;
import java.security.MessageDigest;


public class HashMessage {

	private byte[] randomBytes = new byte[8000];
	private String hashedString = "";

	public HashMessage(){
		Random rand = new Random();
		rand.nextBytes(randomBytes);
		hashedString = SHA1FromBytes(randomBytes);
		//System.out.println("Generated new random byte array: " + randomBytes + ", with hashString: " + hashedString);
	}

	public HashMessage(byte[] randomBytes){
		this.randomBytes = randomBytes;
		hashedString = SHA1FromBytes(randomBytes);
		//System.out.println("Used byte array: " + randomBytes + ", to generate hashString: " + hashedString);
	}

	public HashMessage(String unHashedString){
		this.randomBytes = unHashedString.getBytes();
		hashedString = SHA1FromBytes(randomBytes);
		//System.out.println("Used String: " + unHashedString + ", to generate hashString: " + hashedString);
	}

	public byte[] getByteArray(){
		return randomBytes;
	}

	public String getHashedString(){
		return hashedString;
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

