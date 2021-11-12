package net.explorviz.code.analysis;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.utils.SourceRoot.Callback;
import io.quarkus.runtime.StartupEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import net.explorviz.code.analysis.util.FqnCalculator;
import net.explorviz.code.analysis.visitor.ClassNameVisitor;
import net.explorviz.code.analysis.visitor.ImplementedInterfaceVisitor;
import net.explorviz.code.analysis.visitor.ImportVisitor;
import net.explorviz.code.analysis.visitor.InheritanceVisitor;
import net.explorviz.code.analysis.visitor.LocVisitor;
import net.explorviz.code.analysis.visitor.MethocCallVisitor;
import net.explorviz.code.analysis.visitor.MethodVisitor;
import net.explorviz.code.analysis.visitor.PackageNameVisitor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JavaParserConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaParserConfiguration.class);

  private final ParserConfiguration config;

  private final String folderPath;

  final VoidVisitor<List<String>> classNameVisitor = new ClassNameVisitor();
  final GenericVisitorAdapter<Integer, Integer> locCollector = new LocVisitor();
  final VoidVisitor<List<String>> inheritanceCollector = new InheritanceVisitor();
  final VoidVisitor<List<String>> implementedInterfacesCollector =
      new ImplementedInterfaceVisitor();
  final VoidVisitor<List<String>> methodCollector = new MethodVisitor();
  final VoidVisitor<List<String>> methodCallCollector = new MethocCallVisitor();
  final VoidVisitor<List<String>> importVisitor = new ImportVisitor();
  final GenericVisitorAdapter<String, String> packageCollector = new PackageNameVisitor();

  @Inject
  public JavaParserConfiguration(
      @ConfigProperty(name = "explorviz.watchservice.folder") final String folderPath) {

    this.folderPath = folderPath;

    final CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(),
        new JavaParserTypeSolver(this.folderPath));

    this.config = new ParserConfiguration().setStoreTokens(true)
        .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
  }

  public void processFolder(final String folderOrFilePath) throws IOException {
    final Path pathToSource = Paths.get(folderOrFilePath);
    final SourceRoot sourceRoot = new SourceRoot(pathToSource);

    final List<String> className = new ArrayList<>();
    final List<String> superClassNames = new ArrayList<>();
    final List<String> implementedInterfacesClassNames = new ArrayList<>();
    final List<String> methodsOfClass = new ArrayList<>();
    final List<String> calledMethodsInClass = new ArrayList<>();
    final List<String> importNames = new ArrayList<>();

    sourceRoot.parse("", this.config, new Callback() {

      @Override
      public Result process(final Path localPath, final Path absolutePath,
          final ParseResult<CompilationUnit> result) {

        if (result.isSuccessful() && result.getResult().isPresent()) {
          final CompilationUnit cu = result.getResult().get();

          System.out.println("Class names:");

          // print fqn
          JavaParserConfiguration.this.classNameVisitor.visit(cu, className);
          className.forEach(n -> System.out.println(n));
          className.clear();

          System.out.println("Package:");
          System.out.println(JavaParserConfiguration.this.packageCollector.visit(cu, ""));

          System.out.println(JavaParserConfiguration.this.locCollector.visit(cu, 0));

          System.out.println("Imports:");

          JavaParserConfiguration.this.importVisitor.visit(cu, importNames);
          importNames.forEach(n -> System.out.println(n));

          System.out.println("Super classes:");

          JavaParserConfiguration.this.inheritanceCollector.visit(cu, superClassNames);
          superClassNames.forEach(
              n -> System.out.println(FqnCalculator.calculateFqnBasedOnImport(importNames, n)));

          System.out.println("Implemented interfaces:");

          JavaParserConfiguration.this.implementedInterfacesCollector.visit(cu,
              implementedInterfacesClassNames);
          implementedInterfacesClassNames.forEach(
              n -> System.out.println(FqnCalculator.calculateFqnBasedOnImport(importNames, n)));

          System.out.println("Contained Methods:");

          JavaParserConfiguration.this.methodCollector.visit(cu, methodsOfClass);
          methodsOfClass.forEach(n -> System.out.println(n));

          System.out.println("Called methods:");

          JavaParserConfiguration.this.methodCallCollector.visit(cu, calledMethodsInClass);
          calledMethodsInClass.forEach(n -> System.out.println(n));

          importNames.clear();
          superClassNames.clear();
          implementedInterfacesClassNames.clear();
          methodsOfClass.clear();
          calledMethodsInClass.clear();

          System.out.println("");
        }
        return Result.DONT_SAVE;
      }

    });
  }

  /* default */ void onStart(@Observes final StartupEvent ev) throws IOException {
    LOGGER.debug("Starting initial analysis of the source code for directory {} ...",
        this.folderPath);
    this.processFolder(this.folderPath);
  }


}
