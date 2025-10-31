package framework.servlet;

import framework.annotation.Controller;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ClassScanner {

    private ClassScanner() {}

    public static List<Class<?>> findControllers(ServletContext ctx, String basePackage) {
        List<Class<?>> controllers = new ArrayList<>();

        String classesRoot = ctx.getRealPath("/WEB-INF/classes");
        if (classesRoot == null) {
            System.err.println("[Scanner] getRealPath('/WEB-INF/classes') est null. Déployez en WAR explodé ou utilisez un scanner de classpath.");
            return controllers;
        }

        String basePath = basePackage.replace('.', File.separatorChar);
        File controllerDir = new File(classesRoot, basePath + File.separator + "controller");
        if (!controllerDir.exists() || !controllerDir.isDirectory()) {
            System.out.println("[Scanner] Dossier contrôleurs introuvable: " + controllerDir.getAbsolutePath());
            return controllers;
        }

        List<File> classFiles = new ArrayList<>();
        collectClassFiles(controllerDir, classFiles);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (File f : classFiles) {
            String fqcn = toFqcn(classesRoot, f);
            try {
                Class<?> clazz = Class.forName(fqcn, false, cl);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    controllers.add(clazz);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("[Scanner] Classe introuvable: " + fqcn);
            } catch (NoClassDefFoundError e) {
                System.err.println("[Scanner] Dépendance manquante pour: " + fqcn + " -> " + e.getMessage());
            }
        }

        return controllers;
    }

    private static void collectClassFiles(File dir, List<File> out) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) collectClassFiles(f, out);
            else if (f.getName().endsWith(".class")) out.add(f);
        }
    }

    private static String toFqcn(String classesRoot, File classFile) {
        String abs = classFile.getAbsolutePath();
        String root = new File(classesRoot).getAbsolutePath();
        String rel = abs.substring(root.length() + 1, abs.length() - ".class".length());
        return rel.replace(File.separatorChar, '.');
    }

    public static String detectClasspath(ServletContext ctx) {
        StringBuilder classpath = new StringBuilder();

        // Add /WEB-INF/classes to the classpath
        String classesPath = ctx.getRealPath("/WEB-INF/classes");
        if (classesPath != null) {
            classpath.append(classesPath).append(File.pathSeparator);
        }

        // Add all JAR files in /WEB-INF/lib to the classpath
        String libPath = ctx.getRealPath("/WEB-INF/lib");
        if (libPath != null) {
            File libDir = new File(libPath);
            File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                for (File jar : jarFiles) {
                    classpath.append(jar.getAbsolutePath()).append(File.pathSeparator);
                }
            }
        }

        // Return the constructed classpath
        return classpath.toString();
    }
}
