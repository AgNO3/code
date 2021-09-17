/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2016 by mbechler
 */
package eu.agno3.runtime.jmx;


/**
 * @author mbechler
 *
 */
public class MBeanHolderImpl implements MBeanHolder {

    private final String objectName;
    private final Object mbean;


    /**
     * @param objectName
     * @param mbean
     * 
     */
    public MBeanHolderImpl ( String objectName, Object mbean ) {
        this.objectName = objectName;
        this.mbean = mbean;
    }


    /**
     * @return the object name
     */
    @Override
    public String getObjectName () {
        return this.objectName;
    }


    /**
     * @return the mbean
     */
    @Override
    public Object getMBean () {
        return this.mbean;
    }

}
