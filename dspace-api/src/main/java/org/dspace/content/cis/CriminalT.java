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
//@Table(name = "criminal_t_a",schema = "public")
@Table(name = "criminal_t",schema = "public")
public class CriminalT extends  DSpaceObjectCIS{
    @Id
    @Column(columnDefinition = "bpchar",name="cino",length = 4,unique = true, nullable = false, insertable = true, updatable = false)
    protected String cino;
    @Column(name="fir_no" , columnDefinition = "bpchar")
    protected String fir_no;
    @Column(name="fir_year",columnDefinition = "smallint")
    protected Short fir_year;
    @Column(name="police_dist_code",columnDefinition = "smallint")
    protected Short police_dist_code;

    @Column(name="police_state_id",columnDefinition = "smallint")
    protected Short police_state_id;

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

    public String getFir_no() {
        return fir_no;
    }

    public void setFir_no(String fir_no) {
        this.fir_no = fir_no;
    }

    public Short getFir_year() {
        return fir_year;
    }

    public void setFir_year(Short fir_year) {
        this.fir_year = fir_year;
    }

    public Short getPolice_dist_code() {
        return police_dist_code;
    }


    public void setPolice_dist_code(Short police_dist_code) {
        this.police_dist_code = police_dist_code;
    }

    public Short getPolice_state_id() {
        return police_state_id;
    }

    public void setPolice_state_id(Short police_state_id) {
        this.police_state_id = police_state_id;
    }
}
