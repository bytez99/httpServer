import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;


public class Main   {
    private static final int PORT = 4221;
    private static int connectionCount = 0;



  public static void main(String[] args) {
      try {

          ServerSocket serverSocket = new ServerSocket(PORT);
          ExecutorService executor = Executors.newCachedThreadPool();
          serverSocket.setReuseAddress(true);
          while (true) {
              Socket socket = serverSocket.accept();
              System.out.println("Connection: #" + connectionCount++);
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

            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
    }

    public static byte[] Compress(String data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try(GZIPOutputStream gzipSteam = new GZIPOutputStream(byteStream)){
            gzipSteam.write(data.getBytes(StandardCharsets.UTF_8));
            gzipSteam.finish();
        }

        return byteStream.toByteArray();
    }


    public static void HandleClient(Socket socket) throws IOException {
        System.out.println("New connection accepted");
        System.out.println("Client: " + socket.getRemoteSocketAddress() + "\n");


        try {


            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            OutputStream outputStream = socket.getOutputStream();

            int requestCount = 0;

            boolean isPersistent = true;
            String connectionPersistence = "Connection: keep-alive";

            while(isPersistent) {





                String userAgent = null;
                String host = null;
                String contentType = null;
                String acceptEncoding = null;
                String requestLine = reader.readLine();
                if (requestLine == null){
                    break;
                }
                requestCount++;
                String[] requestLines = requestLine.split(" ");
                String httpMethod = requestLines[0];
                String requestTarget = requestLines[1];
                String httpVersion = requestLines[2];
                int contentLength = 0;


                if (httpVersion.equals("HTTP/1.1")) {
                    isPersistent = true;
                    connectionPersistence = "Connection: keep-alive";


                }else{
                    isPersistent = false;
                }


                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    String trim = line.substring(line.indexOf(":") + 1).trim();
                    if (line.toLowerCase().startsWith("user-agent:")) {
                        userAgent = trim;
                    } else if (line.toLowerCase().startsWith("host:")) {
                        host = trim;
                    } else if (line.toLowerCase().startsWith("content-length")) {
                        contentLength = Integer.parseInt(trim);
                    } else if (line.toLowerCase().startsWith("content-type")) {
                        contentType = trim;

                    } else if (line.toLowerCase().startsWith("accept-encoding")) {
                        acceptEncoding = trim;
                    } else if (line.toLowerCase().startsWith("connection")) {
                        if (line.contains("keep-alive")) {
                            isPersistent = true;
                            connectionPersistence = "Connection: keep-alive";
                        } else if (line.contains("close")) {
                            isPersistent = false;
                            connectionPersistence = "Connection: close";

                        }
                    }
                }



                StringBuilder body = new StringBuilder();
                if (contentLength > 0) {
                    char[] buffer = new char[contentLength];
                    reader.read(buffer, 0, contentLength);
                    body.append(buffer);
                }





                String requestBody = body.toString();
                System.out.println("REQUEST START: #" + requestCount);
                System.out.println("\nRequest Line");
                System.out.println(httpMethod + " " + requestTarget + " " + httpVersion);

                System.out.println("\nHeaders");
                System.out.println("Host:" + host);
                System.out.println("User-Agent:" + userAgent);
                System.out.println("Accept-Encoding:" + acceptEncoding);
                System.out.println("Content-Type:" + contentType);
                System.out.println("Content-Length:\n" + contentLength);

                System.out.println("\nBody");
                System.out.println("Request Body: " + requestBody);
                System.out.println("\nREQUEST FINISH: #" + requestCount + "\n");


                // POST request body to a file
                if (httpMethod.equals("POST") && requestTarget.contains("/files") && requestTarget.length() > 6) {
                    try {
                        System.out.println("requestTarget: " + requestTarget);
                        File file = new File(requestTarget.substring(7, requestTarget.length()));
                        if (file.createNewFile()) {
                            FileWriter fileWriter = new FileWriter(file);
                            fileWriter.write(requestBody);
                            fileWriter.close();
                            String response = "HTTP/1.1 201 Created\r\nContent-Length: 0" + "\n\n";
                            outputStream.write(response.getBytes());
                            outputStream.flush();

                        } else {
                            String response = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\n\n";
                            outputStream.write(response.getBytes());
                        }

                    } catch (IOException e) {
                        System.out.println("IO Exception: " + e.getMessage());
                        String response = "HTTP/1.1 404 Not Found\r\n\n\n";
                        outputStream.write(response.getBytes());
                    }

                }

                // Get request with empty target
                else if (requestTarget.equals("/")) {

                    outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());


                    // Sends HTTP response of the first 3 characters from request line
                } else if (requestTarget.contains("/echo") && requestTarget.length() > 5) {
                    String echo;
                    int ourIndex = 0;
                    try {
                        echo = requestTarget.substring(requestTarget.lastIndexOf("/") + 1, requestTarget.lastIndexOf("/") + 4);

                    } catch (StringIndexOutOfBoundsException e) {
                        // Gets starting index from exception message
                        int startIndex = e.getMessage().lastIndexOf("length") + 7;
                        // Last index
                        int endIndex = startIndex + 1;
                        // Create substring, parse to int
                        ourIndex = Integer.parseInt(e.getMessage().substring(startIndex, endIndex));
                        // Get our new string echo to respond with
                        echo = requestTarget.substring(requestTarget.lastIndexOf("/") + 1, ourIndex);


                    }

                    String echoString;
                    // If Accept-Encoding header is gzip
                    if (acceptEncoding != null && acceptEncoding.contains("gzip")) {

                        byte[] compressedEcho = Compress(echo);


                        echoString = "HTTP/1.1 200 OK\r\nContent-Encoding: gzip\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: " + compressedEcho.length + "\r\n" + connectionPersistence + "\r\n\r\n";
                        outputStream.write(echoString.getBytes(StandardCharsets.UTF_8));
                        // Have to send separately.
                        outputStream.write(compressedEcho);
                        //outputStream.flush();
                    } else {
                        //System.out.println("echo: " + echo);
                        echoString = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: " + echo.length() + "\r\n" + connectionPersistence + "\r\n\r\n" + echo;
                        outputStream.write(echoString.getBytes(StandardCharsets.UTF_8));
                        //outputStream.flush();
                    }


                    // Sends HTTP response with client user-agent
                } else if (requestTarget.equals("/user-agent")) {
                    byte[] bytes = userAgent.getBytes(StandardCharsets.UTF_8);
                    String userAgentString = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: " + bytes.length + "\r\n\r\n" + userAgent;
                    outputStream.write(userAgentString.getBytes());
                    //outputStream.flush();
                    // Responds with file requested if existed, reads file back to user.
                } else if (requestTarget.contains("/files") && requestTarget.length() > 5 && httpMethod.equals("GET")) {

                    String filename = requestTarget.substring(requestTarget.lastIndexOf("/") + 1);
                    File file = new File(filename);

                    if (file.exists()) {

                        byte[] fileContent = Files.readAllBytes(Paths.get(filename));
                        String response = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: 13\r\n\r\n" + new String(fileContent);
                        outputStream.write(response.getBytes());
                    } else {

                        outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                    }
                }
                // Responds with 404 not found, if invalid request.
                else {
                    outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }




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
