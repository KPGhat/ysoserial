package ysoserial.util;

import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;

public class ShiroUtil {
    public static String encrypt(byte[] payload, String base64Key) {
        AesCipherService aes = new AesCipherService();
        byte[] key = org.apache.shiro.codec.Base64.decode(base64Key);
        ByteSource ciphertext = aes.encrypt(payload, key);
        return ciphertext.toString();
    }
}
