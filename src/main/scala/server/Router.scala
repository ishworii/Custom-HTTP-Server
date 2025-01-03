package server

import java.nio.file.{Files,Paths}
import scala.util.Try
import io.circe.syntax._
import io.circe.generic.auto._

object Router{
    private val staticDir = "./public"

    private var jsonData: Map[String, List[Map[String,String|Int]] | String] = Map(
        "message" -> "Hello,JSON!",
        "status" -> "success",
        "data" -> List(Map[String, String|Int](
            "id" -> 1,
            "name" -> "John",
            "age" -> 30
        ))
    )

    private type Route = PartialFunction[Request,Response]

    private val routes : Route = {
        case Request(Method.GET,path,_,_) if isStaticFile(path) =>
            serveStaticFile(path)
        case Request(Method.GET,"/api/data",_,_) =>
            serveJson(jsonData)
        case Request(Method.POST,"/api/data",_,body) => handlePost(body)
        case Request(Method.PUT,path,_,body) if path.startsWith("/api/data")=> 
            val id = path.split("/").last.toInt
            handlePut(id,body)
        case Request(Method.DELETE,path,_,_) if path.startsWith("/api/data") => 
            val id = path.split("/").last.toInt
            handleDelete(id)
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

    private def serveJson(data: Map[String, List[Map[String,String|Int]] | String]): Response = 
        Response(200,Map("Content-Type" -> "application/json"),data.toString())

    private def handlePost(body: Option[String]): Response = {
        val parsedBody = Try(io.circe.parser.parse(body.getOrElse("{}")).flatMap(_.as[Map[String, String]])).toOption
        parsedBody match {
            case Some(data:Map[String,String]) if data.contains("name") && data.contains("age") =>
                val currentData = jsonData("data").asInstanceOf[List[Map[String, String|Int]]]
                val newId = currentData.size + 1
                val newEntry = Map(
                    "id" -> newId,
                    "name" -> data("name"),
                    "age" -> data("age").toInt
                )
                jsonData = jsonData.updated("data", currentData :+ newEntry)
                Response(201, Map("Content-Type" -> "application/json"), "Data created successfully")
            case _ =>
                Response(400, Map("Content-Type" -> "text/plain"), "Invalid JSON or missing fields")
        }
    }

    private def handlePut(id: Int, body: Option[String]): Response = {
        val parsedBody = Try(io.circe.parser.parse(body.getOrElse("{}")).flatMap(_.as[Map[String, String]])).toOption
        parsedBody match {
            case Some(data) =>
                val currentData = jsonData("data").asInstanceOf[List[Map[String, String|Int]]]
                val updatedData = currentData.map { entry =>
                    if (entry("id") == id) {
                        entry ++ data.toSeq.collect {
                            case (key, value) if key == "age" => key -> value
                            case (key, value) => key -> value
                        }
                    } else entry
                }
                jsonData = jsonData.updated("data", updatedData)
                Response(200, Map("Content-Type" -> "application/json"), "Data updated successfully")
            case None =>
                Response(400, Map("Content-Type" -> "text/plain"), "Invalid JSON")
        }
    }

    private def handleDelete(id: Int): Response = {
        val currentData = jsonData("data").asInstanceOf[List[Map[String, String|Int]]]
        val updatedData = currentData.filterNot(entry => entry("id") == id)

        if (currentData.size == updatedData.size) {
            Response(404, Map("Content-Type" -> "text/plain"), "ID not found")
        } else {
            jsonData = jsonData.updated("data", updatedData)
            Response(200, Map("Content-Type" -> "application/json"), "Data deleted successfully")
        }
    }
}
