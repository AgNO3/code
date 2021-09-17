/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class KeyInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8531214664578593907L;

    private long kvno;
    private String algo;
    private String principal;


    /**
     * @return the kvno
     */
    public long getKvno () {
        return this.kvno;
    }


    /**
     * @param kvno
     *            the kvno to set
     */
    public void setKvno ( long kvno ) {
        this.kvno = kvno;
    }


    /**
     * @return the algo
     */
    public String getAlgorithm () {
        return this.algo;
    }


    /**
     * @param algo
     *            the algo to set
     */
    public void setAlgorithm ( String algo ) {
        this.algo = algo;
    }


    /**
     * @param principal
     */
    public void setPrincipal ( String principal ) {
        this.principal = principal;
    }


    /**
     * @return the principal
     */
    public String getPrincipal () {
        return this.principal;
    }

}
