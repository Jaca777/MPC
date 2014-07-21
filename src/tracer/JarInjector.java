package tracer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * @author JacaDev
 */
public class JarInjector {
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 8);

    private static class CallableInjector implements Callable<ClassNode> {
        private final InputStream stream;
        private final ClassNode classNode;
        private final String path;
        public CallableInjector(InputStream stream, String path) {
            this.stream = stream;
            this.classNode = new ClassNode();
            this.path = path;
        }

        @Override
        public ClassNode call() throws Exception {
            ClassReader reader = new ClassReader(stream);
            reader.accept(classNode, 0);
            CallsInjector.inject(classNode, path);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return classNode;
        }
    }

    public static void inject(JarFile in, File out) throws Exception {
        Map<String, byte[]> classes = new HashMap<>();
        Enumeration<JarEntry> entries = in.entries();
        List<CallableInjector> callableInjectors = new ArrayList<CallableInjector>();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                callableInjectors.add(new CallableInjector(in.getInputStream(jarEntry), jarEntry.getName()));
            }
        }

        for (Future<ClassNode> future : EXECUTOR_SERVICE.invokeAll(callableInjectors)) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassNode node = future.get();
            node.accept(writer);
            classes.put(node.name, writer.toByteArray());
        }

        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(out));
        for(Map.Entry<String, byte[]> entry : classes.entrySet()){
            JarEntry jarEntry = new JarEntry(entry.getKey());
            jarOut.putNextEntry(jarEntry);
            jarOut.write(entry.getValue());
            jarOut.closeEntry();
        }
        jarOut.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        in.getManifest().write(jarOut);
        jarOut.close();
    }

}

