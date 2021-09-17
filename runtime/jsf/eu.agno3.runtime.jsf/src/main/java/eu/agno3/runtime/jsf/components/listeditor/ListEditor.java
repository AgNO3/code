/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2013 by mbechler
 */
package eu.agno3.runtime.jsf.components.listeditor;


import java.util.ArrayList;
import java.util.List;

import javax.el.MethodExpression;
import javax.faces.component.UINamingContainer;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.runtime.jsf.components.ResetComponentsVisitCallback;
import eu.agno3.runtime.jsf.components.ResettableComponent;


/**
 * @author mbechler
 * @param <T>
 *            list element type
 * 
 */
public class ListEditor <@Nullable T> extends UINamingContainer implements ResettableComponent {

    private static final Logger log = Logger.getLogger(ListEditor.class);

    /**
     * 
     */
    private static final String FACTORY_METHOD = "factoryMethod"; //$NON-NLS-1$

    /**
     * 
     */
    protected static final String VALUE = "value"; //$NON-NLS-1$

    private static final String STYLE_CLASS = "styleClass"; //$NON-NLS-1$

    private transient DataModel<ListElementWrapper<T>> model;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.components.ResettableComponent#resetComponent()
     */
    @Override
    public boolean resetComponent () {
        log.debug("Resetting list editor"); //$NON-NLS-1$
        this.model = null;
        return true;
    }


    /**
     * @return the effective style class
     */
    public String getEffectiveStyleClass () {
        StringBuilder sb = new StringBuilder();

        sb.append("listEditor"); //$NON-NLS-1$

        if ( this.getModel().getRowCount() == 0 ) {
            sb.append(" empty"); //$NON-NLS-1$
        }

        String styleClassAttr = (String) this.getAttributes().get(STYLE_CLASS);
        if ( styleClassAttr != null ) {
            sb.append(' ');
            sb.append(styleClassAttr);
        }

        return sb.toString();
    }


    /**
     * Add a new entry to the list (append)
     */
    @SuppressWarnings ( "unchecked" )
    public void add () {
        List<T> list = this.getList();
        if ( list == null ) {
            log.debug("Adding to a null valued list, create new list"); //$NON-NLS-1$
            this.getModel().setWrappedData(this.createList());
        }

        ( (MutableListWrapper<T>) this.model.getWrappedData() ).addItem(this.newObject());
    }


    /**
     * Remove the a row from the list
     * 
     * @param idx
     *            element index
     */
    public void remove ( int idx ) {
        List<T> list = this.getList();
        if ( list != null ) {
            ( (List<?>) this.model.getWrappedData() ).remove(idx);
            this.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new ResetComponentsVisitCallback(this));
        }
    }


    /**
     * 
     * @param from
     * @param to
     */
    @SuppressWarnings ( "unchecked" )
    public void moveTo ( int from, int to ) {
        if ( from == to ) {
            return;
        }
        List<T> list = this.getList();
        if ( list != null ) {
            ( (MutableListWrapper<T>) this.model.getWrappedData() ).moveTo(from, to);
            this.visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new ResetComponentsVisitCallback(this));
        }
    }


    /**
     * @return the list model for the backing list
     */
    @SuppressWarnings ( "unused" )
    public DataModel<ListElementWrapper<T>> getModel () {
        if ( this.model == null ) {
            List<T> l = this.getList();
            if ( log.isTraceEnabled() ) {
                log.trace("Log trace creating list model from " + l); //$NON-NLS-1$
            }
            if ( l == null ) {
                this.model = new ListDataModel<>();
            }
            else {
                this.model = new ListDataModel<>(new MutableListWrapper<T>(l));
            }
        }
        return this.model;
    }


    @SuppressWarnings ( "unchecked" )
    protected List<T> getList () {
        return (List<T>) getValueExpression(VALUE).getValue(FacesContext.getCurrentInstance().getELContext());
    }


    protected MutableListWrapper<T> createList () {
        List<T> list = new ArrayList<>();

        getValueExpression(VALUE).setValue(FacesContext.getCurrentInstance().getELContext(), list);
        return new MutableListWrapper<>(list);
    }


    @SuppressWarnings ( "unchecked" )
    protected T newObject () {
        MethodExpression factoryMethod = this.getFactoryMethod();
        if ( factoryMethod == null ) {
            return null;
        }
        return (T) factoryMethod.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] {});
    }


    private MethodExpression getFactoryMethod () {
        return (MethodExpression) getAttributes().get(FACTORY_METHOD);
    }
}
