package a;

import com.zwk.parse.ArgParser;
import com.zwk.parse.correspondent.Correspondent;
import jdk.internal.org.objectweb.asm.Type;

public class SimpleTest {
    public static void main(String[] args) {
        ArgParser argParser = new ArgParser("c.conf");
        Correspondent parse = argParser.parse();
        boolean b = parse.className("com.zwk.model.Cat");

        String className = Type.getObjectType("java/lang/Deprecated").getClassName();
        System.out.println("className = " + className);
    }
}
