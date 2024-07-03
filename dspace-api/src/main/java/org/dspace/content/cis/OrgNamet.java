/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis;

import org.dspace.content.DSpaceObjectCIS;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "orgname_t",schema = "public")
public class OrgNamet extends  DSpaceObjectCIS{
    @Id
    @Column(columnDefinition = "smallint",name="orgid",length = 4,unique = true, nullable = false, insertable = true, updatable = false)
    protected Short orgid;

    @Column(name="orgname",columnDefinition = "bpchar")
    protected  String orgname;



    @Override
    public UUID getID() {
        return null;
    }

    public Short getOrgid() {
        return orgid;
    }

    public void setOrgid(Short orgid) {
        this.orgid = orgid;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }



}
