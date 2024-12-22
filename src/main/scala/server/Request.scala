package server
import scala.util.Try

sealed trait Method

object Method{
    case object GET extends Method
    case object POST extends Method
    case object PUT extends Method
    case object DELETE extends Method
    case object PATCH extends Method
    case object TRACE extends Method
    case object Connect extends Method

    def fromString(method:String) : Option[Method] = method match{
        case "GET" => Some(GET)
        case "POST" => Some(POST)
        case "PUT" => Some(PUT)
        case "DELETE" => Some(DELETE)
        case "PATCH" => Some(PATCH)
        case "TRACE" => Some(TRACE)
        case "Connect" => Some(Connect)
        case _ => None
    }
}


case class Request(
                  method : Method,
                  path : String,
                  headers : Map[String,String],
                  body : Option[String]
                  )

object Request{
    def parse(rawRequest : String) : Either[String,Request]  = {
        val lines = rawRequest.split("\r\n").toList
        if (lines.isEmpty) return  Left("Empty Request")

        val requestLineParts = lines.head.split(" ")
        if (requestLineParts.length != 3) return Left("Invalid Request line")

        val methodString = requestLineParts(0)
        val path = requestLineParts(1)
        val headers = parseHeaders(lines.tail.takeWhile(_.nonEmpty))
        val body = parseBody(lines.dropWhile(_.nonEmpty).drop(1),headers)

//        println("*****Request details*****")
//        println(methodString)
//        println(path)
//        println(headers)
//        println(body)

        Method.fromString(methodString) match{
            case Some(method) => Right(Request(method,path, headers = headers, body = body))
            case None => Left(s"Invalid HTTP method:$methodString")
        }
    }

    private def parseHeaders(headerLines: List[String]) : Map[String,String] = {
        headerLines.flatMap { line =>
            line.split(":", 2) match
                case Array(key,value) => Some(key.trim() -> value.trim())
                case _ => None
        }
    }.toMap

    private def parseBody(bodyLines: List[String], headers: Map[String, String]) : Option[String] = {
        headers.get("Content-Length") match {
            case Some(length) if Try(length.toInt).isSuccess =>
                Some(bodyLines.mkString("\n"))
            case _ => None
        }
    }
}