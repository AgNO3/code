/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.util.Map;


/**
 * @author mbechler
 * 
 */
public class MapEnvironment implements Environment {

    /**
     * 
     */
    private static final long serialVersionUID = 4148051598776174195L;
    private Map<String, String> env;


    /**
     * @param env
     */
    public MapEnvironment ( Map<String, String> env ) {
        this.env = env;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.Environment#getEnv()
     */
    @Override
    public Map<String, String> getEnv () {
        return this.env;
    }

}
