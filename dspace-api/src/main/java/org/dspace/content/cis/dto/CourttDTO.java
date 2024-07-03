/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dto;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author Robert Tansley
 */

public class CourttDTO{
    protected Integer court_no;
    protected String bench_desc;

    public CourttDTO(Integer court_no, String bench_desc) {
        this.court_no = court_no;
        this.bench_desc = bench_desc;
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
