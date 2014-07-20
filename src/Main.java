import org.objectweb.asm.tree.*;
import tracer.IClassLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JacaDev
 */
public class Main {
    public static void main(String... args) throws Exception {
        IClassLoader loader = new IClassLoader();
        Class clazz = loader.loadClass("Main");
        clazz.getMethod("dupa", int.class).invoke(null, 20);
    }
    private static void printInsn(InsnList list){
        for(int i = 0; i < list.size(); i++){
            System.out.println(list.get(i));
        }
    }
    public static void dupa(int lol){
        int number = 20;
        Object object = null;
        object = new Integer(80);
        List<Object> objects = new ArrayList<>();
        objects.add(object);
        objects = objects;
        lol = 80;
    }
}
