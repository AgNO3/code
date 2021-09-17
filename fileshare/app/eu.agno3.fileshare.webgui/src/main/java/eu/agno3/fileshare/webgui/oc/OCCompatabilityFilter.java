/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.oc;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.PathMatchingFilter;


/**
 * @author mbechler
 *
 */
public class OCCompatabilityFilter extends PathMatchingFilter {

    private static final String OC_VERSION = "8.0.4"; //$NON-NLS-1$
    private static final String OC_VERSION_STRING = OC_VERSION;
    private static final String OC_EDITION_STRING = StringUtils.EMPTY;


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.web.servlet.AdviceFilter#doFilterInternal(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws ServletException, IOException {
        try ( PrintWriter out = resp.getWriter() ) {
            out.write("{\"installed\":\"true\","); //$NON-NLS-1$
            out.write("\"version\":\""); //$NON-NLS-1$
            out.write(OC_VERSION);
            out.write("\","); //$NON-NLS-1$
            out.write("\"versionstring\":\""); //$NON-NLS-1$
            out.write(OC_VERSION_STRING);
            out.write("\","); //$NON-NLS-1$
            out.write("\"edition\":\""); //$NON-NLS-1$
            out.write(OC_EDITION_STRING);
            out.write("\"}"); //$NON-NLS-1$
        }
    }
}
