/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.model.helper.MyDateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkFlowProcessCommentRest extends DSpaceObjectRest {
    public static final String NAME = "workflowprocesscomment";
    public static final String PLURAL_NAME = "workflowprocesscomments";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSCOMMENT;
    public static final String GROUPS = "groups";
    private Integer legacyId;
    private String comment;
    private List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRest = new ArrayList<>();
    private WorkFlowProcessHistoryRest workFlowProcessHistoryRest;

    private EPersonRest submitterRest = null;
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date actionDate = null;

    @JsonProperty
    private WorkflowProcessReferenceDocRest noteRest;

    private WorkFlowProcessRest workflowProcessRest;
    private Boolean isdraftsave = false;


    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<WorkflowProcessReferenceDocRest> getWorkflowProcessReferenceDocRest() {
        return workflowProcessReferenceDocRest;
    }

    public void setWorkflowProcessReferenceDocRest(List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRest) {
        this.workflowProcessReferenceDocRest = workflowProcessReferenceDocRest;
    }

    public WorkFlowProcessHistoryRest getWorkFlowProcessHistoryRest() {
        return workFlowProcessHistoryRest;
    }

    public void setWorkFlowProcessHistoryRest(WorkFlowProcessHistoryRest workFlowProcessHistoryRest) {
        this.workFlowProcessHistoryRest = workFlowProcessHistoryRest;
    }

    public EPersonRest getSubmitterRest() {
        return submitterRest;
    }

    public void setSubmitterRest(EPersonRest submitterRest) {
        this.submitterRest = submitterRest;
    }

    public WorkFlowProcessRest getWorkflowProcessRest() {
        return workflowProcessRest;
    }

    public void setWorkflowProcessRest(WorkFlowProcessRest workflowProcessRest) {
        this.workflowProcessRest = workflowProcessRest;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public WorkflowProcessReferenceDocRest getNoteRest() {
        return noteRest;
    }

    public void setNoteRest(WorkflowProcessReferenceDocRest noteRest) {
        this.noteRest = noteRest;
    }

    public Boolean getIsdraftsave() {
        return isdraftsave;
    }

    public void setIsdraftsave(Boolean isdraftsave) {
        this.isdraftsave = isdraftsave;
    }
}
