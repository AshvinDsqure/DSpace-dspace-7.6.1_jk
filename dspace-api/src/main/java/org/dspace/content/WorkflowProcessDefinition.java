/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing an item in DSpace.
 * <P>
 * This class holds in memory the item Dublin Core metadata, the bundles in the
 * item, and the bitstreams in those bundles. When modifying the item, if you
 * modify the Dublin Core or the "in archive" flag, you must call
 * <code>update</code> for the changes to be written to the database.
 * Creating, adding or removing bundles or bitstreams has immediate effect in
 * the database.
 *
 * @author Robert Tansley
 * @author Martin Hald
 */
@Entity
@Table(name = "workflowprocessdefinition")
public class WorkflowProcessDefinition extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "workflowprocessdefinition_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "workflowprocessdefinitionname")
    private String workflowprocessdefinitionname;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "workflowProcessDefinition",cascade = { CascadeType.ALL})
    private Set<WorkflowProcessEperson> workflowProcessDefinitionEpeople=new HashSet<>();
    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "workflowprocessdefinition";
    }

    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }
    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }


    public String getWorkflowprocessdefinitionname() {
        return workflowprocessdefinitionname;
    }

    public void setWorkflowprocessdefinitionname(String workflowprocessdefinitionname) {
        this.workflowprocessdefinitionname = workflowprocessdefinitionname;
    }

    public Set<WorkflowProcessEperson> getWorkflowProcessDefinitionEpeople() {
        return workflowProcessDefinitionEpeople;
    }

    public void setWorkflowProcessDefinitionEpeople(Set<WorkflowProcessEperson> workflowProcessDefinitionEpeople) {
        this.workflowProcessDefinitionEpeople = workflowProcessDefinitionEpeople;
    }
}
