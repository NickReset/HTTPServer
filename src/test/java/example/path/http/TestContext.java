package example.path.http;

import lombok.NonNull;
import social.nickrest.http.data.Context;
import social.nickrest.http.data.Mapping;
import social.nickrest.http.method.MethodEnum;
import social.nickrest.http.request.IResponse;

@Context
public class TestContext {

    @Mapping(
            path = "/hello/{name}",
            method = MethodEnum.GET,
            contentType = "application/json"
    )
    public String test(@NonNull IResponse response) {
        return "Hello, " + response.pathParams().get("name") + " " + response.pathParams().size();
    }
}
