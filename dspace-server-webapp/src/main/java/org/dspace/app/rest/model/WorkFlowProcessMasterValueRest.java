/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import org.dspace.content.WorkFlowProcessMaster;

public class WorkFlowProcessMasterValueRest extends  DSpaceObjectRest{
    public static final String NAME = "workflowprocessmastervalue";
    public static final String PLURAL_NAME = "workflowprocessmastervalues";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSMASTERVALUE;

    public static final String GROUPS = "groups";

    private Integer legacyId;
    private WorkFlowProcessMaster workFlowProcessMaster;
    private String primaryvalue;
    private String secondaryvalue;


    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public WorkFlowProcessMaster getWorkFlowProcessMaster() {
        return workFlowProcessMaster;
    }

    public void setWorkFlowProcessMaster(WorkFlowProcessMaster workFlowProcessMaster) {
        this.workFlowProcessMaster = workFlowProcessMaster;
    }

    public String getPrimaryvalue() {
        return primaryvalue;
    }

    public void setPrimaryvalue(String primaryvalue) {
        this.primaryvalue = primaryvalue;
    }

    public String getSecondaryvalue() {
        return secondaryvalue;
    }

    public void setSecondaryvalue(String secondaryvalue) {
        this.secondaryvalue = secondaryvalue;
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
