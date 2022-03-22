import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import javax.crypto.Cipher;

class Client {
  static PrivateKey pvKey;
  static PublicKey pbKey;
  static Key key;

  public static void main(String[] args) throws Exception {
    String ip = (args.length == 1) ? args[0] : "localhost";
    int port = 6020;
    System.out.println("Connexion au serveur " + ip + ":" + port);
    
    Socket socket = new Socket(ip, port);
    BufferedReader ins = new BufferedReader(
    new InputStreamReader(socket.getInputStream()));
    PrintWriter outs = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

    System.out.println("Connecté, génération du couple des clé ...");
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.genKeyPair();

    pvKey = keyPair.getPrivate();
    pbKey = keyPair.getPublic();

    System.out.println("Clés générés, envoi de celle-ci ...");
    outs.println(Base64.getEncoder().encodeToString(pbKey.getEncoded()));

    System.out.println("Récupération de la clé DES crypté ...");
    String DESEncodedKey = ins.readLine();

    System.out.println("Décriptage de la clé ...");
    byte[] decodedDES = Base64.getDecoder().decode(DESEncodedKey.getBytes());
    decodedDES = decode(decodedDES, pvKey, "RSA");
    key = new SecretKeySpec(decodedDES, 0, decodedDES.length, "DES");
    
    System.out.println("Entrez votre message (\"stop\" pour quitter):");
    
    String line;
    while ((line = new BufferedReader(new InputStreamReader(System.in)).readLine()) != null) {
      byte[] encoded_message = encode(line.getBytes(), key, "DES");
      String base64_message = Base64.getEncoder().encodeToString(encoded_message);
      outs.println(base64_message);

      String serveur_message = ins.readLine();
      byte[] base64_decode = Base64.getDecoder().decode(serveur_message);
      byte[] decoded = decode(base64_decode, key, "DES");
      System.out.println("Server: " + new String(decoded));
      if (line.equals("stop")) break;
    }
    
    outs.println("stop");
    ins.close();
    outs.close();
    socket.close();
  }

  public static byte[] encode(byte[] data, Key key, String mode) throws Exception {
    Cipher cipher = Cipher.getInstance(mode);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    byte[] bytes = cipher.doFinal(data);
    return bytes;
  }

  public static byte[] decode(byte[] b, Key key, String mode) throws Exception {
    Cipher cipher = Cipher.getInstance(mode);
    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] bytes = cipher.doFinal(b);
    return bytes;
  }
}