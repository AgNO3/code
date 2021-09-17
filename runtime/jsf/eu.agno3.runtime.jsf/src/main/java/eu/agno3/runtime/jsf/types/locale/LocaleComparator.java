/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.types.locale;


import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;


/**
 * @author mbechler
 *
 */
public class LocaleComparator implements Comparator<Locale> {

    private final Locale userLocale;
    private final Collator collator;


    /**
     * @param userLocale
     */
    public LocaleComparator ( Locale userLocale ) {
        this.userLocale = userLocale;
        this.collator = Collator.getInstance(userLocale);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( Locale l1, Locale l2 ) {
        return this.collator.compare(l1.getDisplayName(this.userLocale), l2.getDisplayName(this.userLocale));
    }

}
