/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

@LinksRest(links = {

})
/**
 * This class' purpose is to create a container for the information used in the SearchResultsResource
 */
public class CisResultsRest extends  DSpaceObjectRest {
    public static final String NAME = "cisresult";
    public static final String PLURAL_NAME = "cisresult";
    public static final String CATEGORY = RestAddressableModel.CISRESULT;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }
    private  String jsonStr;

    public String getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
    }


}
