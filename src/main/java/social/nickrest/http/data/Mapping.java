package social.nickrest.http.data;

import social.nickrest.http.method.MethodEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Mapping {
    MethodEnum method();
    String path();
    String contentType() default "text/plain"; // only use this if the method returns something instead of void
}
