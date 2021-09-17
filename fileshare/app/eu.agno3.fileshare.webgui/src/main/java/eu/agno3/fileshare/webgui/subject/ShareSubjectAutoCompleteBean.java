/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.autocomplete.AutoComplete;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@RequestScoped
@Named ( "shareSubjectAutocompleteBean" )
public class ShareSubjectAutoCompleteBean {

    @Inject
    private FileshareServiceProvider fsp;

    private AutoComplete component;

    private SubjectQueryResult value;


    /**
     * @param component
     *            the component to set
     */
    public void setComponent ( AutoComplete component ) {
        this.component = component;
    }


    /**
     * @return the component
     */
    public AutoComplete getComponent () {
        return this.component;
    }


    /**
     * @return the value
     */
    public SubjectQueryResult getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( SubjectQueryResult value ) {
        this.value = value;
    }


    /**
     * @param query
     * @return completion results
     */
    public List<SubjectQueryResult> complete ( String query ) {
        try {
            return this.fsp.getSubjectService().querySubjects(query, 20);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }
}
