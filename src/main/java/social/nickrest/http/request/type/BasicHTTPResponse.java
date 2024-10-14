package social.nickrest.http.request.type;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.RequiredArgsConstructor;
import social.nickrest.http.method.HTTPMethod;
import social.nickrest.http.request.IRequest;
import social.nickrest.http.request.IResponse;

@RequiredArgsConstructor
public abstract class BasicHTTPResponse implements IResponse {

    private HttpExchange exchange;

    private final String path;
    private final HTTPMethod method;

    @Override
    public String path() {
        return path;
    }

    @Override
    public HTTPMethod method() {
        return method;
    }

    @Override
    public HttpExchange exchange() {
        return exchange;
    }

    @Override
    public void exchange(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public Headers getHeaders() {
        return exchange.getResponseHeaders();
    }

}
