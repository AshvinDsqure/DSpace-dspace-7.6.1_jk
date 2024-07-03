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

public enum DmsObject {
    ITEM("Login",0),
    LOGIN("Login",1),
    bitstream("Bitstream",2),
    DOCEUMNTTYPE("Document Type",3);

    DmsObject(String type, int id) {
        this.action=type;
        this.id=id;
    }
    public static DmsObject find(Integer actionID) {
      return   EnumSet.allOf(DmsObject.class)
                .stream()
                .filter(e -> e.id.equals(actionID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", actionID)));
    }

    private String action;
    private Integer id;

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


}
