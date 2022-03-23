import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class Connection implements Runnable {
  private Socket socket;
  private PrintWriter outs;
  private String pseudo = null;

  private static String STOP_MESSAGE = "stop";
  private PublicKey pbKey = null;
  private Key desKey;

  public Connection(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      // Entrée
      BufferedReader ins = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      // Sortie
      outs = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

      // Lecture des messages du client
      while (true) {
        String line = ins.readLine();
        if (line == null)
          continue;

        // Si c'est le premier message, récupération de la clé publique envoyé par le
        // client
        if (pbKey == null) {
          System.out.println("Récupération de la clé publique RSA");
          X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(line)); // String to PublicKey
          KeyFactory keyFactory = KeyFactory.getInstance("RSA");
          pbKey = keyFactory.generatePublic(keySpec);

          // Création de la clé DES
          System.out.println("Génération de la clé DES");
          KeyGenerator keyGen = KeyGenerator.getInstance("DES");
          keyGen.init(56);
          desKey = keyGen.generateKey();
          byte[] keyB = desKey.getEncoded();
          byte[] desEncoded = encode(keyB, (Key) pbKey, "RSA"); // Encodage de la clé DES

          System.out.println("Envoie de la clé DES");
          outs.println(Base64.getEncoder().encodeToString(desEncoded)); // Envoi de la clé DES crypté au client
          continue;
        }

        // Pour tout message normal
        byte[] message = decode(Base64.getDecoder().decode(line.getBytes()), desKey, "DES"); // On décode en base 64
                                                                                             // puis on decode avec la
                                                                                             // clé DES
        String message_decoded = new String(message); // Bytes to String

        if (pseudo == null) {
          pseudo = message_decoded;
          Serveur.sendToAll(pseudo + " vient de se connecter"); // Envoi à tous les clients
        } else
          Serveur.sendToAll("[" + pseudo + "] " + message_decoded); // Envoi aux autres clients

        System.out.println("[" + pseudo + "] " + message_decoded); // Affichage du message décodé

        if (new String(message).equals(STOP_MESSAGE))
          break; // Arret du serveur si le message est "stop"
      }
      
      ins.close();
      outs.close();
      socket.close();
      System.out.println("Connection " + pseudo + " disconnected");
      Serveur.sendToAll(pseudo + " déconnecté");
      Serveur.removeConnection(this);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void send(String message) {
    try {
      byte[] encoded_message = encode(message.getBytes(), desKey, "DES"); // Encode le message avec la clé DES
      String base64_message = Base64.getEncoder().encodeToString(encoded_message); // Encode en base64
      outs.println(base64_message); // Envoi le message au client
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Fonction permettant d'encoder un message en fonction de la clé et du type
  // d'encodage
  public static byte[] encode(byte[] data, Key key, String mode) throws Exception {
    Cipher cipher = Cipher.getInstance(mode);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    byte[] bytes = cipher.doFinal(data);
    return bytes;
  }

  // Fonction permettant de décoder un message en fonction de la clé et du type
  // d'encodage
  public static byte[] decode(byte[] b, Key key, String mode) throws Exception {
    Cipher cipher = Cipher.getInstance(mode);
    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] bytes = cipher.doFinal(b);
    return bytes;
  }
}