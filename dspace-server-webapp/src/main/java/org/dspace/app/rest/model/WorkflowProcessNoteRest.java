/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.dspace.app.rest.model.helper.MyDateConverter;
import org.dspace.eperson.EPerson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The rest resource used for workflow definitions
 *
 * @author Maria Verdonck (Atmire) on 11/12/2019
 */
@LinksRest(links = {

})
public class WorkflowProcessNoteRest extends DSpaceObjectRest{

    public static final String CATEGORY = "workflownote";
    public static final String NAME = "workflownote";
    public static final String NAME_PLURAL = "workflownots";

    public static final String COLLECTIONS_MAPPED_TO = "collections";
    public static final String STEPS = "steps";

    @JsonProperty
    private String subject;
    @JsonProperty
    private String description;

    @JsonProperty
    private String fullName;
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date initDate =null;
    @JsonProperty
    List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests=new ArrayList<>();
    private EPerson submitter = null;

    @JsonProperty
    WorkFlowProcessRest workflowProcessRest;
    @Override
    public String getCategory() {
        return CATEGORY;
    }
    @Override
    public String getType() {
        return NAME;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<WorkflowProcessReferenceDocRest> getWorkflowProcessReferenceDocRests() {
        return workflowProcessReferenceDocRests;
    }
    public void setWorkflowProcessReferenceDocRests(List<WorkflowProcessReferenceDocRest> workflowProcessReferenceDocRests) {
        this.workflowProcessReferenceDocRests = workflowProcessReferenceDocRests;
    }
    public EPerson getSubmitter() {
        return submitter;
    }
    public void setSubmitter(EPerson submitter) {
        this.submitter = submitter;
    }

    public WorkFlowProcessRest getWorkflowProcessRest() {
        return workflowProcessRest;
    }

    public void setWorkflowProcessRest(WorkFlowProcessRest workflowProcessRest) {
        this.workflowProcessRest = workflowProcessRest;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
