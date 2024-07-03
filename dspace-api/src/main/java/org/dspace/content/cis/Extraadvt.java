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
@Table(name = "extra_adv_t",schema = "public")
public class Extraadvt extends  DSpaceObjectCIS{
    @Id
    @Column(name="srno",unique = true, nullable = false, insertable = true, updatable = false)
    protected Integer srno;

    @Column(name="adv_name",columnDefinition = "smallint")
    protected String adv_name;

    @Column(name="type",columnDefinition = "smallint")
    protected Short type;
    @ManyToOne
    @JoinColumn(name = "cino", referencedColumnName = "cino")
    private Civilt civilt;
    @Override
    public UUID getID() {
        return null;
    }

    public Integer getSrno() {
        return srno;
    }

    public void setSrno(Integer srno) {
        this.srno = srno;
    }



    public String getAdv_name() {
        return adv_name;
    }

    public void setAdv_name(String adv_name) {
        this.adv_name = adv_name;
    }



    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public Civilt getCivilt() {
        return civilt;
    }

    public void setCivilt(Civilt civilt) {
        this.civilt = civilt;
    }
}
