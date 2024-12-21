package server

@main
def main(args: String*): Unit = {
  val port = args.headOption.map(_.toInt).getOrElse(8080)
  println(s"Starting HTTP Server on port $port...")
  HttpServer.start(port)
}
