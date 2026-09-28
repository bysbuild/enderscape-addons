import com.sun.source.util.JavacTask;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;

/** Syntax-only check; does not resolve Minecraft APIs or verify compilation. */
public final class VerifyJavaSyntax {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length == 0 ? "." : args[0]);
        List<Path> sources = new ArrayList<>();
        for (String module : List.of("enderscape-expansion", "enderscape-trim-bridge")) {
            try (var files = Files.walk(root.resolve(module).resolve("src/main/java"))) {
                sources.addAll(files.filter(p -> p.toString().endsWith(".java")).toList());
            }
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("JDK 21 is required");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, java.nio.charset.StandardCharsets.UTF_8)) {
            JavacTask task = (JavacTask)compiler.getTask(null, manager, diagnostics,
                List.of("-proc:none", "--release", "21"), null, manager.getJavaFileObjectsFromPaths(sources));
            task.parse().forEach(unit -> {});
        }
        var errors = diagnostics.getDiagnostics().stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).toList();
        errors.forEach(System.err::println);
        if (!errors.isEmpty()) throw new IllegalStateException("Java syntax errors: " + errors.size());
        System.out.println("Java 21 syntax: PASS (" + sources.size() + " files). Full compilation not checked.");
    }
}
