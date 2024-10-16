package social.nickrest.util;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

@UtilityClass
public class ClassPathUtil {

    public static List<Class<?>> getClassesForPackage(String packageName, boolean recursive) throws ClassNotFoundException {
        ArrayList<File> directories = new ArrayList<>();

        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = cld.getResources(path);
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath(), StandardCharsets.UTF_8)));
            }
        } catch (NullPointerException | IOException e) {
            throw new RuntimeException("Something went wrong while getting all resources for " + packageName, e);
        }

        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : directories) {
            if (directory.exists()) {
                String[] files = directory.list();
                if (files == null) continue;
                for (String file : files) {
                    if (file.endsWith(".class")) {
                        try {
                            classes.add(Class.forName(packageName + '.' + file.substring(0, file.length() - 6)));
                        } catch (NoClassDefFoundError ignored) {}
                    } else if (recursive && new File(directory, file).isDirectory()) {
                        classes.addAll(getClassesForPackage(packageName + "." + file, true));
                    }
                }
            } else {
                throw new ClassNotFoundException(packageName + " (" + directory.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }

}
