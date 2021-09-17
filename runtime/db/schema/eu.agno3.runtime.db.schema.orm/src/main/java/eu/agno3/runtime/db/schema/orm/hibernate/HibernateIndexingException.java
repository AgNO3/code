/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.hibernate;


/**
 * @author mbechler
 * 
 */
public class HibernateIndexingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4390183165084708132L;


    /**
     * 
     */
    public HibernateIndexingException () {}


    /**
     * @param msg
     */
    public HibernateIndexingException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public HibernateIndexingException ( Throwable t ) {
        super(t);
    }


    /**
     * @param msg
     * @param t
     */
    public HibernateIndexingException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
