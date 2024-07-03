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
public class DspaceEventRest extends DSpaceObjectRest {
    public static final String NAME = "dspaceevent";
    public static final String PLURAL_NAME = "dspaceevent";
    public static final String CATEGORY = RestAddressableModel.DSPACEEVENT;
    private  ItemRest item;
    private  EPersonRest ePersonRest;
    private DocumentTypeTreeRest documenttypenameRest;
    private String action;
    private String description;
    private String title;
    private Date actionDate;
    private String dspaceObject;
    private  String caseType;
    private  String caseYear;
    private  String caseNumber;
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

    public ItemRest getItem() {
        return item;
    }

    public void setItem(ItemRest item) {
        this.item = item;
    }

    public EPersonRest getePersonRest() {
        return ePersonRest;
    }

    public void setePersonRest(EPersonRest ePersonRest) {
        this.ePersonRest = ePersonRest;
    }

    public DocumentTypeTreeRest getDocumenttypenameRest() {
        return documenttypenameRest;
    }

    public void setDocumenttypenameRest(DocumentTypeTreeRest documenttypenameRest) {
        this.documenttypenameRest = documenttypenameRest;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getDspaceObject() {
        return dspaceObject;
    }

    public void setDspaceObject(String dspaceObject) {
        this.dspaceObject = dspaceObject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public String getCaseYear() {
        return caseYear;
    }

    public void setCaseYear(String caseYear) {
        this.caseYear = caseYear;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }
}
