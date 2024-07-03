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
@LinksRest(links = {

})
public class DocumentTypeRest extends DSpaceObjectRest {
    public static final String NAME = "documenttype";
    public static final String PLURAL_NAME = "documenttype";
    public static final String CATEGORY = RestAddressableModel.DOCUMENTTYPE;
    private  String documenttypename;
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

    public String getDocumenttypename() {
        return documenttypename;
    }

    public void setDocumenttypename(String documenttypename) {
        this.documenttypename = documenttypename;
    }
}
