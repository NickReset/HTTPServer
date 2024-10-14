package social.nickrest.http.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.NonNull;
import social.nickrest.http.method.HTTPMethod;

public interface IResponse {
    void handle(@NonNull IRequest res);
    HTTPMethod method();
    String path();
    Headers getHeaders();

    void exchange(HttpExchange exchange);
    HttpExchange exchange();
}

