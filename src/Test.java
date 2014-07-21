import org.objectweb.asm.tree.*;
import tracer.IClassLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JacaDev
 */
public class Test {
    public static void main(String... args) throws Exception {
        runtime();
    }
    private static void runtime() throws Exception {
        IClassLoader loader = new IClassLoader();
        Class clazz = loader.loadClass("Test");
        clazz.getMethod("test", int.class).invoke(null, 20);
    }
    public static void test(int lol){
        int number = 20;
        Object object = null;
        object = new Integer(80);
        List<Object> objects = new ArrayList<>();
        objects.add(object);
        objects = objects;
        lol = 80;
    }
    /**
     * Out:
     * Test:test(I)V I number = 20
     * Test:test(I)V Ljava/lang/Object; object = null
     * Test:test(I)V Ljava/lang/Object; object = 80
     * Test:test(I)V Ljava/util/List; objects = []
     * Test:test(I)V Ljava/util/List; objects = [80]
     * Test:test(I)V I lol = 80
     */
}
