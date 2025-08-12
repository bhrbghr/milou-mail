package milou.core;
import java.util.Random;

public class GenerateCode {
    public static String convertToCode(Integer id) {
        if (id == null) return "a1b2c3";
        Random rng = new Random(id);
        StringBuilder code = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return code.toString();
    }

}
