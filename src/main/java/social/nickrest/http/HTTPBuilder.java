package social.nickrest.http;

import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import lombok.NonNull;
import social.nickrest.http.context.ContextBuilder;
import social.nickrest.http.data.Context;
import social.nickrest.http.method.HTTPMethod;
import social.nickrest.http.request.IRequest;
import social.nickrest.http.request.IResponse;
import social.nickrest.http.request.type.BasicHTTPRequest;
import social.nickrest.http.request.type.Response;
import social.nickrest.util.ClassPathUtil;
import social.nickrest.util.ResourceFileUtil;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class HTTPBuilder {
    private final List<IRequest> requests = new ArrayList<>();
    private final HashMap<String, String> headersToWrite = new HashMap<>();

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

                if(path.endsWith("/") && !path.equals("/")) {
                    Response response = new Response(HTTPMethod.GET, path, requestBody, exchange);
                    response.redirect(path.substring(0, path.length() - 1));
                    return;
                }

                HTTPMethod requestMethod = HTTPMethod.valueOf(exchange.getRequestMethod());
                requests.stream()
                        .filter(iRequest -> iRequest.path().equalsIgnoreCase(path) && iRequest.method().equals(requestMethod))
                        .findFirst()
                        .ifPresentOrElse((request) -> {
                            request.exchange(exchange);
                            request.handle(new Response(requestMethod, path, requestBody, exchange));
                            }, () -> new Response(requestMethod, path, requestBody, exchange)
                                    .status(HttpStatus.NOT_FOUND)
                                    .writeHeader("Content-Type", "text/html")
                                    .write(String.format("<pre>Cannot %s %s</pre>", requestMethod, path))
                        );
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

    public HTTPBuilder openStatic(String basePath) {
        return openStatic(basePath, "/");
    }

    public HTTPBuilder openStatic(File basePath) {
        return openStatic(basePath, "/");
    }


    public HTTPBuilder openStatic(String basePath, String staticPath) {
        List<String> files = ResourceFileUtil.getResourceFiles(basePath, true);

        if (!staticPath.equals("/")) {
            staticPath = staticPath.replaceAll("^/|/$", "");
        }

        for(String file : files) {
            String httpPath = (staticPath.equals("/") ? "" : staticPath) + ContextBuilder.formatPath("/", staticPath.replaceFirst(basePath, ""), file.replaceFirst(basePath, ""));
            String fileExtension = file.substring(file.lastIndexOf(".") + 1);

            if(!staticPath.equals("/")) {
                httpPath = "/" + httpPath;
            }

            request(new BasicHTTPRequest(httpPath, HTTPMethod.GET) {
                @Override
                public void handle(@NonNull IResponse res) {
                    res.writeHeader("Content-Type", "text/" + fileExtension)
                            .write(ResourceFileUtil.getResourceAsString(file));
                }
            });

            if(httpPath.endsWith("static/index.html")) {
                String path = httpPath.replace("static/index.html", "");
                if(path.endsWith("/") && !path.equalsIgnoreCase("/")) {
                    path = path.substring(0, path.length() - 1);
                }

                request(new BasicHTTPRequest(path, HTTPMethod.GET) {
                    @Override
                    public void handle(@NonNull IResponse res) {
                        res.writeHeader("Content-Type", "text/" + fileExtension)
                                .write(ResourceFileUtil.getResourceAsString(file));
                    }
                });
            }
        }
        return this;
    }

    public HTTPBuilder openStatic(File file, String staticPath) {
        if(!file.isDirectory()) {
            throw new RuntimeException("File is not a directory");
        }

        File[] files = file.listFiles();

        if(files == null) {
            throw new RuntimeException("File is not a directory");
        }

        for(File f : files) {
            if(f.isDirectory()) {
                openStatic(f, ContextBuilder.formatPath("/", staticPath, f.getName()));
                continue;
            }

            String httpPath = ContextBuilder.formatPath("/", staticPath, f.getName());
            String fileExtension = f.getName().substring(f.getName().lastIndexOf(".") + 1);
            String fileName = f.getName();

            if(fileName.equalsIgnoreCase("index.html")) {
                String path = httpPath.replace("index.html", "");

                if(path.endsWith("/") && !path.equalsIgnoreCase("/")) path = path.substring(0, path.length() - 1);
                openFile(path, f, fileExtension);
            }

            openFile(httpPath, f, fileExtension);
        }

        return this;
    }

    private void openFile(String path, File f, String fileExtension) {
        request(new BasicHTTPRequest(path, HTTPMethod.GET) {
            @Override
            public void handle(@NonNull IResponse res) {
                try {
                    FileReader reader = new FileReader(f);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    StringBuilder content = new StringBuilder();
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        content.append(line);
                    }

                    res.writeHeader("Content-Type", "text/" + fileExtension)
                            .write(content.toString());

                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public HTTPBuilder writeHeader(String key, String value) {
        headersToWrite.put(key, value);
        return this;
    }

    public HTTPBuilder request(IRequest request) {
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
                        if(IRequest.class.isAssignableFrom(clazz)) {
                            try {
                                builder.set(request((IRequest) clazz.getConstructor().newInstance()));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        System.out.println(clazz + "");

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

}
