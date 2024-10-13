# Getting started
This is a simple example of how to create a basic server using the 'HTTPBuilder`

## 

```java
import lombok.NonNull;
import social.nickrest.http.HTTPBuilder;
import social.nickrest.http.method.HTTPMethod;
import social.nickrest.http.request.IResponse;
import social.nickrest.http.request.type.BasicHTTPRequest;

public class Main {
    public static void main(String[] args) {
        // first create the server
        HTTPBuilder builder = HTTPBuilder.create();

        // create a GET request to the root path that says "Hello World"
        builder.request(new BasicHTTPRequest("/", HTTPMethod.GET) {
            @Override
            public void handle(@NonNull IResponse res) {
                res.writeHeader("Content-Type", "text/html"); // writing the content type (defualt being text/plain)
                res.write("<h1>Hello World</h1>"); // writing the content
            }
        });

        builder.listen(8080, (server) -> System.out.println("Server started on port " + server.getPort()));
    }
}
```

### This is just like the most basic way you can do this but you can make it more advanced by doing this all in one line
```java
    HTTPBuilder.create() // first create the server
        .request(new BasicHTTPRequest("/", HTTPMethod.GET) { // create a GET request to the root path that says "Hello World"
            @Override
            public void handle(@NonNull IResponse res) {
                res.writeHeader("Content-Type", "text/html") // writing the content type (default being text/plain)
                    .write("<h1>Hello World</h1>"); // writing the content
            }
        })
        .listen(8080, (server) -> System.out.println("Server started on port " + server.getPort()));
}
```

### but once again this is just not in depth enough there is a way where you can just use a whole another class a context without extending anything ***(just like springboot)***
see more in the [Documentation](./docs)
