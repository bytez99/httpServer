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

## Installation & Running

1. **Clone the repository:**
   ```sh
   git clone https://github.com/yourusername/httpServer.git
   cd httpServer
   ```
2. **Compile the Java files:**
   ```sh
   javac HttpServer.java
   ```
3. **Run the server:**
   ```sh
   java HttpServer
   ```

## Example Usage

- Fetch the User-Agent:
  ```sh
  curl -X GET http://localhost:8080/user-agent
  ```
- Retrieve a file:
  ```sh
  curl -X GET http://localhost:8080/files/sample.txt
  ```
- Create a file with a POST request:
  ```sh
  curl -X POST http://localhost:8080/files/sample.txt -d "Hello, World!"
  ```
- Echo a string (min. 3 characters):
  ```sh
  curl -X GET http://localhost:8080/echo/hello
  ```
- Handle an invalid route:
  ```sh
  curl -X GET http://localhost:8080/invalid
  ```

## Roadmap & Future Enhancements
- Add support for additional HTTP methods (PUT, DELETE)
- Implement persistent logging
- Improve error handling and request validation
- Implement a simple caching mechanism

## License
This project is open-source and available under the [MIT License](LICENSE).

