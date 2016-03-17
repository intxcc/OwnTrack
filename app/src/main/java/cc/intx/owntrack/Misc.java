package cc.intx.owntrack;

import android.content.Context;
import android.os.Build;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.cert.Certificate;

/* The sole purpose of this class is to implement a few often used functions without reference to anything OwnTrack specific */
public class Misc {
    /*  Get the hash from a certificate. This hash is the same hash calculated by browsers if you view certificate details.
        Initially the even more save SHA-512 was used, but currently no browser makes it easily visible and as we want
        the user to be able to easily check if the cert is the same as from a e.g. laptop we use the quasi-standard which
        is still very secure */
    public static String getCertFingerprint(Certificate certificate) {
        try {
            return hash("SHA-256", certificate.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /*  Calculate the hash of a byte array. Mostly used to get the hash of a string, but for compatibility
        with the getCertFingerprint function and as it is very easy to convert a string to a byte array,
        we use a byte array as input. The string algorithm is a representation of the algorithm to use.
        This is more or less an attempt to get a function like the dev friendly php function hash($alg, $str) */
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

    /* Retrieve color from the xml. We need this function, because getColor(color) was deprecated, but we need to support the APIs */
    public static int getColor(Context context, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(color, null);
        } else {
            return context.getResources().getColor(color);
        }
    }
}
