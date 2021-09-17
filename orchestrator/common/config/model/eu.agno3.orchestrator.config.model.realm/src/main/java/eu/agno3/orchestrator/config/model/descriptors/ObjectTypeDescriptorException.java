/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


/**
 * @author mbechler
 * 
 */
public class ObjectTypeDescriptorException extends RuntimeException {

    /**
     * 
     */
    public ObjectTypeDescriptorException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public ObjectTypeDescriptorException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ObjectTypeDescriptorException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ObjectTypeDescriptorException ( Throwable t ) {
        super(t);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8844073790360783185L;

}
