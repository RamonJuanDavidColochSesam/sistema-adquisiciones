package com.adquisiciones.util;

public class GenerarHash {
    public static void main(String[] args) {
        java.io.Console console = System.console();
        if (console == null) throw new IllegalStateException("Ejecute esta utilidad desde una terminal interactiva");
        char[] password = console.readPassword("Contraseña (12–72 bytes): ");
        if (password == null) return;
        try {
            String value = new String(password);
            int length = value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (length < 12 || length > 72) throw new IllegalArgumentException("Longitud no válida");
            System.out.println(PasswordUtil.hashear(value));
        } finally { java.util.Arrays.fill(password, '\0'); }
    }
}
