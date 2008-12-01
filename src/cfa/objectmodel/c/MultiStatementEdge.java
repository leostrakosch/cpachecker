package cfa.objectmodel.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;

import cfa.objectmodel.AbstractCFAEdge;
import cfa.objectmodel.CFAEdgeType;


public class MultiStatementEdge extends AbstractCFAEdge
{
    private List<IASTExpression> expressions;
    private boolean jumpEdge;
    
    public MultiStatementEdge (String rawStatement,
                              List<IASTExpression> expressions)
    {
        super (rawStatement);
        this.jumpEdge = false;
        
        if (expressions == null)
            this.expressions = new ArrayList<IASTExpression> ();
        else
            this.expressions = expressions;
    }
    
    public void setIsJumpEdge (boolean jumpEdge)
    {
        this.jumpEdge = jumpEdge;
    }
    
    @Override
    public boolean isJumpEdge ()
    {
        return jumpEdge;
    }

    public CFAEdgeType getEdgeType ()
    {
        return CFAEdgeType.MultiStatementEdge;
    }
    
    public List<IASTExpression> getExpressions ()
    {
        return expressions;
    }
    
    @Override
    public String getRawStatement ()
    {
        StringBuilder builder = new StringBuilder ();
        
        for (IASTExpression expr : expressions)
        {
            builder.append (expr.getRawSignature ()).append ("\\n");
        }
        
        return builder.toString ();
    }
}
