/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

import java.util.Optional;

import net.sourceforge.pmd.lang.ast.Node;


/**
 * A field access expression.
 *
 * <pre>
 *
 * TODO the third branch is ambiguous with the second and won't be matched without a rewrite phase
 *
 * MethodCall ::=  {@link ASTName MethodName} {@link ASTArgumentList ArgumentList}
 *              |  {@link ASTPrimaryExpression PrimaryExpression} "." {@link ASTTypeArguments TypeArguments}? &lt;IDENTIFIER&gt; {@link ASTArgumentList ArgumentList}
 *              |  {@link ASTClassOrInterfaceType ClassName} "." {@link ASTTypeArguments TypeArguments}? &lt;IDENTIFIER&gt; {@link ASTArgumentList ArgumentList}
 * </pre>
 */
public final class ASTMethodCall extends AbstractJavaTypeNode implements ASTPrimaryExpression, LateInitNode {

    private ASTName methodName;


    ASTMethodCall(int id) {
        super(id);
    }


    ASTMethodCall(JavaParser p, int id) {
        super(p, id);
    }


    @Override
    public void onInjectFinished() {
        // perform some preliminary disambiguation before we have a true rewrite pass

        if (getExplicitTypeArguments().isPresent()) {
            // then the name is already separate from the lhs
            methodName = (ASTName) jjtGetChild(jjtGetNumChildren() - 2);
        } else {

            Node fstChild = jjtGetChild(0);
            if (fstChild.getClass() == ASTName.class) {
                methodName = (ASTName) fstChild;
            } else if (fstChild instanceof ASTAmbiguousNameExpr) {
                String image = fstChild.getImage();
                int lastDotIdx = image.lastIndexOf('.');

                if (lastDotIdx < 0) {
                    // no dot, this is just a method call like foo()
                    // so we promote the ambiguous expr to a method name
                    ASTName newName = new ASTName(image);
                    newName.copyTextCoordinates(fstChild);
                    replaceChildSameLength(0, newName);
                    methodName = newName;
                } else {

                    String realName = image.substring(lastDotIdx + 1);
                    String remainingAmbiguous = image.substring(0, lastDotIdx);

                    fstChild.setImage(remainingAmbiguous);

                    ASTName newName = new ASTName(realName);
                    newName.copyTextCoordinates(fstChild);

                    ((ASTAmbiguousNameExpr) fstChild).shiftColumns(0, -(realName.length() + 1));
                    newName.shiftColumns(remainingAmbiguous.length() + 1, 0);

                    insertChild(newName, 1);
                    this.methodName = newName;
                }
            } else {
                methodName = getFirstChildOfType(ASTName.class);
            }
        }
        assert methodName != null && !(methodName instanceof ASTAmbiguousNameExpr);
    }

    public String getMethodName() {
        return getNameNode().getImage();
    }


    /**
     * TODO nobody needs this, setting the image would be enough, but
     * we need a procedure to delete nodes.
     */
    public ASTName getNameNode() {
        return methodName;
    }


    public Optional<ASTPrimaryExpression> getLeftHandSide() {
        // note: getNameNode must be called before jjtGetChild here because it changes the first child
        return getNameNode() == jjtGetChild(0)
               ? Optional.empty()
               : Optional.of((ASTPrimaryExpression) jjtGetChild(0));
    }


    public ASTArgumentList getArguments() {
        return (ASTArgumentList) jjtGetChild(jjtGetNumChildren() - 1);
    }


    public Optional<ASTTypeArguments> getExplicitTypeArguments() {
        return Optional.ofNullable(getFirstChildOfType(ASTTypeArguments.class));
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }
}
