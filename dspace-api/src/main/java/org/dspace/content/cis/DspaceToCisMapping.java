/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis;

import org.dspace.content.DSpaceObjectCIS;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author Robert Tansley
 */
@Entity
@Table(name = "jkdms_dspacecismapping",schema = "jkdms")
public class DspaceToCisMapping extends  DSpaceObjectCIS{


    @Id
    @Column(name="cino",columnDefinition = "bpchar")
    protected String cino;

    @Temporal(TemporalType.DATE)
    @Column(name="date")
    protected Date date;
    @Column(name="dspaceobjectid")
    protected  UUID dspaceobjectid;
    @Column(name="cisobjecttype")
    protected  Integer cisobjecttype;
    @Column(name="isrelationshipdone")
    protected  Boolean isrelationshipdone;

    @Override
    public UUID getID() {
        return null;
    }

    public String getCino() {
        return cino;
    }

    public void setCino(String cino) {
        this.cino = cino;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public UUID getDspaceobjectid() {
        return dspaceobjectid;
    }

    public void setDspaceobjectid(UUID dspaceobjectid) {
        this.dspaceobjectid = dspaceobjectid;
    }

    public Integer getCisobjecttype() {
        return cisobjecttype;
    }

    public void setCisobjecttype(Integer cisobjecttype) {
        this.cisobjecttype = cisobjecttype;
    }

    public Boolean getIsrelationshipdone() {
        return isrelationshipdone;
    }

    public void setIsrelationshipdone(Boolean isrelationshipdone) {
        this.isrelationshipdone = isrelationshipdone;
    }
}
