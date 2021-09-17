/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.01.2014 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


import java.util.Arrays;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "static-method" )
public final class FilterBuilder {

    private static FilterBuilder instance;


    /**
     * 
     */
    private FilterBuilder () {}


    /**
     * @return a filter builder
     */
    public static FilterBuilder get () {
        if ( instance == null ) {
            instance = new FilterBuilder();
        }
        return instance;
    }


    /**
     * 
     * @return the empty expression
     */
    public EmptyExpression empty () {
        return new EmptyExpression();
    }


    /**
     * @param attr
     * @param value
     * @return an = expression
     */
    public SimpleFilterExpression eq ( String attr, String value ) {
        return new SimpleFilterExpression(attr, FilterType.EQUALS, value);
    }


    /**
     * @param attr
     * @param value
     * @return an >= expression
     */
    public SimpleFilterExpression ge ( String attr, String value ) {
        return new SimpleFilterExpression(attr, FilterType.GREATER_EQUALS, value);
    }


    /**
     * 
     * @param attr
     * @param value
     * @return an <= expression
     */
    public SimpleFilterExpression le ( String attr, String value ) {
        return new SimpleFilterExpression(attr, FilterType.LESS_EQUALS, value);
    }


    /**
     * 
     * @param attr
     * @param value
     * @return an ~= expression
     */
    public SimpleFilterExpression approx ( String attr, String value ) {
        return new SimpleFilterExpression(attr, FilterType.APPROX, value);
    }


    /**
     * @param attr
     * @return an exists (=*) expression
     */
    public ExistsFilterExpression exists ( String attr ) {
        return new ExistsFilterExpression(attr);

    }


    /**
     * 
     * @param e
     * @return the negated expression
     */
    public NotExpression not ( FilterExpression e ) {
        return new NotExpression(e);
    }


    /**
     * 
     * @param exps
     *            clauses
     * @return anded clauses
     */
    public AndExpression and ( FilterExpression... exps ) {
        return new AndExpression(Arrays.asList(exps));
    }


    /**
     * 
     * @param exps
     *            clauses
     * @return ored clauses
     */
    public OrExpression or ( FilterExpression... exps ) {
        return new OrExpression(Arrays.asList(exps));
    }

}
