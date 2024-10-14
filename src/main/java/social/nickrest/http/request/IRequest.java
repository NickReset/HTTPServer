package social.nickrest.http.request;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import social.nickrest.http.HttpStatus;
import social.nickrest.http.method.HTTPMethod;

import java.util.Map;

public interface IRequest {
    IRequest pathParams(Map<String, String> pathParams);
    Map<String, String> pathParams();
    Map<String, String> query();
    String getBody();
    String getPath();

    HttpExchange exchange();
    HTTPMethod method();

    void write(byte[] bytes);
    Headers getHeaders();
    IRequest writeHeader(String key, String value);
    IRequest status(int status);

    default JsonObject getJsonBody() {
        if(getBody().startsWith("{") && getBody().endsWith("}")) {
            return JsonParser.parseString(getBody()).getAsJsonObject();
        }

        return new JsonObject();
    }

    default IRequest status(HttpStatus status) {
        return status(status.getValue());
    }

    default void redirect(String location) {
        status(302)
                .writeHeader("Location", location)
                .write("");
    }

    int status();
    HttpStatus httpStatus();
    default void write(String response) {
        write(response.getBytes());
    }
}
