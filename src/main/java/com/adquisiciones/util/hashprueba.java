package com.adquisiciones.util;

public class hashprueba {

    public static void main(String[] args) {
        String passwordEnPlano = "DemoAdmin2026!"; // Reemplaza con la contraseña real
        String hashEnBaseDatos = "$2a$12$SoTZcMnjyI09tbSOurFsbuIm8vylYjocy3Gi2SBH/O7p1yVaVNnEi";

        try {
            // Intenta validar con la librería jbcrypt estándar
            boolean esValida = org.mindrot.jbcrypt.BCrypt.checkpw(passwordEnPlano, hashEnBaseDatos);
            System.out.println("¿La contraseña coincide con el hash?: " + esValida);
        } catch (NoClassDefFoundError | Exception e) {
            System.err.println("Error al cargar la librería de BCrypt. Revisa las dependencias del proyecto.");
            e.printStackTrace();
        }
    }
}