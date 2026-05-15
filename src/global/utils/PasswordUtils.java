package global.utils;

public class PasswordUtils {

    public static String encrypt(String rawPassword){
        if (rawPassword == null) return null;

        try{
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes());
            StringBuilder sb = new StringBuilder();

            for(byte b : hash){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
