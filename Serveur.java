import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Serveur {

  static List<Connection> connections = new ArrayList<Connection>();

  public static void main(String[] args) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    // Création de la socket sur le port 6020
    ServerSocket serverSocket = new ServerSocket(6020);
    System.out.println("Serveur démarré, en attente d'un client ...");

    // Création d'un thread permettant d'envoyer des messages depuis le serveur vers
    // les clients
    Thread t = new Thread() {
      public void run() {
        try {
          String line;
          while ((line = new BufferedReader(new InputStreamReader(System.in)).readLine()) != null) { // Lecture de
                                                                                                     // l'entrée clavier
            sendToAll("[Server] " + line); // Envoi à tous les clients
            if (line.equals("stop"))
              break;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();

    while (true) {
      Socket s = serverSocket.accept(); // Accepte la connexion du client
      Connection con = new Connection(s); // Creer un thread de la classe Connection
      executor.execute(con); // Execute ce thread
      connections.add(con); // Ajout à la liste des differentes connexions
    }

    // Fermeture de la sockets
    // serverSocket.close();
  }

  // Envoi d'un message à tous les clients
  public static void sendToAll(String message) {
    for (int i = 0; i < connections.size(); i++) {
      connections.get(i).send(message);
    }
  }

  // Supression d'une connection de la liste
  public static void removeConnection(Connection connection) {
    connections.remove(connection);
  }

}
