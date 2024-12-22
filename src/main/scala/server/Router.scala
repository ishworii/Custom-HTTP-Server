package server

import java.nio.file.{Files,Paths}
import scala.util.Try
import io.circe.syntax._
import io.circe.generic.auto._

object Router{
    private val staticDir = "./public"

    private type Route = PartialFunction[Request,Response]

    private val routes : Route = {
        case Request(Method.GET,path,_,_) if isStaticFile(path) =>
            serveStaticFile(path)
        case Request(Method.GET,"/api/data",_,_) =>
            serveJson(Map("message" -> "Hello,JSON!","status" -> "success"))
        case _ => Response(404,Map("Content-Type" -> "text/plain"),"Route not found")
    }

    def handle(request : Request) : Response =
        routes.applyOrElse(request,(_:Request) =>
            Response(404,Map("Content-Type" -> "text/plain"),"Route not found"))

    private def isStaticFile(path:String) : Boolean = {
        val extensions = Set(".html",".css",".js",".png",".jpg",".jpeg")
        extensions.exists(path.endsWith)
    }

    private def serveStaticFile(path:String) : Response = {
        val filePath = Paths.get(s"$staticDir$path")
        println(filePath)
        if(Files.exists(filePath)) {
            val content = String(Files.readAllBytes(filePath))
            val contentType = detectContentType(path)
            Response(200,Map("Content-Type" -> contentType),content)
        }
        else
            Response(404,Map("Content-Type" -> "text/plain"),"File not found")
    }

    private def detectContentType(path:String) : String = {
        path.split("\\.").lastOption match{
            case Some("html") => "text/html"
            case Some("css") => "text/css"
            case Some("js") => "application/javascript"
            case Some("png") => "image/png"
            case Some("jpg") | Some("jpeg") => "image/jpg"
            case _ => "text/plain"
        }
    }

    private def serveJson(data : Map[String,String]) : Response ={
        val jsonResponse = data.asJson.noSpaces
        Response(200,Map("Content-Type" -> "application/json"),jsonResponse)
    }
}
