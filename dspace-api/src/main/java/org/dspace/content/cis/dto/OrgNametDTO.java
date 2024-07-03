/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dto;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author Robert Tansley
 */

public class OrgNametDTO{
    protected Short orgid;
    protected  String orgname;

    public OrgNametDTO(Short orgid, String orgname) {
        this.orgid = orgid;
        this.orgname = orgname;
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
