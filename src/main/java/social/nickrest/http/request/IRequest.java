package social.nickrest.http.request;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.NonNull;
import social.nickrest.http.method.HTTPMethod;

public interface IRequest {
    void handle(@NonNull IResponse res);
    HTTPMethod method();
    String path();
    Headers getHeaders();

    void exchange(HttpExchange exchange);
    HttpExchange exchange();
}
