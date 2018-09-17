package com.mingyue;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 密码简单加密
 */
public class TestEncode {
    public static void main(String[] args) {

        String passwd = "password";
        byte[] hash = DigestUtils.sha256(passwd);

        System.out.println(Hex.encodeHexString(hash));
        
    }
    
}