package social.nickrest.http.method;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@RequiredArgsConstructor
public class HTTPMethod {

    public static final HTTPMethod GET = new HTTPMethod("GET");
    public static final HTTPMethod HEAD = new HTTPMethod("HEAD");
    public static final HTTPMethod POST = new HTTPMethod("POST");
    public static final HTTPMethod PUT = new HTTPMethod("PUT");
    public static final HTTPMethod PATCH = new HTTPMethod("PATCH");
    public static final HTTPMethod DELETE = new HTTPMethod("DELETE");
    public static final HTTPMethod OPTIONS = new HTTPMethod("OPTIONS");
    public static final HTTPMethod TRACE = new HTTPMethod("TRACE");

    private final String method;


    public static HTTPMethod valueOf(String method) {
        try {
            Field field = HTTPMethod.class.getDeclaredField(method.toUpperCase());
            field.setAccessible(true);

            boolean staticField = Modifier.isStatic(field.getModifiers());

            if (!staticField) {
                return new HTTPMethod(method.toUpperCase());
            }

            return (HTTPMethod) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return new HTTPMethod(method.toUpperCase());
        }
    }

    @Override
    public String toString() {
        return method;
    }


}
