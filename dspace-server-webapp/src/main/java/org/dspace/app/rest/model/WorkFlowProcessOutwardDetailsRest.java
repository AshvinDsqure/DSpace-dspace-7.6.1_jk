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

import java.util.Date;

public class WorkFlowProcessOutwardDetailsRest extends  DSpaceObjectRest{
    public static final String NAME = "workflowprocessoutwarddetail";
    public static final String PLURAL_NAME = "workflowprocessoutwarddetails";
    public static final String CATEGORY = RestAddressableModel.WORKFLOWPROCESSOUTWARDDETAIL;

    public static final String GROUPS = "groups";

    private Integer legacyId;
    private String outwardNumber;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date outwardDate;
    private GroupRest outwardDepartmentRest;
    private WorkFlowProcessMasterValueRest outwardmediumRest = null;
    @JsonDeserialize(converter = MyDateConverter.class)
    private Date dispatchdate;

    private String  awbno;

    private String  serviceprovider;

    @JsonProperty
    private String  body;

    @JsonProperty
    private WorkFlowProcessMasterValueRest categoryRest = null;
    @JsonProperty
    private WorkFlowProcessMasterValueRest subcategoryRest = null;

    public Date getDispatchdate() {
        return dispatchdate;
    }
    public void setDispatchdate(Date dispatchdate) {
        this.dispatchdate = dispatchdate;
    }


    public String getAwbno() {
        return awbno;
    }

    public void setAwbno(String awbno) {
        this.awbno = awbno;
    }

    public String getServiceprovider() {
        return serviceprovider;
    }

    public void setServiceprovider(String serviceprovider) {
        this.serviceprovider = serviceprovider;
    }

    public String getOutwardNumber() {
        return outwardNumber;
    }

    public void setOutwardNumber(String outwardNumber) {
        this.outwardNumber = outwardNumber;
    }

    public Date getOutwardDate() {
        return outwardDate;
    }

    public void setOutwardDate(Date outwardDate) {
        this.outwardDate = outwardDate;
    }

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


    public GroupRest getOutwardDepartmentRest() {
        return outwardDepartmentRest;
    }

    public void setOutwardDepartmentRest(GroupRest outwardDepartmentRest) {
        this.outwardDepartmentRest = outwardDepartmentRest;
    }

    public WorkFlowProcessMasterValueRest getOutwardmediumRest() {
        return outwardmediumRest;
    }

    public void setOutwardmediumRest(WorkFlowProcessMasterValueRest outwardmediumRest) {
        this.outwardmediumRest = outwardmediumRest;
    }

    public WorkFlowProcessMasterValueRest getCategoryRest() {
        return categoryRest;
    }

    public void setCategoryRest(WorkFlowProcessMasterValueRest categoryRest) {
        this.categoryRest = categoryRest;
    }

    public WorkFlowProcessMasterValueRest getSubcategoryRest() {
        return subcategoryRest;
    }

    public void setSubcategoryRest(WorkFlowProcessMasterValueRest subcategoryRest) {
        this.subcategoryRest = subcategoryRest;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
