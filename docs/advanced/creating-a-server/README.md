## First Create the server
```java
HTTPBuilder.create() // just like we have been doing create builder
    .advancedRequest(
     "social.nickrest.example", // first parameter is a path in your classpath which leads to a directory of classes that handle requests
     "/", // second parameter is where these request will listen to
     false, // thrid parameter is if you want when it listens to classes to listen to sub paths (recursively)
    )
    .listen(8080, (server) -> System.out.println("Server started on port " + server.getPort())); /// then ovbiously start the server
```

## Now we will create the [Context](../context) class 
### *(you can have multiple of these just make sure they are in the same directory as the path you provided in the `advancedRequest` method)*

```java
package social.nickrest.example;

import social.nickrest.http.data.Context;
import social.nickrest.http.data.Mapping;

/**
 * please do not forget to add the `@Context` annotation to the class it tells the builder to look at this class
 * but if you want it to go off a subdirectory of the path you started listening to you can use the `@Context` annotation 
 * with a parameter of the path
 */
@Context("/api") // <--- you do not have to include the sub directory in the path the default is the root
public class RequestHandler {

    /**
     * Okay so the first example I will show you is just a Hello World application
     */
    @Mapping(
            path = "/",  // <-- this will listen to /api so the full path will be /api sense I just listened to the root directory in #advancedRequest
            contentType = "text/html" // <-- you do not have to include this the default is `text/plain` but I am trying to return html
    )
    public String helloWorld() {
        return "<h1>Hello World</h1>";
    }
}
```
### for more information on the `@Context` class please visit the [Context](../context) page for a more in-depth look at the class