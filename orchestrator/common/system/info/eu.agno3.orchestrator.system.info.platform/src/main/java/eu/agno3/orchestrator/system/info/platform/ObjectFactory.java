/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * @return default cpu core implementation
     */
    public CPUCore createCpuCore () {
        return new CPUCoreImpl();
    }


    /**
     * 
     * @return default cpu info implementation
     */
    public CPUInformation createCpuInformation () {
        return new CPUInformationImpl();
    }


    /**
     * 
     * @return default memory information impl
     */
    public MemoryInformation createMemoryInformation () {
        return new MemoryInformationImpl();
    }


    /**
     * 
     * @return default platform information impl
     */
    public PlatformInformation createPlatformInformation () {
        return new PlatformInformationImpl();
    }

}
