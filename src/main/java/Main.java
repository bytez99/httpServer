import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Main {
  public static void main(String[] args) {
    while(true) {
        try {

            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            Socket socket = serverSocket.accept();
            HandleClient(socket);
            serverSocket.close();


        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.out.println("Server Socket");
        }
    }

  }

  public static void HandleClient(Socket socket) {
      System.out.println("Accepted new connection");
      try {

          InputStream inputStream = socket.getInputStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

          OutputStream outputStream = socket.getOutputStream();

          String requestLine = reader.readLine();
          String[] requestLines = requestLine.split(" ");

//          for (int i = 0; i < requestLines.length; i++) {
//              System.out.println(requestLines[i]);
//          }

          if (Objects.equals(requestLines[1],"/")){
              outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
          }else {
              outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
          }


          socket.close();



      }catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
          System.out.println("Client Handling");
      }
  }

}
