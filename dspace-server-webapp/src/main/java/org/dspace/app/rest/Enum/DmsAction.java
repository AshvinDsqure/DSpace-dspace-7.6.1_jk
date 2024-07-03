/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.Enum;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DmsEvent;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.Item;
import org.dspace.content.service.DmsEventService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumSet;

public enum DmsAction {
    LOGIN("Login",0),
    VIEWDOC("View Document",1),
    VIEWMEAREDOC("View Mearge Document",2),
    DOWNLOAD("Download",3),
    EDIT("Edit",4),
    DELETE("Delete",5),
    MERGEPDF("Merge PDF",6);

    DmsAction(String action,int id) {
        this.action=action;
        this.actionID=id;
    }
    public static DmsAction find(Integer actionID) {
      return   EnumSet.allOf(DmsAction.class)
                .stream()
                .filter(e -> e.actionID.equals(actionID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", actionID)));
    }
    private DmsEventService dmsEventService;
    private String action;
    private Integer id;
    private Integer actionID;
    private EPerson ePerson;
    private DocumentTypeTree documentTypeTree;
    private Item item;
    private String description;
    private String title;
    private  DmsObject dsDmsObject;
    @Component
    public static class ServiceInjector {
        @Autowired
        private DmsEventService dmsEventService;
        @PostConstruct
        public void postConstruct() {
            for (DmsAction rt : EnumSet.allOf(DmsAction.class)) {
                rt.setDmsEventService(dmsEventService);
            }
        }

        public DmsEventService getDmsEventService() {
            return dmsEventService;
        }

        public void setDmsEventService(DmsEventService dmsEventService) {
            this.dmsEventService = dmsEventService;
        }
    }

    public DmsEventService getDmsEventService() {
        return dmsEventService;
    }
    public DmsAction StoreDmsAction(Context context) throws SQLException, AuthorizeException {
        DmsEvent dmsEvent= dmsEventService.create(context);
        dmsEvent.setAction(this.getActionID());
        dmsEvent.setActionDate(new Date());
        dmsEvent.setItem(this.getItem());
        dmsEvent.setTitle(this.getTitle());
        dmsEvent.setePerson(this.getePerson());
        dmsEvent.setDocumentTypeTree(this.getDocumentTypeTree());
        if(ePerson != null) {
            dmsEventService.create(context, dmsEvent);
        }
        if(this.getDsDmsObject() != null) {
            dmsEvent.setObjectType(this.getDsDmsObject().getId());
        }
        return  null;

    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getActionID() {
        return actionID;
    }

    public void setActionID(Integer actionID) {
        this.actionID = actionID;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public EPerson getePerson() {
        return ePerson;
    }

    public void setePerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }

    public DocumentTypeTree getDocumentTypeTree() {
        return documentTypeTree;
    }

    public void setDocumentTypeTree(DocumentTypeTree documentTypeTree) {
        this.documentTypeTree = documentTypeTree;
    }

    public void setDmsEventService(DmsEventService dmsEventService) {
        this.dmsEventService = dmsEventService;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DmsObject getDsDmsObject() {
        return dsDmsObject;
    }

    public void setDsDmsObject(DmsObject dsDmsObject) {
        this.dsDmsObject = dsDmsObject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(item != null){
            this.title=item.caseDetail();
        }else {
            this.title = title;
        }
    }
}
