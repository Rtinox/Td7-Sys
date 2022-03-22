import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

class Serveur {

  static String STOP_MESSAGE = "stop";
  static PublicKey pbKey = null;
  static Key desKey;

  public static void main(String[] args) throws Exception {
    ServerSocket serverSocket = new ServerSocket(6020);
    System.out.println("START");
    Socket socket = serverSocket.accept();
    BufferedReader ins = new BufferedReader(
        new InputStreamReader(socket.getInputStream()));

    PrintWriter outs = new PrintWriter(new BufferedWriter(
        new OutputStreamWriter(socket.getOutputStream())), true);

    Thread t = new Thread() {
      public void run() {
        try
        {
          String line;
          while ((line = new BufferedReader(new InputStreamReader(System.in)).readLine()) != null) {
            byte[] encoded_message = encode(line.getBytes(), desKey, "DES");
            String base64_message = Base64.getEncoder().encodeToString(encoded_message);
            outs.println(base64_message);
  
            /*String serveur_message = ins.readLine();
            byte[] base64_decode = Base64.getDecoder().decode(serveur_message);
            byte[] decoded = decode(base64_decode, desKey, "DES");
            System.out.println("Server: " + new String(decoded));*/
            if (line.equals("stop")) break;
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    };
    t.start();

    while (true) {
      String line = ins.readLine();
      if (line == null)
        continue;
      if (pbKey == null) {
        System.out.println("Récupération de la clé publique RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(line));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        pbKey = keyFactory.generatePublic(keySpec);

        // 2 . Création de la clé DES
        System.out.println("Génération de la clé DES");
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        keyGen.init(56);
        desKey = keyGen.generateKey();
        byte[] keyB = desKey.getEncoded();
        byte[] desEncoded = encode(keyB, (Key) pbKey, "RSA");

        System.out.println("Envoie de la clé DES");
        outs.println(Base64.getEncoder().encodeToString(desEncoded));
        continue;
      }
      byte[] message = decode(Base64.getDecoder().decode(line.getBytes()), desKey, "DES");
      String message_decoded = new String(message);
      System.out.println("Reçu: " + message_decoded);
      //outs.println(Base64.getEncoder().encodeToString(encode(message_decoded.getBytes(), desKey, "DES")));
      if (new String(message).equals(STOP_MESSAGE))
        break;
    }

    ins.close();
    outs.close();
    socket.close();
    serverSocket.close();
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