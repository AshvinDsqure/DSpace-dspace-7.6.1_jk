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
@Table(name = "subject_master",schema = "public")
public class SubjectMaster extends  DSpaceObjectCIS{
    @Id
    @Column(columnDefinition = "bpchar",name="subject_code")
    protected Integer subject_code;
    @Column(name="subject_name",columnDefinition = "bpchar")
    protected  String subject_name;
    @Override
    public UUID getID() {
        return null;
    }


    public String getSubject_name() {
        return subject_name;
    }

    public void setSubject_name(String subject_name) {
        this.subject_name = subject_name;
    }

    public Integer getSubject_code() {
        return subject_code;
    }

    public void setSubject_code(Integer subject_code) {
        this.subject_code = subject_code;
    }
}
