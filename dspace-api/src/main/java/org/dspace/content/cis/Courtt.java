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
@Table(name = "court_t",schema = "public")
public class Courtt extends  DSpaceObjectCIS{
    @Id
    @Column(name="court_no",unique = true, nullable = false, insertable = true, updatable = false)
    protected Integer court_no;
    @Column(name="bench_desc",columnDefinition = "smallint")
    protected String bench_desc;

    @Override
    public UUID getID() {
        return null;
    }

    public Integer getCourt_no() {
        return court_no;
    }

    public void setCourt_no(Integer court_no) {
        this.court_no = court_no;
    }

    public String getBench_desc() {
        return bench_desc;
    }

    public void setBench_desc(String bench_desc) {
        this.bench_desc = bench_desc;
    }


}
