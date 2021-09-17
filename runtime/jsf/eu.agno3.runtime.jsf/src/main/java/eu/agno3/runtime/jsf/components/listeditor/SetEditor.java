/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.listeditor;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.runtime.jsf.components.ResettableComponent;
import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class SetEditor <@Nullable T> extends ListEditor<T> implements ResettableComponent {

    private static final Logger log = Logger.getLogger(SetEditor.class);

    private static final String LIST = "list"; //$NON-NLS-1$
    private static final String COMPARATOR = "comparator"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.listeditor.ListEditor#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        this.getStateHelper().remove(LIST);
        return super.resetComponent();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.listeditor.ListEditor#getEffectiveStyleClass()
     */
    @Override
    public String getEffectiveStyleClass () {
        return super.getEffectiveStyleClass() + " set"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.listeditor.ListEditor#getList()
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    protected List<T> getList () {
        List<T> l = (List<T>) this.getStateHelper().get(LIST);

        if ( l == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Creating list from delegate set " + getValueExpression(VALUE).getExpressionString()); //$NON-NLS-1$
            }
            Set<T> delegateSet = (Set<T>) getValueExpression(VALUE).getValue(FacesContext.getCurrentInstance().getELContext());

            if ( delegateSet == null ) {
                delegateSet = new HashSet<>();
            }
            l = new ArrayList<>(delegateSet);
            Collections.sort(l, getSetComparator());
            this.getStateHelper().put(LIST, l);

        }
        return l;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.listeditor.ListEditor#newObject()
     */
    @Override
    protected T newObject () {
        T obj = super.newObject();

        if ( obj == null ) {
            throw new FacesException("For setEditors a factoryMethod must be set and not return null"); //$NON-NLS-1$
        }

        return obj;
    }


    private Comparator<T> getSetComparator () {
        @SuppressWarnings ( "unchecked" )
        Comparator<T> comp = (Comparator<T>) this.getAttributes().get(COMPARATOR);

        if ( comp == null ) {
            throw new FacesException("No set comparator set"); //$NON-NLS-1$
        }

        return comp;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.component.UIComponentBase#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext ctx ) {
        super.processUpdates(ctx);

        List<T> l = this.getList();

        SortedSet<T> set = new TreeSet<>(this.getSetComparator());
        set.addAll(l);

        if ( l.size() != set.size() ) {
            ctx.addMessage(this.getClientId(ctx), new FacesMessage(BaseMessages.get("setEditor.duplicateEntries"))); //$NON-NLS-1$
            ctx.validationFailed();
            return;
        }

        getValueExpression(VALUE).setValue(FacesContext.getCurrentInstance().getELContext(), set);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.jsf.components.listeditor.ListEditor#createList()
     */
    @Override
    protected MutableListWrapper<T> createList () {
        MutableListWrapper<T> l = new MutableListWrapper<>(new ArrayList<T>());
        this.getStateHelper().add(LIST, l);
        return l;
    }

}
