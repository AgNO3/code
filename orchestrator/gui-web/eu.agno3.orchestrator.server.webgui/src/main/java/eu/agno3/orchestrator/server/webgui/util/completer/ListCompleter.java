/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.completer;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public class ListCompleter implements Completer<String> {

    private final List<String> options;


    /**
     * @param options
     * 
     */
    public ListCompleter ( String... options ) {
        this.options = new ArrayList<>(Arrays.asList(options));
        Collections.sort(this.options, Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.util.completer.Completer#complete(java.lang.String)
     */
    @Override
    public List<String> complete ( String query ) {
        List<String> res = new ArrayList<>();

        if ( StringUtils.isBlank(query) ) {
            res.addAll(this.options);
        }
        else {
            for ( String option : this.options ) {
                if ( option.startsWith(query) ) {
                    res.add(query);
                }
            }
        }
        return res;
    }

}
