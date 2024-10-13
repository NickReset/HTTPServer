package example.path;

import social.nickrest.http.HTTPBuilder;

public class Main {

    public static void main(String[] args) {
        HTTPBuilder.create()
                .advancedRequest("example.path.http", "/", true)
                .listen(8080, (s) -> System.out.println("Server started at http://localhost:" + s.getAddress().getPort()));
    }
}
