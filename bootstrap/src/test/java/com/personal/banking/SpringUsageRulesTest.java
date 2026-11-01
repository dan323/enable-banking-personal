package com.personal.banking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Enforces the wiring rules that Maven module boundaries cannot:
 * beans are created only in bootstrap's config classes; controllers are the one place with Spring annotations.
 */
class SpringUsageRulesTest {

    private static final Path ROOT = Path.of("..");
    private static final List<String> MODULES = List.of("domain", "port", "application",
            "adapter-enablebanking", "adapter-persistence", "adapter-web");
    private static final Pattern STEREOTYPES = Pattern.compile(
            "@(Component|Service|Repository|Autowired|Configuration|Bean|Value|ConfigurationProperties)\\b");
    private static final Pattern ANY_SPRING_IMPORT = Pattern.compile("^import org\\.springframework", Pattern.MULTILINE);

    @Test
    void noStereotypeOrWiringAnnotationsOutsideBootstrap() throws IOException {
        for (Path file : javaFiles(MODULES)) {
            String src = Files.readString(file);
            assertTrue(!STEREOTYPES.matcher(src).find(), file + " must not use bean/wiring annotations");
        }
    }

    @Test
    void springOnlyAppearsInControllersAndTheirDependencies() throws IOException {
        List<String> noSpringModules = List.of("domain", "port", "application", "adapter-persistence");
        for (Path file : javaFiles(noSpringModules)) {
            assertTrue(!ANY_SPRING_IMPORT.matcher(Files.readString(file)).find(), file + " must not import Spring");
        }
        for (Path file : javaFiles(List.of("adapter-web"))) {
            if (file.getFileName().toString().endsWith("Controller.java")) continue;
            assertTrue(!ANY_SPRING_IMPORT.matcher(Files.readString(file)).find(),
                    file + " must not import Spring (only controllers may)");
        }
    }

    private List<Path> javaFiles(List<String> modules) throws IOException {
        return modules.stream().flatMap(m -> {
            try (Stream<Path> s = Files.walk(ROOT.resolve(m).resolve("src/main/java"))) {
                return s.filter(f -> f.toString().endsWith(".java")).toList().stream();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }).toList();
    }
}
