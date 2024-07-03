/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.jbpm.models;

import java.util.ArrayList;
import java.util.List;

public class JBPMResponse {

    private Integer count;
    private List<Message> message =new ArrayList<>();

    private String next_user;
    private String performed_by;

    private List<String> next_group;
    private String type;
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Message> getMessage() {
        return message;
    }

    public void setMessage(List<Message> message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNext_user() {
        return next_user;
    }

    public void setNext_user(String next_user) {
        this.next_user = next_user;
    }

    public String getPerformed_by() {
        return performed_by;
    }

    public void setPerformed_by(String performed_by) {
        this.performed_by = performed_by;
    }

    public List<String> getNext_group() {
        return next_group;
    }

    public void setNext_group(List<String> next_group) {
        this.next_group = next_group;
    }
}
