import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main   {
    private static final int PORT = 4221;

  public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        ExecutorService executor = Executors.newCachedThreadPool();
        serverSocket.setReuseAddress(true);
        while (true) {
            Socket socket = serverSocket.accept();
            executor.execute(new HandleConnection(socket));
            }
        }
    }

class HandleConnection implements Runnable{
    private Socket socket;
    public HandleConnection(Socket socket) {
        this.socket = socket;
    }
    public void run(){
            try {
                HandleClient(socket);
                socket.close();

            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
    }

    public static void HandleClient(Socket socket) {
        System.out.println("Accepted new connection");
        try {

            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            OutputStream outputStream = socket.getOutputStream();

            String userAgent = null;
            String host = null;
            String requestLine = reader.readLine();

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String trim = line.substring(line.indexOf(":") + 1).trim();
                if(line.toLowerCase().startsWith("user-agent:")){
                    userAgent = trim;
                } else if (line.toLowerCase().startsWith("host:")) {
                    host = trim;
                }
            }

            System.out.println("HTTP Method: " + requestLine);
            System.out.println("Host: " + host);
            System.out.println("User-Agent: " + userAgent);



            String[] requestLines = requestLine.split(" ");

            //String httpMethod = requestLines[0];
            String requestTarget = requestLines[1];
            //String httpVersion = requestLines[2];


            if (requestTarget.equals("/")){
                outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            } else if (requestTarget.contains("/echo") && requestTarget.length() > 5) {
                String echo = requestTarget.substring(requestTarget.lastIndexOf("/") + 1, requestTarget.length());
                System.out.println("echo: " + echo);
                String echoString = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 3\r\n\r\n" + echo;
                outputStream.write(echoString.getBytes());
            } else if(requestTarget.equals("/user-agent")){
                String userAgentString = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 12\r\n\r\n" + userAgent;
                outputStream.write(userAgentString.getBytes());
            }
            else {
                outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }

            outputStream.close();
            socket.close();



        }catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.out.println("Client Handling");
        }
    }
}
