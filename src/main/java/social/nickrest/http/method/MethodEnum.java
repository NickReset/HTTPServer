package social.nickrest.http.method;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MethodEnum {
    GET(HTTPMethod.GET),
    HEAD(HTTPMethod.HEAD),
    POST(HTTPMethod.POST),
    PUT(HTTPMethod.PUT),
    PATCH(HTTPMethod.PATCH),
    DELETE(HTTPMethod.DELETE),
    OPTIONS(HTTPMethod.OPTIONS),
    TRACE(HTTPMethod.TRACE);

    private final HTTPMethod method;
}
