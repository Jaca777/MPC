import tracer.ClassLoaderImpl;
import tracer.JarInjector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author JacaDev
 */
public class Test {
    public static void main(String... args) throws Exception {
        runtime();
    }
    @SuppressWarnings("unchecked")
    private static void runtime() throws Exception {
        ClassLoaderImpl loader = new ClassLoaderImpl();
        Class clazz = loader.loadClass("Test");
        clazz.getMethod("test", int.class).invoke(null, 20);
    }
    private static void toJar() throws Exception {
        JarFile in = new JarFile(new File("src/SampleJar.jar"));
        File out = new File("src/out.jar");
        JarInjector.inject(in, out);
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
