/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.move;

import eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestConfigurator;


/**
 * @author mbechler
 */
public class MoveConfigurator extends AbstractFileSourceDestConfigurator<Move, MoveConfigurator> {

    /**
     * @param unit
     */
    public MoveConfigurator ( Move unit ) {
        super(unit);
    }

}
