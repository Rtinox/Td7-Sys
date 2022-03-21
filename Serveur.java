import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


class Serveur {
  
  static String STOP_MESSAGE = "stop";
  
  public static void main(String[] args) throws Exception {
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
}