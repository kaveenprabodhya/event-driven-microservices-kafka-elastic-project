package com.microservices.demo.jasypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class TestJasypt {
    public static void main(String[] args) {
        StandardPBEStringEncryptor pbeStringEncryptor = new StandardPBEStringEncryptor();
        pbeStringEncryptor.setPassword("qwerty@123");
        pbeStringEncryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        pbeStringEncryptor.setIvGenerator(new RandomIvGenerator());
        String result = pbeStringEncryptor.encrypt("qwerty@123");
        System.out.println(result);
        System.out.println(pbeStringEncryptor.decrypt(result));
    }
}