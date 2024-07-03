package org.dspace.content.cis;
/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
import javax.persistence.*;
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
//@Table(name = "civ_address_t_a",schema = "public")
@Table(name = "civ_address_t",schema = "public")
public class Civaddresst {
    @Id
    @Column(name="party_no",columnDefinition = "smallint",unique = true, nullable = false, insertable = true, updatable = false)
    protected Short party_no;

    @Column(name ="name",columnDefinition = "character varying(250) DEFAULT NULL")
    private String name;

    @Column(name="type",columnDefinition = "smallint")
    protected Short type=0;

    @ManyToOne
    @JoinColumn(name = "cino", referencedColumnName = "cino")
    private Civilt civilt;

    public Short getParty_no() {
        return party_no;
    }

    public void setParty_no(Short party_no) {
        this.party_no = party_no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Civilt getCivilt() {
        return civilt;
    }

    public void setCivilt(Civilt civilt) {
        this.civilt = civilt;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }
}
