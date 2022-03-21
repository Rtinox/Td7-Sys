import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;


class Serveur {
  
  static String STOP_MESSAGE = "stop";
  static PrivateKey pvKey;
  static PublicKey pbKey;
  static PublicKey encodeKey;
  
  public static void main(String[] args) throws Exception {

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.genKeyPair();

    pvKey = keyPair.getPrivate();
    pbKey = keyPair.getPublic();

    ServerSocket serverSocket = new ServerSocket(6020);
    System.out.println("START");
    Socket socket = serverSocket.accept();
    BufferedReader ins = new BufferedReader(
      new InputStreamReader(socket.getInputStream()));
    
    PrintWriter outs = new PrintWriter(new BufferedWriter(
      new OutputStreamWriter(socket.getOutputStream())), true);
    
    
    while (true) {
      String line = ins.readLine();
      if(line == null) continue;
      System.out.println("Reçu: " + line);
      outs.println(line);
      if(line.equals(STOP_MESSAGE)) break;
    }
    
    ins.close();
    outs.close();
    socket.close();
    serverSocket.close();
  }

  public static byte[] encode(byte[] data, Key key) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key); 

    byte[] bytes = cipher.doFinal(data);
    return bytes;
  }

  public static byte[] decode(byte[] b, Key key) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, key); 

    byte[] bytes = cipher.doFinal(b);
    return bytes;
  }
}