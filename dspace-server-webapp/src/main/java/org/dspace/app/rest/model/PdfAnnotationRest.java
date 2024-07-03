/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * The Item REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

public class PdfAnnotationRest extends DSpaceObjectRest {
    public static final String NAME = "pdfnnnotation";
    public static final String PLURAL_NAME = "pdfnnnotation";
    public static final String CATEGORY = RestAddressableModel.PDFANNOTATION;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String entityType = null;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }
    private  BitstreamRest bitstreamRest;
    private EPersonRest ePersonRest;
    private String pdfannotationStr;
    private Date created_date;

    private String noteannotatiostr;

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public BitstreamRest getBitstreamRest() {
        return bitstreamRest;
    }

    public void setBitstreamRest(BitstreamRest bitstreamRest) {
        this.bitstreamRest = bitstreamRest;
    }

    public EPersonRest getePersonRest() {
        return ePersonRest;
    }

    public void setePersonRest(EPersonRest ePersonRest) {
        this.ePersonRest = ePersonRest;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public String getPdfannotationStr() {
        return pdfannotationStr;
    }

    public void setPdfannotationStr(String pdfannotationStr) {
        this.pdfannotationStr = pdfannotationStr;
    }

    public String getNoteannotatiostr() {
        return noteannotatiostr;
    }

    public void setNoteannotatiostr(String noteannotatiostr) {
        this.noteannotatiostr = noteannotatiostr;
    }

}
