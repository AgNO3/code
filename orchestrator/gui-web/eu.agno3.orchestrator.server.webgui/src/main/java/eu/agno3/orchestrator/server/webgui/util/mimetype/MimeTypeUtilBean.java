/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.mimetype;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Named;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;

import eu.agno3.orchestrator.server.webgui.util.completer.Completer;


/**
 * @author mbechler
 *
 */
@Named ( "mimeTypeUtilBean" )
public class MimeTypeUtilBean {

    static final SortedSet<String> MIME_TYPES = new TreeSet<>();

    static {

        for ( MediaType type : MediaTypeRegistry.getDefaultRegistry().getTypes() ) {
            MIME_TYPES.add(type.toString());
        }
    }


    public Comparator<String> getComparator () {
        return new MimeTypeComparator();
    }


    public Completer<String> getCompleter () {
        return new Completer<String>() {

            @Override
            public List<String> complete ( String query ) {
                List<String> res = new ArrayList<>();
                for ( String type : MimeTypeUtilBean.MIME_TYPES ) {
                    if ( type != null && type.contains(query) ) {
                        res.add(type);
                    }
                }
                return res;
            }
        };
    }

}
