# Custom Http Server

This is custom HTTP server written in Scala from scratch
built with the intent of learning more about http protocol.
right now it can handle a simple requests like:
GET /api/data -> returns a json
GET /api/{filename} -> serves the file if it exists on the ./public directory
POST / -> acknowledges POST request with toy response
## Completed tasks
1. First i just want to make a tcp connection and echo the request back.
2. Parse the request
3. Handled basic GET and POST
4. serve static files
5. added more methods[GET,POST,PUT,DELETE]

## Things to add
1. Better routing and parsing
2. Dynamic route handling
3. Middlewares??
4. Authentication
