/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.contents;


import eu.agno3.orchestrator.system.base.units.file.AbstractFileConfigurator;


/**
 * @author mbechler
 * 
 */
public class ContentsConfigurator extends AbstractFileConfigurator<Contents, ContentsConfigurator> {

    /**
     * @param unit
     */
    protected ContentsConfigurator ( Contents unit ) {
        super(unit);
    }


    /**
     * @param provider
     * @return this configurator
     */
    public ContentsConfigurator content ( ContentProvider provider ) {
        this.getExecutionUnit().setContentProvider(provider);
        return this.self();
    }


    /**
     * @param bytes
     * @return this configurator
     */
    public ContentsConfigurator content ( byte[] bytes ) {
        return this.content(new ByteContentProvider(bytes));
    }


    /**
     * 
     * @return this configurator
     */
    public ContentsConfigurator noHashTracking () {
        this.getExecutionUnit().setNoHashTracking(true);
        return this.self();
    }

}
