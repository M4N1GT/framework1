package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.lang.reflect.Method;
import framework.annotation.Methode;
import java.util.Map;
import java.util.HashMap;

public class FrontServlet extends HttpServlet {

    private Map<String, MethodBinding> routes;

    private static class MethodBinding {
        final Class<?> controllerClass;
        final Method method;
        MethodBinding(Class<?> controllerClass, Method method) {
            this.controllerClass = controllerClass;
            this.method = method;
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext ctx = getServletContext();
        String basePackage = ctx.getInitParameter("app.basePackage");
        if (basePackage == null || basePackage.isBlank()) {
            throw new ServletException("Paramètre de contexte 'app.basePackage' manquant (ex: test.app)");
        }

        List<Class<?>> controllers = ClassScanner.findControllers(ctx, basePackage);
        ctx.setAttribute("framework.controllers", controllers);

        // Build route map from @Methode annotations
        routes = new HashMap<>();
        for (Class<?> c : controllers) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Methode.class)) {
                    Methode ann = m.getAnnotation(Methode.class);
                    String url = ann.url();
                    String normalized = url.startsWith("/") ? url : ("/" + url);
                    routes.put(normalized, new MethodBinding(c, m));
                }
            }
        }

        System.out.println("[FrontServlet] Contrôleurs détectés (" + controllers.size() + "):");
        for (Class<?> c : controllers) {
            System.out.println(" - " + c.getName());
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
       
        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestUri.substring(contextPath.length());
        System.out.println("Vous essayez d'acceder a : " + requestUri + " -> path: " + path);

        // Affiche la liste des contrôleurs à la racine de l'application (ex: http://host:port/testFramework/)
        if ("/".equals(path) || path.isEmpty()) {
            resp.setContentType("text/html;charset=UTF-8");
            @SuppressWarnings("unchecked")
            List<Class<?>> controllers = (List<Class<?>>) getServletContext().getAttribute("framework.controllers");
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>Annotated Controllers</title></head><body>");
            html.append("<h1>Classes annotées @Controller</h1>");
            if (controllers == null || controllers.isEmpty()) {
                html.append("<p>Aucune classe trouvée.</p>");
            } else {
                html.append("<ul>");
                for (Class<?> c : controllers) {
                    html.append("<li>").append(c.getName()).append("</li>");
                }
                html.append("</ul>");

                html.append("<h2>Methodes annotées @Methode</h2>");
                html.append("<ul>");
                for (Class<?> c : controllers) {
                    for (Method m : c.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(Methode.class)) {
                            Methode ann = m.getAnnotation(Methode.class);
                            String url = ann.url();
                            String link = contextPath + (url.startsWith("/") ? url : "/" + url);
                            html.append("<li>")
                                .append(c.getName())
                                .append("::")
                                .append(m.getName())
                                .append(" -> ")
                                .append("<a href=\"")
                                .append(link)
                                .append("\">")
                                .append(url)
                                .append("</a>")
                                .append("</li>");
                        }
                    }
                }
                html.append("</ul>");
            }
            html.append("</body></html>");
            resp.getWriter().write(html.toString());
            return;
        }

        // Dispatch to controller method if a route matches
        MethodBinding binding = (routes != null) ? routes.get(path) : null;
        if (binding != null) {
            try {
                Object controller = binding.controllerClass.getDeclaredConstructor().newInstance();
                Class<?>[] paramTypes = binding.method.getParameterTypes();
                Object[] args = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    Class<?> pt = paramTypes[i];
                    if (Map.class.isAssignableFrom(pt)) {
                        // Inject request parameters into Map<String, Object> using getParameter (single value)
                        Map<String, String[]> raw = req.getParameterMap();
                        Map<String, Object> paramMap = new HashMap<>();
                        for (String name : raw.keySet()) {
                            paramMap.put(name, req.getParameter(name));
                        }
                        args[i] = paramMap;
                    } else if (HttpServletRequest.class.isAssignableFrom(pt)) {
                        args[i] = req;
                    } else if (HttpServletResponse.class.isAssignableFrom(pt)) {
                        args[i] = resp;
                    } else {
                        // Unsupported parameter type for now
                        args[i] = null;
                    }
                }

                Object result = binding.method.invoke(controller, args);
                if (result != null) {
                    resp.setContentType("text/plain;charset=UTF-8");
                    resp.getWriter().write(String.valueOf(result));
                } else {
                    // No content produced by controller
                    if (!resp.isCommitted()) {
                        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                }
                return;
            } catch (Exception ex) {
                throw new ServletException("Erreur d'invocation de la méthode contrôleur: "
                        + binding.controllerClass.getName() + "::" + binding.method.getName(), ex);
            }
        }

        // No route found -> 404
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write("<h1>404 - Page non trouvée: " + path + "</h1>");
    }
}
