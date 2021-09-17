/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema;


import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;


/**
 * @author mbechler
 * 
 */
public class LSInputImpl implements LSInput {

    private String baseURI;
    private InputStream byteStream;
    private boolean certifiedText;
    private Reader characterStreamReader;
    private String encoding;
    private String publicId;
    private String stringData;
    private String systemId;


    /**
     * @param byteStream
     */
    public LSInputImpl ( InputStream byteStream ) {
        this.byteStream = byteStream;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getBaseURI()
     */
    @Override
    public String getBaseURI () {
        return this.baseURI;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getByteStream()
     */
    @Override
    public InputStream getByteStream () {
        return this.byteStream;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getCertifiedText()
     */
    @Override
    public boolean getCertifiedText () {
        return this.certifiedText;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getCharacterStream()
     */
    @Override
    public Reader getCharacterStream () {
        return this.characterStreamReader;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getEncoding()
     */
    @Override
    public String getEncoding () {
        return this.encoding;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getPublicId()
     */
    @Override
    public String getPublicId () {
        return this.publicId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getStringData()
     */
    @Override
    public String getStringData () {
        return this.stringData;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#getSystemId()
     */
    @Override
    public String getSystemId () {
        return this.systemId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setBaseURI(java.lang.String)
     */
    @Override
    public void setBaseURI ( String baseURI ) {
        this.baseURI = baseURI;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setByteStream(java.io.InputStream)
     */
    @Override
    public void setByteStream ( InputStream byteStream ) {
        this.byteStream = byteStream;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setCertifiedText(boolean)
     */
    @Override
    public void setCertifiedText ( boolean certifiedText ) {
        this.certifiedText = certifiedText;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setCharacterStream(java.io.Reader)
     */
    @Override
    public void setCharacterStream ( Reader characterStream ) {
        this.characterStreamReader = characterStream;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setEncoding(java.lang.String)
     */
    @Override
    public void setEncoding ( String encoding ) {
        this.encoding = encoding;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setPublicId(java.lang.String)
     */
    @Override
    public void setPublicId ( String publicId ) {
        this.publicId = publicId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setStringData(java.lang.String)
     */
    @Override
    public void setStringData ( String stringData ) {
        this.stringData = stringData;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.w3c.dom.ls.LSInput#setSystemId(java.lang.String)
     */
    @Override
    public void setSystemId ( String systemId ) {
        this.systemId = systemId;
    }

}
