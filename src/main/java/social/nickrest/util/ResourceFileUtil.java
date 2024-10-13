package social.nickrest.util;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.URL;
import java.util.*;

@UtilityClass
public class ResourceFileUtil {

    public List<String> getResourceFiles(String path, boolean recursive) {
        List<String> filenames = new ArrayList<>();
        try {

            URL url = Thread.currentThread().getContextClassLoader().getResource(path);
            if (url == null) {
                return filenames;
            }

            if (url.getProtocol().equals("file")) {
                File file = new File(url.getFile());
                if (file.isDirectory()) {
                    filenames.addAll(getResourcesFromDirectory(file, path, recursive));
                } else {
                    filenames.add(path); // Return the full relative path
                }
            } else if (url.getProtocol().equals("jar")) {
                filenames.addAll(getResourcesFromJar(path, recursive));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return filenames;
    }

    private List<String> getResourcesFromDirectory(File directory, String basePath, boolean recursive) {
        List<String> filenames = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                String relativePath = basePath + "/" + file.getName();
                if (file.isDirectory() && recursive) {
                    filenames.addAll(getResourcesFromDirectory(file, relativePath, true));
                } else if (file.isFile()) {
                    filenames.add(relativePath); // Add the full relative path
                }
            }
        }

        return filenames;
    }

    private List<String> getResourcesFromJar(String path, boolean recursive)  {
        List<String> filenames = new ArrayList<>();

        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String resource;
            while ((resource = br.readLine()) != null) {
                String fullPath = path + "/" + resource;
                if (resource.endsWith("/") && recursive) {
                    filenames.addAll(getResourcesFromJar(fullPath, true));
                } else {
                    filenames.add(fullPath); // Keep full path
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filenames;
    }

    public static String getResourceAsString(String file) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
            assert in != null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                return sb.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
