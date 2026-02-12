package com.frameLab.frameSprite.utils;


import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.PredefinedAeadParameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class EncryptUtils {
    private static final  Aead AEAD;
    private static final  File KEY_FILE;
    static  {
        try {
            AeadConfig.register();
        KEY_FILE = new File("keyset.json");
        KeysetHandle keysetHandle;

        if (KEY_FILE.exists()){
            String jsonKeySet = Files.readString(KEY_FILE.toPath());
            keysetHandle = TinkJsonProtoKeysetFormat.parseKeyset(jsonKeySet,InsecureSecretKeyAccess.get());
        } else {
            keysetHandle = KeysetHandle.generateNew(PredefinedAeadParameters.AES128_GCM);
           String jsonKeySet = TinkJsonProtoKeysetFormat.serializeKeyset(keysetHandle,InsecureSecretKeyAccess.get());
            Files.writeString(KEY_FILE.toPath(),jsonKeySet);
        }
        AEAD = keysetHandle.getPrimitive(Aead.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String encrypt(String stringToEncrypt){
        if (stringToEncrypt == null) {
            return null;
        }
        try {
            byte[] plaintext = stringToEncrypt.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = AEAD.encrypt(plaintext, new byte[0]);
            return Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String stringToDecrypt) {
        if (stringToDecrypt == null) {
            return null;
        }
        try {
            byte[] ciphertext = Base64.getDecoder().decode(stringToDecrypt);
            byte[] plaintext = AEAD.decrypt(ciphertext, new byte[0]);
            return new String(plaintext,StandardCharsets.UTF_8);

        } catch (Exception e){
            throw new RuntimeException(e);
        }

    }


}
