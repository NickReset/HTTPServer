package social.nickrest.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpHandlers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import social.nickrest.http.context.ContextBuilder;
import social.nickrest.http.data.Context;
import social.nickrest.http.method.HTTPMethod;
import social.nickrest.http.request.IRequest;
import social.nickrest.http.request.IResponse;
import social.nickrest.http.request.type.Request;
import social.nickrest.util.ClassPathUtil;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class HTTPBuilder {
    private final List<IResponse> requests = new ArrayList<>();
    private final HashMap<String, String> headersToWrite = new HashMap<>();

    private final List<ServerStartedHandle> queue = new ArrayList<>();
    private final Map<StaticFileServer, HttpHandler> staticFiles = new HashMap<>();
    private ClientConnectedHandle handle;

    public static HTTPBuilder create() {
        return new HTTPBuilder();
    }

    public void listen(int port) {
        listen(port, null);
    }

    public void listen(int port, ServerStartedHandle startedHandle) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            queue.forEach(handle -> handle.handle(server));
            server.createContext("/", (exchange -> {
                InetAddress address = exchange.getRemoteAddress().getAddress();

                if(handle != null) {
                    handle.handle(address);
                }

                InputStream is = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }

                headersToWrite.keySet().forEach(key -> exchange.getResponseHeaders().set(key, headersToWrite.get(key)));

                String requestBody = body.toString();
                String path = exchange.getRequestURI().getPath();
                HTTPMethod requestMethod = HTTPMethod.valueOf(exchange.getRequestMethod());

                if (requestMethod == HTTPMethod.GET) {
                    StaticFileServer staticFileServer = staticFiles.keySet().stream()
                            .filter(staticFile -> path.startsWith(staticFile.staticPath))
                            .findFirst()
                            .orElse(null);

                    if(staticFileServer != null) {
                        File requestedFile = getFile(staticFileServer, path);

                        if(requestedFile.exists()) {
                            staticFiles.get(staticFileServer).handle(exchange);
                            return;
                        }
                    }
                }

                if (path.endsWith("/") && !path.equals("/")) {
                    Request response = new Request(requestMethod, path, requestBody, exchange);
                    response.redirect(path.substring(0, path.length() - 1));
                    return;
                }

                requests.stream()
                        .filter(request -> {
                            // Convert path pattern to regex
                            String regex = request.path().replaceAll("\\{[^}]+}", "([^/]+)");
                            regex = "^" + regex + "$"; // Anchors to match the entire path

                            // Compile regex and match against the incoming path
                            Pattern compiledPattern = Pattern.compile(regex);
                            Matcher matcher = compiledPattern.matcher(exchange.getRequestURI().getPath()); // Use the request URI

                            return (matcher.matches() || request.path().equals(path)) && requestMethod.equals(request.method()); // Check if the entire string matches
                        })
                        .findFirst()
                        .ifPresentOrElse((request) -> {
                            // Extract path parameters
                            Map<String, String> pathParams = extractPathParams(request.path(), exchange.getRequestURI().getPath());

                            // Handle the request
                            request.exchange(exchange);
                            request.handle(new Request(requestMethod, path, requestBody, exchange)
                                    .pathParams(pathParams));
                        }, () -> {
                            // Handle not found case
                            new Request(requestMethod, path, requestBody, exchange)
                                    .status(HttpStatus.NOT_FOUND)
                                    .writeHeader("Content-Type", "text/html")
                                    .write(String.format("<pre>Cannot %s %s</pre>", requestMethod, path));
                        });
            }));

            duplicateCheck(); // this will prevent the server from starting if there are duplicate requests

            server.setExecutor(null);
            server.start();
            if(startedHandle != null) {
                startedHandle.handle(server);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static File getFile(StaticFileServer staticFileServer, String path) {
        File file = staticFileServer.listeningFolder;
        String filePath = path.substring(staticFileServer.staticPath.length());
        String lastPath = filePath.substring(filePath.lastIndexOf("/") + 1);
        String fileExtension = lastPath.contains(".") ? lastPath.substring(lastPath.lastIndexOf(".") + 1) : null;

        if(filePath.endsWith("/")) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }

        if(fileExtension == null) {
            filePath += "/index.html";
        }

        return new File(file, filePath);
    }

    private Map<String, String> extractPathParams(String pattern, String requestPath) {
        Map<String, String> params = new HashMap<>();

        // Convert pattern to regex
        String regex = pattern.replaceAll("\\{([^}]+)}", "([^/]+)"); // Convert {param} to ([^/]+)
        regex = "^" + regex + "$"; // Anchors to match the entire path

        Pattern compiledPattern = Pattern.compile(regex);
        Matcher matcher = compiledPattern.matcher(requestPath);

        // Check if the path matches
        if (matcher.matches()) {
            // Extract parameters
            Matcher paramMatcher = Pattern.compile("\\{([^}]+)}").matcher(pattern);
            int i = 1; // Start from 1 for capturing groups
            while (paramMatcher.find()) {
                String paramName = paramMatcher.group(1);
                String paramValue = matcher.group(i++);
                params.put(paramName, paramValue);
            }
            return params; // Return the populated map
        }

        return null; // Return null if no match found
    }

    public HTTPBuilder openStatic(File basePath) {
        return openStatic(basePath, "/");
    }

    public HTTPBuilder openStatic(File file, String staticPath) {
        staticFiles.put(new StaticFileServer(staticPath, file), SimpleFileServer.createFileHandler(Path.of(file.getAbsolutePath())));
        return this;
    }

    public HTTPBuilder writeHeader(String key, String value) {
        headersToWrite.put(key, value);
        return this;
    }

    public HTTPBuilder request(IResponse request) {
        requests.add(request);
        return this;
    }

    public HTTPBuilder advancedRequest(String baseClassLocation, String path, boolean recursive) {
        AtomicReference<HTTPBuilder> builder = new AtomicReference<>(this);

        try {
            List<Class<?>> classes = ClassPathUtil.getClassesForPackage(baseClassLocation, recursive);

            classes.stream()
                    .filter(clazz -> clazz.isAnnotationPresent(Context.class))
                    .forEach(clazz -> {
                        if(IResponse.class.isAssignableFrom(clazz)) {
                            try {
                                builder.set(request((IResponse) clazz.getConstructor().newInstance()));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        try {
                            ContextBuilder.build(path, clazz.getConstructor().newInstance()).forEach(this::request);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return builder.get();
    }

    public HTTPBuilder onClientConnection(ClientConnectedHandle handle) {
        this.handle = handle;
        return this;
    }

    private void duplicateCheck() {
        for (int i = 0; i < requests.size(); i++) {
            for (int j = i + 1; j < requests.size(); j++) {
                if(requests.get(i).path().equals(requests.get(j).path()) && requests.get(i).method().equals(requests.get(j).method())) {
                    throw new RuntimeException("Duplicate request found: " + requests.get(i).path() + " " + requests.get(i).method());
                }
            }
        }
    }

    @RequiredArgsConstructor
    public static class StaticFileServer {
        private final String staticPath;
        private final File listeningFolder;
    }

}
