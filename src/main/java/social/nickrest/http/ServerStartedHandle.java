package social.nickrest.http;

import com.sun.net.httpserver.HttpServer;

public interface ServerStartedHandle {
    void handle(HttpServer exchange);
}
