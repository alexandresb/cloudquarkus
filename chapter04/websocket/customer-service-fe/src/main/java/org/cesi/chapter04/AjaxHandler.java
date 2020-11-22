package org.cesi.chapter04;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/ajaxhandler")
public class AjaxHandler extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //si CUSTOMER_SERVICE est déclaré dans le contexte d'exécution,
    // la val de CUSTOMER_SERVICE est utilisée comme URI du endpoint serveur,
    //sinon on utilise ws://localhost:8080/customers
    String endpoint =
                System.getenv("CUSTOMER_SERVICE") != null
                        ? System.getenv("CUSTOMER_SERVICE") : "ws://localhost:8080/customers";

        PrintWriter out = response.getWriter();
        out.println(endpoint);
        out.flush();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}