package server


case class Response(
                   statusCode : Int,
                   headers : Map[String,String],
                   body : String
                   ):
    def toRaw: String = {
        val statusLine = s"HTTP/1.1 $statusCode ${Response.getReasonPhrase(statusCode)}"
        val headerLines = headers.map { case (key, value) => s"$key: $value" }.mkString("\r\n")
        s"$statusLine\r\n$headerLines\r\n\r\n$body"
    }

private object Response{
    private val reasonPhrase = Map(
        200 -> "OK",
        201 -> "Created",
        400 -> "Bad Request",
        404 -> "Not Found",
        500 -> "Internal Server Error"
    )

    private def getReasonPhrase(statusCode : Int) : String = {
        reasonPhrase.getOrElse(statusCode,"Unknown")
    }
}