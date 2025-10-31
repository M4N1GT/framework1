package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FrontServlet extends HttpServlet {

    private Map<String, Class<?>> routeRegistry = new HashMap<>();

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

        System.out.println("[FrontServlet] Contrôleurs détectés (" + controllers.size() + "):");
        for (Class<?> c : controllers) {
            System.out.println(" - " + c.getName());
            // Map URL to controller class (e.g., /Alpha -> AlphaController)
            String name = c.getSimpleName().replace("Controller", "");
            String urlPath = "/" + name;
            routeRegistry.put(urlPath, c);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
       
        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestUri.substring(contextPath.length());
        System.out.println("Vous essayez d'acceder a : " + requestUri + " -> path: " + path);

        // Find the controller for the requested URL (path within the context)
        Class<?> controller = routeRegistry.get(path);
        if (controller != null) {
            resp.setContentType("text/html");
            resp.getWriter().write("<h1>Classe correspondante : " + controller.getName() + "</h1>");
        } else {
            // Default response if no controller matches
            resp.setContentType("text/html");
            resp.getWriter().write("<h1>Aucune classe correspondante trouvée pour : " + path + "</h1>");
        }
    }
}
