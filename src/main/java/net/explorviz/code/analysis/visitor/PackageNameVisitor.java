package net.explorviz.code.analysis.visitor;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class PackageNameVisitor extends GenericVisitorAdapter<String, String> {

  @Override
  public String visit(final PackageDeclaration n, final String arg) {
    super.visit(n, arg);
    return n.getNameAsString();
  }

}
