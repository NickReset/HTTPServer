#### Okay so if you are in this page you were previously in the [Creating a Server](../server/README.md) page. If you weren't, I recommend you to go back to that page and follow the instructions there. so that's where we are picking up at

## Step 1: Creating a Context
```java
package social.nickrest.example;

import social.nickrest.http.data.Context;

@Context
public class RequestHandler {
    /** include code below in #Step-2: Mapping */
}
```
#### awesome we created a context class that listens to the root directory of what ever we provided in the `advancedRequest` method

## Step 2: Mapping
* ## Step 2.1 Create a method annotated with `@Mapping`
    #### something you can do with this is return a value and it will be sent to the client any object
    ```java
    import social.nickrest.http.data.Mapping;import social.nickrest.http.method.MethodEnum;
    
    @Mapping(
        path = "/hello", // the path we are listening to
        method = MethodEnum.GET, // the method we are listening to
        contentType = "text/html" // this is optional I just want to send html the default is "text/plain"
    )
    public String helloWorld() {
        return "<h1>Hello World</h1>";
    }
    ```

* ## Step 2.2 Getting more advanced
    #### so if you still want to use the `IRequest` *interface* or the `IResponse` *interface* just like we did in the Basic example, you can do that too
    ```java
    import lombok.NonNull;
    import social.nickrest.http.data.Mapping;
    import social.nickrest.http.request.IRequest;
    import social.nickrest.http.request.IResponse;
        
    @Mapping(
            path = "/hi",
            method = MethodEnum.GET,
            contentType = "text/html"
    ) 
    // okay so in my opinion this is the best feature of this library
    
    public String helloWorld(@NonNull IRequest request) {
        return "<h1>Hello World</h1>";
    }

    ```
* ## Step 2.3 you can have multiple methods
    #### okay so personally I think this is the best feature of this library you can have multiple methods in the same class and put `IRequest` and `IResponse` in any order you want and it will still work
    #### *or* you dont to have `IRequest` or `IResponse` at all or you can have one but not the other the library will fix it for you
    
    * ## you can have it like this
    ```java
        @Mapping(path = "/hi", method = MethodEnum.GET, contentType = "text/html")
        public String hiWorld(@NonNull IRequest request) {
            return "<h1>Hi World</h1>";
        }
    ```

    * ## this
    ```java
        @Mapping(path = "/hi", method = MethodEnum.GET, contentType = "text/html")
        public String hiWorld(@NonNull IResponse request) {
            return "<h1>Hi World</h1>";
        }
    ```
    
    * ## this
    ```java
        @Mapping(path = "/hi" ,method = MethodEnum.GET, contentType = "text/html")
        public String hiWorld(@NonNull IRequest request, @NonNull IResponse response) {
            return "<h1>Hello World</h1>";
        }
    ```
    
    * ## or like this
    ```java
    @Mapping(path = "/hi", method = MethodEnum.GET, contentType = "text/html")
    public String hiWorld(@NonNull IResponse request, @NonNull IRequest response) {
        return "<h1>Hello World</h1>";
    }
    ```

    * ## and finally like this
    ```java
    @Mapping(path = "/hi", method = MethodEnum.GET, contentType = "text/html")
    public String hiWorld() {
        return "<h1>Hello World</h1>";
    }
    ```