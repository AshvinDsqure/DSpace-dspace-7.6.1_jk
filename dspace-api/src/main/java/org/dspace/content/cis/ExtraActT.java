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
import javax.print.attribute.EnumSyntax;
import java.sql.Timestamp;
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
//@Table(name = "extraact_t_a",schema = "public")
@Table(name = "extraact_t",schema = "public")
public class ExtraActT extends  DSpaceObjectCIS{

    @Id
    @Column(name="serialno",columnDefinition = "smallint",unique = true, nullable = false, insertable = true, updatable = false)
    protected Short serialno;
    @OneToOne(fetch = FetchType.LAZY,cascade = {CascadeType.ALL})
    @JoinColumn(name = "acts", referencedColumnName = "actcode")
    private Act acts = null;

    @Column(name="section",columnDefinition = "bpchar")
    protected String section;
    @ManyToOne
    @JoinColumn(name = "cino", referencedColumnName = "cino")
    private Civilt civilt;


    @Override
    public UUID getID() {
        return null;
    }



    public Act getActs() {
        return acts;
    }

    public void setActs(Act acts) {
        this.acts = acts;
    }

    public Civilt getCivilt() {
        return civilt;
    }

    public void setCivilt(Civilt civilt) {
        this.civilt = civilt;
    }

    public Short getSerialno() {
        return serialno;
    }

    public void setSerialno(Short serialno) {
        this.serialno = serialno;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
