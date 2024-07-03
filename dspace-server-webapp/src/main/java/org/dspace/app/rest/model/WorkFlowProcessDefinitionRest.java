/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * The Item REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@LinksRest(links = {

})
public class WorkFlowProcessDefinitionRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocessdefinition";
    public static final String PLURAL_NAME = "workflowprocessdefinitions";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSDEFINITION;
    @JsonProperty
    private String workflowprocessdefinitionname;
    @JsonProperty
    List<WorkflowProcessEpersonRest> workflowProcessDefinitionEpersonRests=new ArrayList<>();
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

    public String getWorkflowprocessdefinitionname() {
        return workflowprocessdefinitionname;
    }

    public void setWorkflowprocessdefinitionname(String workflowprocessdefinitionname) {
        this.workflowprocessdefinitionname = workflowprocessdefinitionname;
    }

    public List<WorkflowProcessEpersonRest> getWorkflowProcessDefinitionEpersonRests() {
        return workflowProcessDefinitionEpersonRests;
    }

    public void setWorkflowProcessDefinitionEpersonRests(List<WorkflowProcessEpersonRest> workflowProcessDefinitionEpersonRests) {
        this.workflowProcessDefinitionEpersonRests = workflowProcessDefinitionEpersonRests;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
