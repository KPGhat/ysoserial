package ysoserial.util;

import com.sun.org.apache.bcel.internal.Repository;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.Utility;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BCELUtil {
    public static void main(String[] args) throws Exception{
        if (args.length < 2) {
            System.err.println("Usage: java -cp ysoserial-[version]-all.jar [encode|decode] [class|file:filename] [decodeToClassFile]");
            System.err.println("\tExample:\n" +
                "\t\tjava -cp ysoserial.jar ysoserial.util.BCELUtil encode file:filename\n" +
                "\t\tjava -cp ysoserial.jar ysoserial.util.BCELUtil encode classname\n");
            System.exit(0);
        }

        if (args[0].equalsIgnoreCase("encode")) {
            String[] encodeArgs = args[1].split(":");
            String bcelClass = null;
            if (encodeArgs.length == 2) {
                byte[] classContent = Files.readAllBytes(Paths.get(encodeArgs[1]));
                bcelClass = encode(classContent);
            } else {
                bcelClass = encode(encodeArgs[0]);
            }

            System.out.println(bcelClass);
        } else {
            String classFilename = "test.class";
            if (args.length == 3) {
                classFilename = args[2];
            }

            decode(args[1], classFilename);
            System.out.println("Write File Success");
        }
    }
    public static String encode(String classname) throws Exception {
        Class clazz = Class.forName(classname, false, ClassLoader.getSystemClassLoader());
        JavaClass cls = Repository.lookupClass(clazz);
        String code = Utility.encode(cls.getBytes(), true);
        return "$$BCEL$$" + code;
    }

    public static String encode(byte[] classContent) throws Exception {
        String code = Utility.encode(classContent, true);
        return "$$BCEL$$" + code;
    }

    public static void decode(String bcelByteCode, String filename) throws Exception {
        if(bcelByteCode.startsWith("$$BCEL$$")){
            bcelByteCode = bcelByteCode.substring(8);
        }
        byte[] classBytes = Utility.decode(bcelByteCode, true);
        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
            fileOut.write(classBytes);
        }
    }
}
