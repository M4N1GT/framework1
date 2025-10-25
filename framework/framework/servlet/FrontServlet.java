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
       
        String urlPath = req.getRequestURI();
    
        System.out.println("Vous essayez d'acceder a : " + urlPath);
    
        resp.setContentType("text/html");
        resp.getWriter().write("<h1>Vous essayez d'acceder a : " + urlPath + "</h1>");
    }
}
