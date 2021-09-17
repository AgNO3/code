/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.agno3.orchestrator.system.base.execution.BaseExecutable;
import eu.agno3.orchestrator.system.base.execution.ExecutionGroup;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.JobIterator;
import eu.agno3.orchestrator.system.base.execution.Phase;


/**
 * @author mbechler
 *
 */
public class JobIteratorImpl implements JobIterator {

    /**
     * 
     */
    private static final long serialVersionUID = 8464416473655992603L;

    private List<BaseExecutable> list = new ArrayList<>();
    private List<BaseExecutable> failureList = new ArrayList<>();


    /**
     * 
     */
    public JobIteratorImpl () {}


    /**
     * @param executables
     */
    public JobIteratorImpl ( List<? extends BaseExecutable> executables ) {
        this.list = Collections.unmodifiableList(executables);
    }


    @Override
    public Iterator<ExecutionUnit<?, ?, ?>> iterate ( Phase p ) {
        List<ExecutionUnit<?, ?, ?>> units = new ArrayList<>();

        if ( p == Phase.VALIDATE ) {
            addUnits(this.failureList, units, null, false);
        }

        addUnits(this.list, units, null, false);

        return new JobListIterator(units);
    }


    /**
     * @param units
     */
    private static void addUnits ( List<BaseExecutable> source, List<ExecutionUnit<?, ?, ?>> units, Collection<ExecutionUnit<?, ?, ?>> filter,
            boolean reverse ) {
        int size = source.size();
        for ( int i = 0; i < size; i++ ) {
            BaseExecutable e = source.get(reverse ? ( size - i - 1 ) : i);
            if ( e instanceof ExecutionUnit<?, ?, ?> ) {
                addIfMatch(units, (ExecutionUnit<?, ?, ?>) e, filter);
            }
            else if ( e instanceof ExecutionGroup ) {
                addUnits( ( (ExecutionGroup) e ).getBefore(), units, filter, reverse);
                addUnits( ( (ExecutionGroup) e ).getMain(), units, filter, reverse);
                addUnits( ( (ExecutionGroup) e ).getAfter(), units, filter, reverse);
            }
            else {
                throw new IllegalArgumentException("Unsupported executable"); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param e
     * @param filter
     */
    private static void addIfMatch ( List<ExecutionUnit<?, ?, ?>> units, ExecutionUnit<?, ?, ?> e, Collection<ExecutionUnit<?, ?, ?>> filter ) {
        if ( filter == null || filter.contains(e) ) {
            units.add(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobIterator#reversedIterator(java.util.Collection)
     */
    @Override
    public JobIterator reversedIterator ( Collection<ExecutionUnit<?, ?, ?>> toRollback ) {
        List<ExecutionUnit<?, ?, ?>> units = new ArrayList<>();
        addUnits(this.list, units, toRollback, true);
        return new JobIteratorImpl(units);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobIterator#failure(eu.agno3.orchestrator.system.base.execution.Phase)
     */
    @Override
    public JobIterator failure ( Phase failedIn ) {
        if ( failedIn != Phase.EXECUTE ) {
            return new JobIteratorImpl();
        }
        return new JobIteratorImpl(this.failureList);
    }


    @Override
    public void add ( BaseExecutable unit ) {
        this.list.add(unit);
    }


    @Override
    public void addFailureUnit ( BaseExecutable unit ) {
        this.failureList.add(unit);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobIterator#size()
     */
    @Override
    public int size () {
        int unitCount = 0;
        for ( BaseExecutable baseExecutable : this.list ) {
            unitCount += baseExecutable.unitCount();
        }
        return unitCount;
    }

}
