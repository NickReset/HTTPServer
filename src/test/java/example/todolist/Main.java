package example.todolist;

import social.nickrest.http.HTTPBuilder;

public class Main {

    public static void main(String[] args) {
        HTTPBuilder.create()
                .openStatic("todolist")
                .advancedRequest("example.todolist.api", "/", true)
                .listen(8080, (s) -> System.out.println("Server started at http://localhost:" + s.getAddress().getPort()));
    }

}