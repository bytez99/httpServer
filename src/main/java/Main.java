import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main   {
    private static final int PORT = 4221;

  public static void main(String[] args) {
      try {
          ServerSocket serverSocket = new ServerSocket(PORT);
          ExecutorService executor = Executors.newCachedThreadPool();
          serverSocket.setReuseAddress(true);
          while (true) {
              Socket socket = serverSocket.accept();
              executor.execute(new HandleConnection(socket));
          }
      }catch (IOException e){
          System.out.println("IO Exception" + e.getMessage());
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
            String contentType = null;
            String acceptEncoding = null;
            String requestLine = reader.readLine();
            String[] requestLines = requestLine.split(" ");
            String httpMethod = requestLines[0];
            String requestTarget = requestLines[1];
            String httpVersion = requestLines[2];
            int contentLength = 0;

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String trim = line.substring(line.indexOf(":") + 1).trim();
                if(line.toLowerCase().startsWith("user-agent:")){
                    userAgent = trim;
                } else if (line.toLowerCase().startsWith("host:")) {
                    host = trim;
                }else if (line.toLowerCase().startsWith("content-length")){
                    contentLength = Integer.parseInt(trim);
                }else if (line.toLowerCase().startsWith("content-type")){
                    contentType = trim;

                }else if (line.toLowerCase().startsWith("accept-encoding")){
                    acceptEncoding = trim;
                }
            }

            StringBuilder body = new StringBuilder();
            if (contentLength > 0){
                char[] buffer = new char[contentLength];
                reader.read(buffer, 0, contentLength);
                body.append(buffer);
            }

            String requestBody = body.toString();

            System.out.println("");
            System.out.println("Request Line");
            System.out.println(httpMethod + " " + requestTarget + " " + httpVersion);

            System.out.println("Headers:");
            System.out.println("Host:" + host);
            System.out.println("User-Agent:" + userAgent);
            System.out.println("Accept-Encoding:" + acceptEncoding);
            System.out.println("Content-Type:" + contentType);
            System.out.println("Content-Length:" + contentLength);

            System.out.println("Body:");
            System.out.println("requestBody: " + requestBody);



            // POST request body to a file
            if(httpMethod.equals("POST") && requestTarget.contains("/files") && requestTarget.length() > 6){
                try {
                    System.out.println("requestTarget: " + requestTarget);
                    File file = new File(requestTarget.substring(7, requestTarget.length()));
                    if (file.createNewFile()){
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(requestBody);
                        fileWriter.close();
                        String response = "HTTP/1.1 201 Created\r\n\n\n";
                        outputStream.write(response.getBytes());

                    }else {
                        String response = "HTTP/1.1 404 Not Found\r\n\n\n";
                        outputStream.write(response.getBytes());
                    }

                }catch (IOException e){
                    System.out.println("IO Exception: " + e.getMessage());
                    String response = "HTTP/1.1 404 Not Found\r\n\n\n";
                    outputStream.write(response.getBytes());
                }

            }

            // Get request with empty target
            else if (requestTarget.equals("/")){

                outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());


            // Sends HTTP response of the first 3 characters from request line
            } else if (requestTarget.contains("/echo") && requestTarget.length() > 5) {
                String echo = requestTarget.substring(requestTarget.lastIndexOf("/") + 1, requestTarget.length());
                String echoString;

                if (acceptEncoding != null && acceptEncoding.contains("gzip")){
                    echoString = "HTTP/1.1 200 OK\r\nContent-Encoding: gzip\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 3\r\n\r\n" + echo;
                }else {
                    //System.out.println("echo: " + echo);
                     echoString = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 3\r\n\r\n" + echo;

                }

                outputStream.write(echoString.getBytes());
            // Sends HTTP response with client user-agent
            } else if(requestTarget.equals("/user-agent")){
                byte[] bytes = userAgent.getBytes(StandardCharsets.UTF_8);
                String userAgentString = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: " + bytes.length +"\r\n\r\n" + userAgent;
                outputStream.write(userAgentString.getBytes());
            // Responds with file requested if existed, reads file back to user.
            }else if(requestTarget.contains("/files") && requestTarget.length() > 5 && httpMethod.equals("GET")){

                String filename = requestTarget.substring(requestTarget.lastIndexOf("/") + 1);
                File file = new File(filename);

                if (file.exists()){

                    byte[] fileContent = Files.readAllBytes(Paths.get(filename));
                    String response = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: 13\r\n\r\n"+new String(fileContent);
                    outputStream.write(response.getBytes());
                }else {

                    outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }
            }
            // Responds with 404 not found, if invalid request.
            else {
                outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }

            outputStream.flush();
            outputStream.close();
            socket.close();



        }catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.out.println("Client Handling");
        }
    }
}
