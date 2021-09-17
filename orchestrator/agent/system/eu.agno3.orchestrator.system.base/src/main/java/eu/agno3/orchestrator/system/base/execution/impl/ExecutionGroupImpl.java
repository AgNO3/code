/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.util.ArrayList;
import java.util.List;

import eu.agno3.orchestrator.system.base.execution.BaseExecutable;
import eu.agno3.orchestrator.system.base.execution.ExecutionGroup;


/**
 * @author mbechler
 *
 */
public class ExecutionGroupImpl implements ExecutionGroup {

    /**
     * 
     */
    private static final long serialVersionUID = -8149696366013787472L;

    private List<BaseExecutable> before = new ArrayList<>();
    private List<BaseExecutable> after = new ArrayList<>();
    private List<BaseExecutable> main = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.BaseExecutable#unitCount()
     */
    @Override
    public int unitCount () {
        return this.before.size() + this.main.size() + this.after.size();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionGroup#getBefore()
     */
    @Override
    public List<BaseExecutable> getBefore () {
        return this.before;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionGroup#getMain()
     */
    @Override
    public List<BaseExecutable> getMain () {
        return this.main;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionGroup#getAfter()
     */
    @Override
    public List<BaseExecutable> getAfter () {
        return this.after;
    }

}
