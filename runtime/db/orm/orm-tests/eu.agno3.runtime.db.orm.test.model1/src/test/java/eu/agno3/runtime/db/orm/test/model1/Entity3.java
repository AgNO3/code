/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.07.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.test.model1;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
public class Entity3 {

    public int eid;

    public String test;


    /**
     * @return the eid
     */
    protected int getEid () {
        return this.eid;
    }


    /**
     * @param eid
     *            the eid to set
     */
    protected void setEid ( int eid ) {
        this.eid = eid;
    }


    /**
     * @return the test
     */
    protected String getTest () {
        return this.test;
    }


    /**
     * @param test
     *            the test to set
     */
    protected void setTest ( String test ) {
        this.test = test;
    }
}
