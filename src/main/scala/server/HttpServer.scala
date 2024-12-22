package server

import java.net.{ServerSocket, Socket}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import scala.util.{Try, Success, Failure}

object HttpServer {
    def start(port: Int): Unit = {
        val serverSocket = new ServerSocket(port)
        println(s"Server running on port $port")

        while (true) {
            val clientSocket = serverSocket.accept()
            handleConnection(clientSocket)
        }
    }

    private def handleConnection(clientSocket: Socket): Unit = {
        val result = for {
            rawRequest <- readFromSocket(clientSocket)
            parsedRequest <- parseRequest(rawRequest)
            response = handleRequest(parsedRequest)
            _ <- writeToSocket(clientSocket, response)
        } yield ()

        result match {
            case Success(_) => println("Handled connection successfully.")
            case Failure(e) => println(s"Failed to handle connection: ${e.getMessage}")
        }

        clientSocket.close()
    }

    private def readFromSocket(socket: Socket): Try[String] = Try {
        val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

        val headersBuilder = new StringBuilder
        var line: String = in.readLine()
        while (line != null && line.nonEmpty) {
            headersBuilder.append(line).append("\r\n")
            line = in.readLine()
        }
        headersBuilder.append("\r\n")

        val headers = headersBuilder.toString()
        val contentLength = extractContentLength(headers)

        val body = if (contentLength > 0) {
            val bodyChars = new Array[Char](contentLength)
            in.read(bodyChars, 0, contentLength)
            new String(bodyChars)
        } else {
            ""
        }

        headers + body
    }

    private def extractContentLength(headers: String): Int = {
        val ContentLengthPattern = "Content-Length: (\\d+)".r
        ContentLengthPattern.findFirstMatchIn(headers).map(_.group(1).toInt).getOrElse(0)
    }

    private def parseRequest(rawRequest: String): Try[Request] = Try {
        Request.parse(rawRequest) match {
            case Right(request) => request
            case Left(error)    => throw new IllegalArgumentException(error)
        }
    }

    private def writeToSocket(socket: Socket, response: Response): Try[Unit] = Try {
        val out = new PrintWriter(socket.getOutputStream, true)
        val rawResponse = response.toRaw
        println(s"Sending Response:\n$rawResponse")
        out.println(rawResponse)
        out.flush()
    }

    private def handleRequest(request: Request): Response = {
        request.method match{
            case Method.GET => Router.handle(request)
            case Method.POST => handlePost(request)
            case _ =>
                Response(
                    statusCode = 400,
                    headers = Map("Content-Type" -> "text/plain"),
                    body = "Unsupported HTTP request"
            )
        }
    }

    private def handlePost(request: Request): Response = {
        val receivedBody = request.body.getOrElse("No body provided")
        val content = s"Handling POST request for path: ${request.path}, with body: $receivedBody"
        Response(
            statusCode = 201,
            headers = Map("Content-Type" -> "text/plain"),
            body = content
        )
    }
}
