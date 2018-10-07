package com.lge.camera.zipcrypto;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESWrapper {
    private static final String CIPHER_SPEC = "AES/CBC/PKCS5Padding";
    private static final byte[] initVector = new byte[]{(byte) 2, (byte) 0, (byte) 1, (byte) 8, (byte) 0, (byte) 1, (byte) 2, (byte) 5, (byte) 1, (byte) 9, (byte) 7, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1};
    private static final byte[] key = new byte[]{(byte) 67, (byte) 65, (byte) 77, (byte) 69, (byte) 82, (byte) 65, (byte) 83, (byte) 84, (byte) 73, (byte) 67, (byte) 75, (byte) 69, (byte) 82, (byte) 76, (byte) 71, (byte) 69};
    private static final SecretKey secretKey = new SecretKeySpec(key, 0, key.length, "AES");

    public byte[] encrypt(byte[] data) {
        byte[] ecryptedBuffer = null;
        try {
            Cipher aesCipher = Cipher.getInstance(CIPHER_SPEC);
            aesCipher.init(1, secretKey, new IvParameterSpec(initVector));
            return aesCipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ecryptedBuffer;
        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
            return ecryptedBuffer;
        } catch (InvalidAlgorithmParameterException e3) {
            e3.printStackTrace();
            return ecryptedBuffer;
        } catch (InvalidKeyException e4) {
            e4.printStackTrace();
            return ecryptedBuffer;
        } catch (BadPaddingException e5) {
            e5.printStackTrace();
            return ecryptedBuffer;
        } catch (IllegalBlockSizeException e6) {
            e6.printStackTrace();
            return ecryptedBuffer;
        }
    }

    public byte[] decrypt(byte[] data) {
        byte[] ecryptedBuffer = null;
        try {
            Cipher aesCipher = Cipher.getInstance(CIPHER_SPEC);
            aesCipher.init(2, secretKey, new IvParameterSpec(initVector));
            return aesCipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ecryptedBuffer;
        } catch (NoSuchPaddingException e2) {
            e2.printStackTrace();
            return ecryptedBuffer;
        } catch (InvalidAlgorithmParameterException e3) {
            e3.printStackTrace();
            return ecryptedBuffer;
        } catch (InvalidKeyException e4) {
            e4.printStackTrace();
            return ecryptedBuffer;
        } catch (BadPaddingException e5) {
            e5.printStackTrace();
            return ecryptedBuffer;
        } catch (IllegalBlockSizeException e6) {
            e6.printStackTrace();
            return ecryptedBuffer;
        }
    }

    public byte[] readFile(String path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        FileInputStream fis = null;
        byte[] buffer = null;
        try {
            FileInputStream fis2 = new FileInputStream(path);
            try {
                buffer = new byte[fis2.available()];
                fis2.read(buffer, 0, fis2.available());
                try {
                    fis2.close();
                    fis = fis2;
                } catch (IOException e3) {
                    e3.printStackTrace();
                    fis = fis2;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fis = fis2;
                }
            } catch (FileNotFoundException e4) {
                e2 = e4;
                fis = fis2;
                try {
                    e2.printStackTrace();
                    try {
                        fis.close();
                    } catch (FileNotFoundException e22) {
                        e22.printStackTrace();
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                    return buffer;
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        fis.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    } catch (Exception ex22) {
                        ex22.printStackTrace();
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e32 = e5;
                fis = fis2;
                e32.printStackTrace();
                try {
                    fis.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                } catch (Exception ex222) {
                    ex222.printStackTrace();
                }
                return buffer;
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                fis.close();
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e22 = e6;
            e22.printStackTrace();
            fis.close();
            return buffer;
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            fis.close();
            return buffer;
        }
        return buffer;
    }

    public boolean writeFile(byte[] data, String path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        if (data == null) {
            return false;
        }
        if ((data != null && data.length <= 0) || path == null) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(path);
            try {
                fos2.write(data, 0, data.length);
                try {
                    fos2.close();
                    fos = fos2;
                    return true;
                } catch (IOException e3) {
                    e3.printStackTrace();
                    fos = fos2;
                    return false;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fos = fos2;
                    return false;
                }
            } catch (FileNotFoundException e4) {
                e2 = e4;
                fos = fos2;
                try {
                    e2.printStackTrace();
                    try {
                        fos.close();
                        return false;
                    } catch (FileNotFoundException e22) {
                        e22.printStackTrace();
                        return false;
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                        return false;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        fos.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    } catch (Exception ex22) {
                        ex22.printStackTrace();
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e32 = e5;
                fos = fos2;
                e32.printStackTrace();
                try {
                    fos.close();
                    return false;
                } catch (IOException e322) {
                    e322.printStackTrace();
                    return false;
                } catch (Exception ex222) {
                    ex222.printStackTrace();
                    return false;
                }
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                fos.close();
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e22 = e6;
            e22.printStackTrace();
            fos.close();
            return false;
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            fos.close();
            return false;
        }
    }
}
