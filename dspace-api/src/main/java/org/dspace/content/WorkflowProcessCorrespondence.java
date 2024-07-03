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
import java.util.Date;

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
@Table(name = "workflowprocesscorrespondence")
public class WorkflowProcessCorrespondence extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";

    @Column(name = "workflowprocesscorrespondence_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "diarynumber")
    private String name;
    @Column(name = "officelocation")
    private String officeLocation;
    @Column(name = "diarydate", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date diarydate = new Date();
    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "workflow";
    }
    @Override
    public Integer getLegacyId() {
        return this.legacyId;
    }
    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public Date getDiarydate() {
        return diarydate;
    }

    public void setDiarydate(Date diarydate) {
        this.diarydate = diarydate;
    }
}
