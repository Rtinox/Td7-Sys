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
  static String STOP_MESSAGE = "stop";

  public static void main(String[] args) throws Exception {
    // Récupération de l'ip si mise en argument.
    String ip = (args.length == 1) ? args[0] : "localhost";
    int port = 6020;
    System.out.println("Connexion au serveur " + ip + ":" + port);

    // Création de la socket, du buffer d'entrée et de sortie.
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
    outs.println(Base64.getEncoder().encodeToString(pbKey.getEncoded())); // Envoie au serveur.

    System.out.println("Récupération de la clé DES crypté ..."); 
    String DESEncodedKey = ins.readLine(); // Récupération depuis le serveur.

    System.out.println("Décriptage de la clé ...");
    byte[] decodedDES = Base64.getDecoder().decode(DESEncodedKey.getBytes());
    decodedDES = decode(decodedDES, pvKey, "RSA");
    key = new SecretKeySpec(decodedDES, 0, decodedDES.length, "DES");

    // Création d'un Thread pour la réception des messages.
    Thread t = new Thread() {
      public void run() {
        try {
          while (true) {
            String line = ins.readLine(); // Lecture de l'entrée.
            if (line == null)
              continue;
            // Décode en base64 puis décode avec la clé DES
            byte[] message = decode(Base64.getDecoder().decode(line.getBytes()), key, "DES");
            String message_decoded = new String(message);
            System.out.println(message_decoded);
            if (new String(message).equals(STOP_MESSAGE))
              break;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();

    System.out.println("Entrez votre message (\"stop\" pour quitter):");

    // Envoie d'un message vers le serveur.
    String line;
    while ((line = new BufferedReader(new InputStreamReader(System.in)).readLine()) != null) {
      // Lecture de la saisie clavier puis encodage DES.
      byte[] encoded_message = encode(line.getBytes(), key, "DES");
      String base64_message = Base64.getEncoder().encodeToString(encoded_message);
      outs.println(base64_message); // Envoie au serveur.

      if (line.equals("stop")) break;
    }

    // Fermeture des sockets.
    t.interrupt();
    outs.println("stop");
    ins.close();
    outs.close();
    socket.close();
  }

  // Fonction permettant d'encoder un message en fonction de la clé et du type d'encodage
  public static byte[] encode(byte[] data, Key key, String mode) throws Exception {
    Cipher cipher = Cipher.getInstance(mode);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    byte[] bytes = cipher.doFinal(data);
    return bytes;
  }

    // Fonction permettant de décoder un message en fonction de la clé et du type d'encodage
  public static byte[] decode(byte[] b, Key key, String mode) throws Exception {
    Cipher cipher = Cipher.getInstance(mode);
    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] bytes = cipher.doFinal(b);
    return bytes;
  }
}