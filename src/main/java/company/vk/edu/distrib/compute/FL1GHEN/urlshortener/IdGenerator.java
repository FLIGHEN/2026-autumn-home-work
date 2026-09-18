package company.vk.edu.distrib.compute.FL1GHEN.urlshortener;
import java.security.SecureRandom;

public class IdGenerator {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom generator = new SecureRandom();

    public static String GetId(int length){
        StringBuilder result = new StringBuilder(length);
        for(int i = 0; i < length; i++){
            int nextCharInd = generator.nextInt(0, ALPHABET.length());

            result.append(ALPHABET.charAt(nextCharInd));
        }

        return result.toString();
    }

    public static boolean validate(String id){
        if(id.length() == 0)
            return false;

        for(int i = 0; i < id.length(); i++){
            if(ALPHABET.indexOf(id.charAt(i)) == -1)
                return false;
        }
        return true;
    }
}
