/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.server.webgui.config.TemplateCacheBean;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.server.webgui.util.completer.Completer;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "resourceLibraryCompleterFactory" )
public class ResourceLibraryCompleterFactory implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3758165540517092426L;

    @Inject
    private TemplateCacheBean tplCache;

    @Inject
    private StructureViewContextBean viewContext;


    public Completer<String> getNameCompleterForType ( String type ) {
        final List<ResourceLibrary> libraries = getResourceLibrariesForType(type);
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new LinkedList<>();
                for ( ResourceLibrary e : libraries ) {
                    String alias = e.getName();
                    if ( alias != null && !alias.isEmpty() && alias.startsWith(query) ) {
                        res.add(alias);
                    }
                }
                return res;
            }

        };
    }


    /**
     * @param type
     * @return
     */
    private List<ResourceLibrary> getResourceLibrariesForType ( String type ) {
        try {
            StructuralObject obj = this.viewContext.getSelectedObject();
            if ( obj != null ) {
                return this.tplCache.getResourceLibrariesWithType(obj, type);
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);

        }
        return Collections.EMPTY_LIST;
    }
}
