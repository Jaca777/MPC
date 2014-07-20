package tracer;

/**
 * @author JacaDev
 */
public class Tracer {
    public static void varChanged(Object value, String var, String method){
        System.out.println(method + " " + var + " = " + value);
    }
}
