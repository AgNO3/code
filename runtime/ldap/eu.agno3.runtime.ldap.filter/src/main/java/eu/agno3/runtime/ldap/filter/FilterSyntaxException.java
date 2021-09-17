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
public class FilterSyntaxException extends FilterParserException {

    /**
     * 
     */
    private static final long serialVersionUID = -8789010142234383375L;

    private final int offset;


    /**
     * @param message
     * @param offset
     *            Offset at which the error occured
     */
    public FilterSyntaxException ( String message, int offset ) {
        super(message);
        this.offset = offset;
    }


    /**
     * @param message
     * @param offset
     *            Offset at which the error occured
     * @param t
     */
    public FilterSyntaxException ( String message, int offset, Throwable t ) {
        super(message, t);
        this.offset = offset;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage () {
        return String.format("Syntax error at offset %d: %s", this.getOffset(), super.getMessage()); //$NON-NLS-1$
    }


    /**
     * @return the offset at which the error occured
     */
    public int getOffset () {
        return this.offset;
    }
}
