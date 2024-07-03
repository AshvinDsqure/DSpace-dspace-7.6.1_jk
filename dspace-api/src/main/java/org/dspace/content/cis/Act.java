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
@Table(name = "act_t",schema = "public")
public class Act {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actcode",columnDefinition = "bigint NOT NULL DEFAULT 0")
    private Long actcode;

    @Column(name ="actname",columnDefinition = "character varying(250) DEFAULT NULL")
    private String actname;

    public Long getActcode() {
        return actcode;
    }

    public void setActcode(Long actcode) {
        this.actcode = actcode;
    }

    public String getActname() {
        return actname;
    }

    public void setActname(String actname) {
        this.actname = actname;
    }
}
