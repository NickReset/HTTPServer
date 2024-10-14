package social.nickrest.http.request.type;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.RequiredArgsConstructor;
import social.nickrest.http.HttpStatus;
import social.nickrest.http.method.HTTPMethod;
import social.nickrest.http.request.IRequest;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Request implements IRequest {

    private final Map<String, String> pathParams = new HashMap<>();
    private final HTTPMethod httpType;
    private final String path;
    private final String body;
    private final HttpExchange exchange;
    private int status = 200;

    @Override
    public void write(byte[] response) {
        try {
            exchange.sendResponseHeaders(status, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBody() {
        return body.trim();
    }

    @Override
    public IRequest writeHeader(String key, String value) {
        exchange.getResponseHeaders().set(key, value);
        return this;
    }

    @Override
    public Headers getHeaders() {
        return exchange.getRequestHeaders();
    }

    @Override
    public IRequest status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.valueOf(status);
    }

    @Override
    public Map<String, String> pathParams() {
        return pathParams;
    }

    @Override
    public Map<String, String> query() {
        return exchange.getRequestURI().getQuery() == null ? new HashMap<>() : parseQuery(exchange.getRequestURI().getQuery());
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            String allAfter = param.substring(param.indexOf("=") + 1);
            map.put(entry[0], entry.length > 1 ? allAfter : "");
        }
        return map;
    }

    @Override
    public IRequest pathParams(Map<String, String> pathParams) {
        this.pathParams.putAll(pathParams);
        return this;
    }

    @Override
    public HTTPMethod method() {
        return httpType;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public HttpExchange exchange() {
        return exchange;
    }

}