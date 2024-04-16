package SisopProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class DecryptThread {

	private static final int NUM_THREAD = 5;
	private static final int RANGE_MAX = Integer.MAX_VALUE;
	private static final String TARGET = "SISOP-corsoB";
	private static byte[] cryptedText = readTextInByte();
	private static AtomicBoolean found = new AtomicBoolean (false);
	
	private static byte [] readTextInByte() {
		byte[] text = {};
		try {
			text = Files.readAllBytes(Paths.get("C:\\Users\\frff1\\Desktop\\Seminario\\document2024_B.encrypted"));
		}catch(IOException e) {
			e.printStackTrace();
		}
		return text;
	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		Thread [] threadArray = new Thread [NUM_THREAD];
		int offset = RANGE_MAX/NUM_THREAD;
		
		for (int i = 0; i< NUM_THREAD; i++) {
			int startRange = i* offset;
			int endRange = Math.min((i+1)*offset, RANGE_MAX);
			threadArray[i] = new Thread (new RunnableSubclass(startRange, endRange));
			threadArray[i].start();
		}
		
		
		for (Thread t : threadArray) {
			try {
				t.join();
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		long endTime = System.currentTimeMillis();
        System.out.println("Tempo impiegato: " + (endTime - startTime) + " millisecondi");
	}
	//Ciclo for per suddividere il lavoro tra diversi thread + start di ognuno
	//ciclo for separato per fare la join
	
	static class RunnableSubclass implements Runnable{
		private final int inizio;
		private final int fine;
		
		public RunnableSubclass (int puntoIniziale, int puntoFinale) {
			this.inizio = puntoIniziale;
			this.fine = puntoFinale;
		}
		
		@Override
		public void run () {
			for (int key = inizio; key < fine && !found.getAcquire(); key++) {
				String paddedKey = formatKey(key);
				String decryptedText = decryptedText (paddedKey);
				System.out.println("Chiave provata: " + paddedKey);
				if (decryptedText != null && decryptedText.contains(TARGET)) {
					found = new AtomicBoolean (true);
					System.out.println("Chiave valida: " + paddedKey);
					break;
				}
			}
		}
		//Modifico AtomicBoolean in modo esclusivo, ho trovato la chiave
		
		//Padding della chiave -> se dovesse essere al di sotto dei 16 caratteri aggiunge gli 0 a sx
		private String formatKey(int key) {
			return String.format("%16s", key).replace(' ', '0');
		}
		
		
		
		//Viene decifrato e restituisce la stringa contenente il testo decifrato in modo da controllare se contiene "Sisop-corsoB"
        private String decryptedText(String key) {
            try {
                Cipher cipher = Cipher.getInstance("AES");
                Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decryptedBytes = cipher.doFinal(cryptedText);
                return new String(decryptedBytes);  
            } catch (NoSuchPaddingException | NoSuchAlgorithmException| InvalidKeyException | BadPaddingException
            		| IllegalBlockSizeException ex) {
                return null;
            }
        }
	}
	
}
