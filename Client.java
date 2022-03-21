import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

class Client {
  public static void main(String[] args) throws Exception {
    String ip = (args.length == 1) ? args[0] : "localhost";
    int port = 6020;
    System.out.println("Connexion au serveur " + ip + ":" + port);
    
    Socket socket = new Socket(ip, port);
    BufferedReader ins = new BufferedReader(
    new InputStreamReader(socket.getInputStream()));
    PrintWriter outs = new PrintWriter(new     OutputStreamWriter(socket.getOutputStream()), true);
    System.out.println("Entrez votre message (\"stop\" pour quitter):");
    
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
}