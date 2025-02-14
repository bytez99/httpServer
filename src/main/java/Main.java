import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {

     try {
       ServerSocket serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);

       HandleClient(serverSocket.accept());

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }


  }

  public static void HandleClient(Socket socket) {
      System.out.println("Accepted new connection");
      try {

          socket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
          socket.close();


      }catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
      }
  }

}
