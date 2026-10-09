package com.adquisiciones.filtro;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Los recursos estaticos (js, css, html) se entregan sin cache: cada recarga
 * vuelve a pedirlos al servidor, de modo que una nueva compilacion se refleja
 * de inmediato en el navegador en lugar de ejecutar una copia guardada.
 */
public final class RecursoFilter implements Filter {
 @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException {
  ((HttpServletResponse)res).setHeader("Cache-Control","no-store");
  chain.doFilter(req,res);
 }
}
