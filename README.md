# Simple HTTP Server

A lightweight HTTP server built in Java that handles basic HTTP requests, supports file operations, and manages concurrent connections.

## Features

- **GET /user-agent**: Returns the client's `User-Agent` string.
- **GET /files/{filename}**: Serves the requested file from the server's directory.
- **POST /files/{filename}**: Creates and writes to a file (if it does not exist) with the request body. Responds with `201 Created`.
- **GET /echo/{string}**: Returns the string back in the response body (if at least 3 characters long) with `200 OK`.
- **Concurrent Connections**: Handles multiple requests simultaneously.
- **Root Path (`/`)**: Responds with `200 OK` for requests without a specific path.
- **404 Not Found**: Returns `404 Not Found` for any invalid or unsupported routes.
- **GZip Encoding**: Responds with `Content-Encoding: gzip` with a compressed body, when request header `Accept-Content: gzip` is enabled for /echo route.
- **Persistent Connections**: Will automatically use persistent connections with http1.1 unless the `Connection: close` header says otherwise.
- **Multiple Persisent Connections**: Will handle multiple persistent connections at the same time.

## Installation & Running

1. **Clone the repository:**
   ```sh
   git clone https://github.com/bytez99/httpServer.git
   cd httpServer
   ```
2. **Compile the Java files:**
   ```sh
   javac Main.java
   ```
3. **Run the server:**
   ```sh
   java Main
   ```

## Example Usage

- Fetch the User-Agent:
  ```sh
  curl -v http://localhost:4221/user-agent
  ```
- Retrieve a file:
  ```sh
  curl -v http://localhost:4221/files/foo --output output
  ```
- Create a file with a POST request:
  ```sh
  curl -v --data "hello world" -H "Content-Type: application/octet-stream" http://localhost:4221/files/hello
  ```
  Responds with 201 Created.

- Echo a string (min. 3 characters):
  ```sh
  curl -v http://localhost:4221/echo/hello
  ```
- Handle an invalid route:
  ```sh
  curl -v http://localhost:4221/invalid
  ```
- Handle multiple encoding headers including gzip
  ```sh
  curl -v -H "Accept-Encoding: gzip, encoding-2, encoding-3" http://localhost:4221/echo/abc
  ```
  - If using `Accept-Encoding: gzip` on the /echo route, will respond back with 3 letter string compressed via gzip.
- Handle persistent connections
  ```sh
  curl --http1.1 -v http://localhost:4221/echo/banana --next http://localhost:4221/user-agent -H "User-Agent: blueberry/apple-blueberry"
  ```
  Will automatically use persistent connections if using http1.1 method.\
  Can disable with `Connection: close` header.\
  Can test with [oha](https://github.com/hatoo/oha) below.
    ```sh
    oha -c 10 -n 100 http://localhost:4221/echo/banana
    ```
  This will limit 10 connections for 100 total requests, will see each connection handles around 10 requests. \
  Use `--disable-keepalive` flag to disable persistence, will have 100 total connections, with 1 request each. 
  

## Roadmap & Future Enhancements
- Add support for additional HTTP methods (PUT, DELETE).
- Implement persistent logging.
- Improve error handling and request validation.
- Add support for the Accept-Encoding and Content-Encoding headers. ✔️
- Add support for Accept-Encoding headers that contain multiple compression schemes. ✔️
- Add support for gzip compression. ✔️
- Add persistent connections. ✔️
- Add multiple persistent connections at once. ✔️
