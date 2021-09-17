/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;
import eu.agno3.runtime.util.matching.PatternUtil;


/**
 * @author mbechler
 *
 */
public class SearchResultTreeNode extends AbstractBrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 4608524262419577886L;
    private static final int MIN_QUERY_LENGTH = 3;
    private String query;
    private boolean haveMoreResults;
    private int limit;
    private int offset;
    private boolean pagingAllowed;
    private int searchResults;


    /**
     * 
     */
    public SearchResultTreeNode () {}


    /**
     * @param query
     * @param treeProvder
     */
    public SearchResultTreeNode ( String query, BrowseTreeProvider treeProvder ) {
        super(treeProvder);
        this.query = query;
    }


    /**
     * @return the query
     */
    public String getQuery () {
        return this.query;
    }


    /**
     * @return the limit
     */
    public int getLimit () {
        return this.limit;
    }


    /**
     * 
     * @return the number of search results
     */
    public int getNumSearchResults () {
        return this.searchResults;
    }


    /**
     * @param limit
     *            the limit to set
     */
    public void setLimit ( int limit ) {
        this.limit = limit;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#hasPermission(eu.agno3.fileshare.model.GrantPermission)
     */
    @Override
    public boolean hasPermission ( GrantPermission perm ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-search"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.SEARCH_RESULT_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {

        String q = this.getQuery();
        if ( StringUtils.isBlank(q) || getEffectiveQueryLength() < MIN_QUERY_LENGTH ) {
            this.searchResults = 0;
            return Arrays.asList(new InvalidQueryTreeNode());
        }

        q = PatternUtil.makeSubstringQuery(q);

        List<BrowseTreeNode> res = this.getTreeProvider().getSearchChildren(this, q);

        if ( res.isEmpty() ) {
            this.searchResults = 0;
            res.add(new EmptySearchResultTreeNode());
        }
        else {
            this.searchResults = res.size();
        }
        return res;
    }


    private int getEffectiveQueryLength () {
        return this.getQuery().length() - StringUtils.countMatches(this.getQuery(), '*') - StringUtils.countMatches(this.getQuery(), '?');
    }


    /**
     * @return the haveMoreResults
     */
    public boolean getHaveMoreResults () {
        return this.haveMoreResults;
    }


    /**
     * @param y
     */
    public void setHaveMoreResults ( boolean y ) {
        this.haveMoreResults = y;
    }


    /**
     * @return the pagingAllowed
     */
    public boolean getPagingAllowed () {
        return this.pagingAllowed;
    }


    /**
     * @param pagingAllowed
     *            the pagingAllowed to set
     */
    public void setPagingAllowed ( boolean pagingAllowed ) {
        this.pagingAllowed = pagingAllowed;
    }


    /**
     * @return the display offset
     */
    public int getOffset () {
        return this.offset;
    }


    /**
     * @param offset
     *            the offset to set
     */
    public void setOffset ( int offset ) {
        this.offset = offset;
    }
}
