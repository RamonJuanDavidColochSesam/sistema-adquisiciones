package com.adquisiciones.conexion;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class AplicacionLifecycle implements ServletContextListener {
    @Override public void contextDestroyed(ServletContextEvent event) {
        ConexionManager.getInstancia().close();
    }
}
