/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.move;


import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestExecutionUnit;
import eu.agno3.orchestrator.system.base.units.file.FileResult;


/**
 * @author mbechler
 * 
 */
public class Move extends AbstractFileSourceDestExecutionUnit<Move, MoveConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = -4964912026084504608L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestExecutionUnit#createConfigurator()
     */
    @Override
    public MoveConfigurator createConfigurator () {
        return new MoveConfigurator(this);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.file.AbstractFileSourceDestExecutionUnit#buildCopyOptions(java.util.Set)
     */
    @Override
    protected void buildCopyOptions ( Set<CopyOption> options ) {
        super.buildCopyOptions(options);
        options.add(StandardCopyOption.ATOMIC_MOVE);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult prepare ( Context context ) throws ExecutionException {

        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public FileResult execute ( Context context ) throws ExecutionException {

        return null;
    }

}
