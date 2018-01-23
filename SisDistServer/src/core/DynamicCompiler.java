/*
 * @author Reneé Rojas <reneerojas@gmail.com>
 */
package core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import lib.json.JsonArray;
import lib.json.JsonValue;

/**
 *
 */
public class DynamicCompiler {

    private static String classOutputFolder = "dynCodes";

    private DynamicCompiler() {
        makeFolder();
    }

    public static DynamicCompiler getInstance() {
        return DynamicCompilerHolder.INSTANCE;
    }

    private static class DynamicCompilerHolder {

        private static final DynamicCompiler INSTANCE = new DynamicCompiler();
    }

    private void makeFolder() {
        File r = new File(classOutputFolder);
        if (!r.exists()) {
            r.mkdirs();
        }
    }

    public class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {

        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {

            System.out.println("Line Number->" + diagnostic.getLineNumber());
            System.out.println("code->" + diagnostic.getCode());
            System.out.println("Message->"
                    + diagnostic.getMessage(Locale.ENGLISH));
            System.out.println("Source->" + diagnostic.getSource());
            System.out.println(" ");
        }
    }

    /**
     * java File Object represents an in-memory java source file <br>
     * so there is no need to put the source file on hard disk *
     */
    public class InMemoryJavaFileObject extends SimpleJavaFileObject {

        private String contents = null;

        public InMemoryJavaFileObject(String className, String contents) throws Exception {
            super(URI.create("string:///" + className.replace('.', '/')
                    + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.contents = contents;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors)
                throws IOException {
            return contents;
        }
    }

    /**
     * Get a simple Java File Object ,<br>
     * It is just for demo, content of the source code is dynamic in real use
     * case
     */
    private JavaFileObject createJavaClass(String name, String code) {

        StringBuilder contents = new StringBuilder(
                "package dynClass;"
                + code);
        JavaFileObject so = null;
        try {
            so = new InMemoryJavaFileObject(name, contents.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return so;
    }

    /**
     * compile your files by JavaCompiler
     */
    public void compile(Iterable<? extends JavaFileObject> files) {
        //get system compiler:
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // for compilation diagnostic message processing on compilation WARNING/ERROR
        MyDiagnosticListener c = new MyDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(c,
                Locale.ENGLISH,
                null);
        //specify classes output folder
        Iterable options = Arrays.asList("-d", classOutputFolder);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager,
                c, options, null,
                files);
        Boolean result = task.call();
        if (result == true) {
            System.out.println("Compiled");
        }
    }

    public int hasClass(DynamicClass cl) {
        // Create a File object on the root of the directory
        // containing the class file
        File file = new File(classOutputFolder);

        try {
            // Convert File to a URL
            URL url = file.toURI().toURL(); // file:/dynCodes/dynClass/
            URL[] urls = new URL[]{url};

            // Create a new class loader with the directory
            ClassLoader loader = new URLClassLoader(urls);

            // Load in the class; Class.childclass should be located in
            // the directory file:/dynCodes/dynClass/
            Class thisClass = loader.loadClass("dynClass." + cl.getName());

            Class[] params = {};
            Object paramsObj[] = {};
            int count = 0;
            Object instance = thisClass.newInstance();
            Method thisMethod = thisClass.getDeclaredMethod("getVersion", params);

            // run the testAdd() method on the instance:
            return (int) thisMethod.invoke(instance, paramsObj);
        } catch (MalformedURLException e) {
        } catch (ClassNotFoundException e) {
        } catch (Exception ex) {
            return 0;
        }
        return 0;
    }

    /**
     * run class from the compiled byte code file by URLClassloader
     */
    public String run(DynamicClass cl, String method, JsonArray par) {
        // Create a File object on the root of the directory
        // containing the class file
        File file = new File(classOutputFolder);

        try {
            // Convert File to a URL
            URL url = file.toURI().toURL(); // file:/dynCodes/dynClass/
            URL[] urls = new URL[]{url};

            // Create a new class loader with the directory
            ClassLoader loader = new URLClassLoader(urls);

            // the directory file:/dynCodes/dynClass/
            Class thisClass = loader.loadClass("dynClass." + cl.getName());

            Class[] params = new Class[par.size()];
            Object paramsObj[] = new Object[par.size()];
            int count = 0;
            for (JsonValue val : par) {
                params[count] = int.class;
                paramsObj[count] = val.asInt();
                count++;
            }
            Object instance = thisClass.newInstance();
            Method thisMethod = thisClass.getDeclaredMethod(method, params);

            // run the testAdd() method on the instance:
            return (String) ""+thisMethod.invoke(instance, paramsObj);
        } catch (MalformedURLException e) {
        } catch (ClassNotFoundException e) {
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    public void initClass(DynamicClass dyn) {
        JavaFileObject file = createJavaClass(dyn.getName(), dyn.codeProperty().get());
        Iterable<? extends JavaFileObject> files = Arrays.asList(file);

        //2.Compile your files by JavaCompiler
        compile(files);
    }

    public void test() {
        String name = "Somador";
        int version = 2;
        String code = "public class Somador { "
                + "  public void testAdd(int i1, int i2) { "
                + "    System.out.println(i1+i2); "
                + "  }"
                + " public int getVersion(){return 2;}"
                + "} ";
        DynamicClass d1 = new DynamicClass("Somador", 2, code);
        if (hasClass(d1) < version) {
            initClass(d1);
        }

        name = "Somador";
        code = "public class Somador { "
                + "  public int testAdd(int i1, int i2) { "
                + "    return(i1+i2); "
                + "  }"
                + " public int getVersion(){return 3;}"
                + "} ";
        DynamicClass d2 = new DynamicClass("Somador", 3, code);
        if (hasClass(d2) < version) {
            initClass(d2);
        }
        
        name = "Subtrator";
        code = "public class Subtrator { "
                + "  public int testAdd(int i1, int i2) { "
                + "    return(i2-i1); "
                + "  }"
                + " public int getVersion(){return 2;}"
                + "} ";
        DynamicClass d3 = new DynamicClass("Subtrator", 2, code);
        if (hasClass(d3) < version) {
            initClass(d3);
        }

        System.out.println("ver: " + hasClass(d1));
        //3.Load your class by URLClassLoader, then instantiate the instance, and call method by reflection
        JsonArray arr = new JsonArray();
        arr.add(2);
        arr.add(3);
        run(d1, "testAdd", arr);
        run(d3, "testAdd", arr);
    }
}
