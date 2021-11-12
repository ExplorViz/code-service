package net.explorviz.code.analysis.visitor;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.List;

public class ImportVisitor extends VoidVisitorAdapter<List<String>> {

  @Override
  public void visit(final CompilationUnit n, final List<String> collector) {
    super.visit(n, collector);

    for (final ImportDeclaration i : n.getImports()) {

      collector.add(i.getNameAsString());

    }

  }

}
