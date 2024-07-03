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
import java.util.List;
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
@Table(name = "police_stn_t",schema = "public")
public class PoliceStnT extends  DSpaceObjectCIS{
    @Id
    @Column(name="police_st_code",unique = true, nullable = false, insertable = true, updatable = false)
    protected Integer police_st_code;
    @Column(name="police_st_name",columnDefinition = "bpchar")
    protected String police_st_name;

    @Column(name="dist_code",columnDefinition = "smallint")
    protected Short dist_code;

    @Column(name="state_id",columnDefinition = "smallint")
    protected Integer state_id;


    @Override
    public UUID getID() {
        return null;
    }

    public Integer getPolice_st_code() {
        return police_st_code;
    }

    public void setPolice_st_code(Integer police_st_code) {
        this.police_st_code = police_st_code;
    }

    public String getPolice_st_name() {
        return police_st_name;
    }

    public void setPolice_st_name(String police_st_name) {
        this.police_st_name = police_st_name;
    }

    public Short getDist_code() {
        return dist_code;
    }

    public void setDist_code(Short dist_code) {
        this.dist_code = dist_code;
    }

    public Integer getState_id() {
        return state_id;
    }

    public void setState_id(Integer state_id) {
        this.state_id = state_id;
    }
}
