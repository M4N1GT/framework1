package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class FrontServlet extends HttpServlet {

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
            }
            html.append("</body></html>");
            resp.getWriter().write(html.toString());
            return;
        }

        // Pour les autres chemins, simple 404
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write("<h1>404 - Page non trouvée: " + path + "</h1>");
    }
}
