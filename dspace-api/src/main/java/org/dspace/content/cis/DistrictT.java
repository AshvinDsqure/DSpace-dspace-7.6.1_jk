package org.dspace.content.cis;
/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
import org.dspace.content.DSpaceObjectCIS;


import javax.persistence.*;
import java.awt.*;
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
@Table(name = "district_t",schema = "public")
public class DistrictT {
    @Id
    @Column(name="dist_code",columnDefinition = "smallint",unique = true, nullable = false, insertable = true, updatable = false)
    protected Short dist_code;

    @Column(name ="dist_name",columnDefinition = "character varying(250) DEFAULT NULL")
    private String dist_name;

    @Column(name="state_id")
    protected Integer state_id;


    public Short getDist_code() {
        return dist_code;
    }

    public void setDist_code(Short dist_code) {
        this.dist_code = dist_code;
    }

    public String getDist_name() {
        return dist_name;
    }

    public void setDist_name(String dist_name) {
        this.dist_name = dist_name;
    }

    public Integer getState_id() {
        return state_id;
    }

    public void setState_id(Integer state_id) {
        this.state_id = state_id;
    }
}
