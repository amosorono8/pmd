/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.errorprone;

import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.JModifier;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.java.rule.internal.TestFrameworksUtil;

/**
 * The method clone() should only be implemented if the class implements the
 * Cloneable interface with the exception of a final method that only throws
 * CloneNotSupportedException. This version uses PMD's type resolution
 * facilities, and can detect if the class implements or extends a Cloneable
 * class
 *
 * @author acaplan
 */
public class DetachedTestCaseRule extends AbstractJavaRulechainRule {

    public DetachedTestCaseRule() {
        super(ASTClassOrInterfaceDeclaration.class);
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, final Object data) {
        NodeStream<ASTMethodDeclaration> methods = node.getDeclarations(ASTMethodDeclaration.class);
        if (methods.any(TestFrameworksUtil::isTestMethod)) {
            // looks like a test case
            methods.filter(m -> m.getArity() == 0
                       && m.isVoid()
                       && !m.getModifiers().hasAny(JModifier.STATIC, JModifier.PRIVATE, JModifier.PROTECTED))
                   // the method itself has no annotation
                   .filter(it -> it.getDeclaredAnnotations().isEmpty())
                   .forEach(m -> addViolation(data, m));
        }
        return null;
    }
}
