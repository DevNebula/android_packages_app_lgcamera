package com.lge.camera.zipcrypto;

public class Runner {
    public static final String OPTION_DECRYPTION = "-D";
    public static final String OPTION_ENCRYPTION = "-E";

    public static void main(String[] args) {
        AESWrapper aw;
        if (args.length != 2) {
            System.out.println("use just 2 parameters");
            printHelp();
        } else if (OPTION_ENCRYPTION.equalsIgnoreCase(args[0])) {
            aw = new AESWrapper();
            aw.writeFile(aw.encrypt(aw.readFile(args[1])), args[1] + "_encrypted");
        } else if (OPTION_DECRYPTION.equalsIgnoreCase(args[0])) {
            aw = new AESWrapper();
            aw.writeFile(aw.decrypt(aw.readFile(args[1])), args[1] + "_decrypted");
        } else {
            System.out.println("use right option");
            printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("HELP==============");
        System.out.println("use option -e,-E for encryption and second parameter is file full path");
        System.out.println("use option -d,-D for decryption and second parameter is file full path");
        System.out.println("HELP==============");
    }
}
