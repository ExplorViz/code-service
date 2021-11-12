package net.explorviz.code.analysis.visitor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.List;

public class MethodVisitor extends VoidVisitorAdapter<List<String>> {

  @Override
  public void visit(final MethodDeclaration n, final List<String> collector) {
    super.visit(n, collector);

    // String result = "";
    //
    // result += calculateFqnBasedOnImport(importNames, n.getTypeAsString());
    // result += " - " + n.getName();
    //
    // result += " - (";
    //
    // for (final Parameter p : n.getParameters()) {
    //
    // result += calculateFqnBasedOnImport(importNames, p.getName().asString());
    // result += ", ";
    // }
    //
    // result += ")";
    //
    // collector.add(result);
  }

}
