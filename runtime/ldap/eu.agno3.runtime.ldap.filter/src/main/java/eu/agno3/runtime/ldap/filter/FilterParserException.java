/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


/**
 * @author mbechler
 * 
 */
public class FilterParserException extends FilterException {

    /**
     * 
     */
    private static final long serialVersionUID = -1211006100879425068L;


    /**
     * 
     */
    public FilterParserException () {
        super();
    }


    /**
     * 
     * @param msg
     */
    public FilterParserException ( String msg ) {
        super(msg);
    }


    /**
     * 
     * @param t
     */
    public FilterParserException ( Throwable t ) {
        super(t);
    }


    /**
     * 
     * @param msg
     * @param t
     */
    public FilterParserException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
