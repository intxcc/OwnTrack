package cc.intx.owntrack;

import java.security.MessageDigest;
import java.security.cert.Certificate;

public class Misc {
    public static String getCertFingerprint(Certificate certificate) {
        byte[] digest = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] der = certificate.getEncoded();
            md.update(der);
            digest = md.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (digest == null) {
            return "";
        }

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(digest.length * 2);

        for (int i = 0; i < digest.length; ++i) {
            buf.append(hexDigits[(digest[i] & 0xf0) >> 4]);
            buf.append(hexDigits[digest[i] & 0x0f]);
        }

        String returnString = buf.toString();

        return returnString.toUpperCase();
    }
}
