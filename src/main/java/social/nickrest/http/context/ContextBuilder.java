package social.nickrest.http.context;

import lombok.NonNull;
import social.nickrest.http.data.Context;
import social.nickrest.http.data.Mapping;
import social.nickrest.http.request.IRequest;
import social.nickrest.http.request.IResponse;
import social.nickrest.http.request.type.AdvancedHTTPRequest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ContextBuilder {

    public static List<IRequest> build(String basePath, Object object) {
        Class<?> clazz = object.getClass();
        List<IRequest> requests = new ArrayList<>();

        if(!clazz.isAnnotationPresent(Context.class)) {
            throw new IllegalArgumentException(String.format("Class \"%s\" must be annotated with @%s", clazz.getName(), Context.class.getName()));
        }

        for(Method method : clazz.getDeclaredMethods()) {
            if(method.isAnnotationPresent(Mapping.class)) {
                Mapping mapping = method.getAnnotation(Mapping.class);

                String path = formatPath(basePath, clazz.getAnnotation(Context.class).value(), mapping.path());
                IRequest request = new AdvancedHTTPRequest(path, mapping.method().getMethod()) {
                    @Override
                    public void handle(@NonNull IResponse res) {
                        int argsLength = method.getParameterCount();
                        Object[] args = new Object[argsLength];

                        if(argsLength > 2) {
                            throw new IllegalArgumentException("Method must have 0-2 parameters");
                        }

                        for(int i = 0; i < argsLength; i++) {
                            Class<?> parameterType = method.getParameterTypes()[i];
                            if(parameterType == IResponse.class) {
                                args[i] = res;
                            } else if(parameterType == IRequest.class) {
                                args[i] = this;
                            } else {
                                throw new IllegalArgumentException("Method parameters must be of type IRequest or IResponse");
                            }
                        }

                        if(method.getReturnType() != void.class) {
                            try {
                                Object result = method.invoke(object, args);
                                res.writeHeader("Content-Type", mapping.contentType())
                                        .write(result.toString());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        try {
                            method.invoke(object, args);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                requests.add(request);
            }
        }

        return requests;
    }

    public static String formatPath(String basePath, String path, String finalPath) {
        StringBuilder result = new StringBuilder();

        // Handle basePath
        if (basePath.equals("/")) {
            result.append("/");
        } else {
            result.append(basePath.endsWith("/") ? basePath : basePath + "/");
        }

        // Handle path
        if (!path.equals("/")) {
            String trimmedPath = path.startsWith("/") ? path.substring(1) : path;
            trimmedPath = trimmedPath.endsWith("/") ? trimmedPath.substring(0, trimmedPath.length() - 1) : trimmedPath;
            if (!trimmedPath.isEmpty()) {
                result.append(trimmedPath).append("/");
            }
        }

        // Handle finalPath
        if (!finalPath.isEmpty()) {
            if (finalPath.startsWith("/")) {
                result.append(finalPath.substring(1));
            } else {
                result.append(finalPath);
            }
        } else if (result.length() > 1 && result.charAt(result.length() - 1) == '/') {
            // Remove trailing slash if finalPath is empty and result is not just "/"
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }
}
