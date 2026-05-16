package global.utils;

public class PasswordUtils {

    public static String encrypt(String rawPassword){
        if (rawPassword == null) return null;

        try{
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            for(byte b : hash){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }catch(Exception e){
            throw new RuntimeException("비밀번호 암호화 중 오류 발생", e);
        }
    }
}
