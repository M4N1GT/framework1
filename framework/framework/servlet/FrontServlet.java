package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class FrontServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext ctx = getServletContext();
        String basePackage = ctx.getInitParameter("app.basePackage");
        if (basePackage == null || basePackage.isBlank()) {
            throw new ServletException("Paramètre de contexte 'app.basePackage' manquant (ex: test.app)");
        }

        // Removed ClassScanner logic
        System.out.println("[FrontServlet] Initialisation terminée sans scanner de classes.");
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
