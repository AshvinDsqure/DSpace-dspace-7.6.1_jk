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
@Table(name = "subnature1_t",schema = "public")
public class SubnatureOnet extends  DSpaceObjectCIS{

    @Column(name="case_type_cd",unique = true, nullable = false, insertable = true, updatable = false)
    protected Integer case_type_cd;
    @Id
    @Column(name="nature_cd",unique = true, nullable = false, insertable = true, updatable = false)
    protected Integer nature_cd;
    @Id
    @Column(name="subnature1_cd",unique = true, nullable = false, insertable = true, updatable = false)
    protected Integer subnature1_cd;
    @Column(name="subnature1_desc",columnDefinition = "bpchar")
    protected  String subnature1_desc;
    /*@OneToMany(mappedBy = "subnatureOnets")
    private Set<Civilt> entityBSet = new HashSet<>();*/
    @Override
    public UUID getID() {
        return null;
    }

    public Integer getCase_type_cd() {
        return case_type_cd;
    }

    public void setCase_type_cd(Integer case_type_cd) {
        this.case_type_cd = case_type_cd;
    }

    public Integer getNature_cd() {
        return nature_cd;
    }

    public void setNature_cd(Integer nature_cd) {
        this.nature_cd = nature_cd;
    }

    public Integer getSubnature1_cd() {
        return subnature1_cd;
    }

    public void setSubnature1_cd(Integer subnature1_cd) {
        this.subnature1_cd = subnature1_cd;
    }

    public String getSubnature1_desc() {
        return subnature1_desc;
    }

    public void setSubnature1_desc(String subnature1_desc) {
        this.subnature1_desc = subnature1_desc;
    }

    /*public Set<Civilt> getEntityBSet() {
        return entityBSet;
    }

    public void setEntityBSet(Set<Civilt> entityBSet) {
        this.entityBSet = entityBSet;
    }*/
}
