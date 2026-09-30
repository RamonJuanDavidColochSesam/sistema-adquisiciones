package com.adquisiciones.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hashear(String contrasenaPlano) {
        return BCrypt.hashpw(contrasenaPlano, BCrypt.gensalt());
    }

    public static boolean verificar(String contrasenaPlano, String hashGuardado) {
        return BCrypt.checkpw(contrasenaPlano, hashGuardado);
    }
}