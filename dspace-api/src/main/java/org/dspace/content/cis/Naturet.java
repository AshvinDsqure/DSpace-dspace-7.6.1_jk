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
@Table(name = "nature_t",schema = "public")
public class Naturet extends  DSpaceObjectCIS{
    @Id
    @Column(columnDefinition = "smallint",name="case_type_cd",length = 4,unique = true, nullable = false, insertable = true, updatable = false)
    protected Short case_type_cd;
    @Column(name="nature_cd",columnDefinition = "smallint")
    protected  Short nature_cd;
    @Column(name="nature_desc")
    protected  String nature_desc;

    @Override
    public UUID getID() {
        return null;
    }

    public Short getCase_type_cd() {
        return case_type_cd;
    }

    public void setCase_type_cd(Short case_type_cd) {
        this.case_type_cd = case_type_cd;
    }

    public Short getNature_cd() {
        return nature_cd;
    }

    public void setNature_cd(Short nature_cd) {
        this.nature_cd = nature_cd;
    }

    public String getNature_desc() {
        return nature_desc;
    }

    public void setNature_desc(String nature_desc) {
        this.nature_desc = nature_desc;
    }
}
