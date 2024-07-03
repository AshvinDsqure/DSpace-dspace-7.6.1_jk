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
import org.dspace.app.rest.model.helper.MyDateConverterA;

import java.util.Date;

/**
 * The Item REST Resource
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@LinksRest(links = {

})
public class WorkflowProcessReferenceDocVersionRest extends DSpaceObjectRest {

    public static final String NAME = "workflowprocessreferencedocversion";
    public static final String PLURAL_NAME = "workflowprocessreferencedocversions";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSREFERENCEDOCVERSION;
    private EPersonRest creator = null;
    private WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest = null;
    @JsonProperty
    @JsonDeserialize(converter = MyDateConverterA.class)
    private Date creationdatetime = new Date();
    @JsonProperty
    private String remark;
    @JsonProperty
    private Double versionnumber;
    @JsonProperty
    private Boolean isactive;
    @JsonProperty
    private BitstreamRest bitstreamRest;
    @JsonProperty
    private String editortext;
    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }

    public EPersonRest getCreator() {
        return creator;
    }

    public void setCreator(EPersonRest creator) {
        this.creator = creator;
    }

    public WorkflowProcessReferenceDocRest getWorkflowProcessReferenceDocRest() {
        return workflowProcessReferenceDocRest;
    }

    public void setWorkflowProcessReferenceDocRest(WorkflowProcessReferenceDocRest workflowProcessReferenceDocRest) {
        this.workflowProcessReferenceDocRest = workflowProcessReferenceDocRest;
    }

    public Date getCreationdatetime() {
        return creationdatetime;
    }

    public void setCreationdatetime(Date creationdatetime) {
        this.creationdatetime = creationdatetime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getVersionnumber() {
        return versionnumber;
    }

    public BitstreamRest getBitstreamRest() {
        return bitstreamRest;
    }

    public void setBitstreamRest(BitstreamRest bitstreamRest) {
        this.bitstreamRest = bitstreamRest;
    }

    public void setVersionnumber(Double versionnumber) {
        this.versionnumber = versionnumber;
    }

    public Boolean getIsactive() {
        return isactive;
    }

    public void setIsactive(Boolean isactive) {
        this.isactive = isactive;
    }

    public String getEditortext() {
        return editortext;
    }

    public void setEditortext(String editortext) {
        this.editortext = editortext;
    }
}
