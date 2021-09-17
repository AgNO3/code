/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractComplexExpression implements FilterExpression {

    private List<FilterExpression> clauses;
    private String operatorSymbol;


    /**
     * @param clauses
     * @param operatorSymbol
     * 
     */
    public AbstractComplexExpression ( List<FilterExpression> clauses, String operatorSymbol ) {
        this.clauses = new ArrayList<>(clauses);
        this.operatorSymbol = operatorSymbol;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.ldap.filter.FilterExpression#getSourceLength()
     */
    @Override
    public int getSourceLength () {
        // parentheses + operator symbol
        int length = 2 + this.operatorSymbol.length();

        for ( FilterExpression clause : this.clauses ) {
            length += clause.getSourceLength();
        }

        return length;
    }


    /**
     * @return the OR'ed expressions
     */
    public List<FilterExpression> getClauses () {
        return Collections.unmodifiableList(this.clauses);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof AbstractComplexExpression ) {
            return this.getClauses().equals( ( (AbstractComplexExpression) obj ).getClauses());
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        int hashCode = 0;

        for ( FilterExpression clause : this.getClauses() ) {
            hashCode += clause.hashCode();
        }

        return hashCode;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        StringBuilder buf = new StringBuilder();

        buf.append("("); //$NON-NLS-1$
        buf.append(this.operatorSymbol);
        for ( FilterExpression clause : this.getClauses() ) {
            buf.append(clause);
        }
        buf.append(")"); //$NON-NLS-1$

        return buf.toString();
    }

}