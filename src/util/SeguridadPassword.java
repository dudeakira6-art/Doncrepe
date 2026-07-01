package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SeguridadPassword {
    private SeguridadPassword() {
    }

    public static String encriptar(String usuario, String password) {
        String base = normalizarUsuario(usuario) + ":" + password;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no esta disponible.", ex);
        }
    }

    public static boolean coincide(String usuario, String passwordIngresado, String passwordGuardado) {
        if (passwordGuardado == null) {
            return false;
        }
        String guardado = passwordGuardado.trim();
        if (guardado.equals(encriptar(usuario, passwordIngresado))) {
            return true;
        }
        // Compatibilidad con datos antiguos cargados en texto plano.
        return guardado.equals(passwordIngresado);
    }

    public static String normalizarRol(String usuario, String rolBD) {
        String usuarioNormalizado = normalizarUsuario(usuario);
        if ("admin".equals(usuarioNormalizado)) {
            return "Gerente";
        }
        if ("empleado".equals(usuarioNormalizado)) {
            return "Empleado";
        }
        return rolBD;
    }

    private static String normalizarUsuario(String usuario) {
        return usuario == null ? "" : usuario.trim().toLowerCase();
    }
}
