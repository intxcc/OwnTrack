package cc.intx.owntrack;

import java.security.MessageDigest;
import java.security.cert.Certificate;

public class Misc {
    public static String getCertFingerprint(Certificate certificate) {
        try {
            return hash("SHA-256", certificate.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String hash(String algorithm, byte[] input) {
        byte[] digest = null;

        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(input);
            digest = md.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (digest == null) {
            return "";
        }

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(digest.length * 2);
        for (byte b: digest) {
            buf.append(hexDigits[(b & 0xf0) >> 4]);
            buf.append(hexDigits[b & 0x0f]);
        }

        String returnString = buf.toString();

        return returnString.toUpperCase();
    }
}
