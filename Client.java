import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

class Client {
  static PrivateKey pvKey;
  static PublicKey pbKey;
  static PublicKey encodeKey;

  public static void main(String[] args) throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.genKeyPair();

    pvKey = keyPair.getPrivate();
    pbKey = keyPair.getPublic();

    String ip = (args.length == 1) ? args[0] : "localhost";
    int port = 6020;
    System.out.println("Connexion au serveur " + ip + ":" + port);
    
    Socket socket = new Socket(ip, port);
    BufferedReader ins = new BufferedReader(
    new InputStreamReader(socket.getInputStream()));
    PrintWriter outs = new PrintWriter(new     OutputStreamWriter(socket.getOutputStream()), true);
    System.out.println("Entrez votre message (\"stop\" pour quitter):");
    
    outs.println(pbKey.getEncoded().toString());
    String keyString = ins.readLine();

    X509EncodedKeySpec  keySpec = new X509EncodedKeySpec(keyString.getBytes());
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");	  
    encodeKey = keyFactory.generatePublic(keySpec);
    // TODO: Verifier la vérification de la clé

    // Read console input and send to sever
    String line;
    while ((line = new BufferedReader(new InputStreamReader(System.in)).readLine()) != null) {
      outs.println(line);
      System.out.println("Server: " + ins.readLine());
      if (line.equals("stop")) break;
    }
    
    outs.println("stop");
    ins.close();
    outs.close();
    socket.close();
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