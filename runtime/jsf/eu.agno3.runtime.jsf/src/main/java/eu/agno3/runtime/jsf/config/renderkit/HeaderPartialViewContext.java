/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config.renderkit;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextWrapper;

import org.apache.myfaces.shared.util.StringUtils;


/**
 * @author mbechler
 *
 */
public class HeaderPartialViewContext extends PartialViewContextWrapper {

    private static final String SOURCE_HEADER = "X-JSF-Partial-Source"; //$NON-NLS-1$
    private static final String EXECUTE_HEADER = "X-JSF-Partial-Execute"; //$NON-NLS-1$
    private static final String RENDER_HEADER = "X-JSF-Partial-Render"; //$NON-NLS-1$

    private PartialViewContext wrapped;
    private Collection<String> executeClientIds;
    private Collection<String> renderClientIds;


    /**
     * @param wrapped
     */
    public HeaderPartialViewContext ( PartialViewContext wrapped ) {
        this.wrapped = wrapped;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.context.PartialViewContextWrapper#getExecuteIds()
     */
    @Override
    public Collection<String> getExecuteIds () {
        Collection<String> executeIds = super.getExecuteIds();

        if ( executeIds.isEmpty() ) {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            return getHeaderExecuteIds(ctx);
        }

        return executeIds;
    }


    /**
     * @param ctx
     * @return
     */
    private Collection<String> getHeaderExecuteIds ( ExternalContext ctx ) {
        if ( this.executeClientIds == null ) {
            String executeMode = ctx.getRequestHeaderMap().get(EXECUTE_HEADER);

            if ( executeMode != null && !executeMode.isEmpty() &&
            // !PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode) &&
                    !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(executeMode) ) {

                String[] clientIds = StringUtils.splitShortString(replaceTabOrEnterCharactersWithSpaces(executeMode), ' ');

                // The collection must be mutable
                List<String> tempList = new ArrayList<>();
                for ( String clientId : clientIds ) {
                    if ( clientId.length() > 0 ) {
                        tempList.add(clientId);
                    }
                }
                // The "javax.faces.source" parameter needs to be added to the list of
                // execute ids if missing (otherwise, we'd never execute an action associated
                // with, e.g., a button).

                String source = ctx.getRequestHeaderMap().get(SOURCE_HEADER);

                if ( source != null ) {
                    source = source.trim();

                    if ( !tempList.contains(source) ) {
                        tempList.add(source);
                    }
                }

                this.executeClientIds = tempList;
            }
            else {
                this.executeClientIds = new ArrayList<>();
            }
        }
        return this.executeClientIds;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.context.PartialViewContextWrapper#getRenderIds()
     */
    @Override
    public Collection<String> getRenderIds () {
        Collection<String> renderIds = super.getRenderIds();

        if ( renderIds.isEmpty() ) {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            return getHeaderRenderIds(ctx);
        }

        return renderIds;
    }


    /**
     * @param ctx
     * @return
     */
    private Collection<String> getHeaderRenderIds ( ExternalContext ctx ) {
        if ( this.renderClientIds == null ) {
            String renderMode = ctx.getRequestHeaderMap().get(RENDER_HEADER);

            if ( renderMode != null && !renderMode.isEmpty() &&
            // !PartialViewContext.NO_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode) &&
                    !PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode) ) {
                String[] clientIds = StringUtils.splitShortString(replaceTabOrEnterCharactersWithSpaces(renderMode), ' ');

                // The collection must be mutable
                List<String> tempList = new ArrayList<>();
                for ( String clientId : clientIds ) {
                    if ( clientId.length() > 0 ) {
                        tempList.add(clientId);
                    }
                }
                this.renderClientIds = tempList;
            }
            else {
                this.renderClientIds = new ArrayList<>();

                if ( PartialViewContext.ALL_PARTIAL_PHASE_CLIENT_IDS.equals(renderMode) ) {
                    this.renderClientIds.add(PartialResponseWriter.RENDER_ALL_MARKER);
                }
            }
        }
        return this.renderClientIds;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.context.PartialViewContextWrapper#getWrapped()
     */
    @Override
    public PartialViewContext getWrapped () {
        return this.wrapped;
    }


    private static String replaceTabOrEnterCharactersWithSpaces ( String mode ) {
        StringBuilder builder = new StringBuilder(mode.length());
        for ( int i = 0; i < mode.length(); i++ ) {
            if ( mode.charAt(i) == '\t' || mode.charAt(i) == '\n' ) {
                builder.append(' ');
            }
            else {
                builder.append(mode.charAt(i));
            }
        }
        return builder.toString();
    }
}
