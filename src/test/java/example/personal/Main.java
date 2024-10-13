package example.personal;

import social.nickrest.http.HTTPBuilder;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        HTTPBuilder.create()
                .openStatic(new File("example/personal"))
                .listen(8080, (s) -> System.out.println("Server started at http://localhost:" + s.getAddress().getPort()));
    }

}