/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import org.dspace.content.WorkFlowProcessMasterValue;

import java.util.ArrayList;
import java.util.List;

public class WorkFlowProcessMasterRest extends  DSpaceObjectRest{
    public static final String NAME = "workflowprocessmaster";
    public static final String PLURAL_NAME = "workflowprocessmasters";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSMASTER;

    public static final String GROUPS = "groups";

    private Integer legacyId;

    private String mastername;

    private List<WorkFlowProcessMasterValue> workFlowProcessMasterValues=new ArrayList<>();

    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public String getMastername() {
        return mastername;
    }

    public void setMastername(String mastername) {
        this.mastername = mastername;
    }

    public List<WorkFlowProcessMasterValue> getWorkFlowProcessMasterValues() {
        return workFlowProcessMasterValues;
    }

    public void setWorkFlowProcessMasterValues(List<WorkFlowProcessMasterValue> workFlowProcessMasterValues) {
        this.workFlowProcessMasterValues = workFlowProcessMasterValues;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getType() {
        return NAME;
    }
}
