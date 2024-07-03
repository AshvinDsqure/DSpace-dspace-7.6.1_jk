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
@Table(name = "judge_name_t",schema = "public")
public class JudgeNamet extends  DSpaceObjectCIS{
    @Id
    @Column(columnDefinition = "smallint",name="judge_code",length = 4,unique = true, nullable = false, insertable = true, updatable = false)
    protected Short judge_code;

    @Column(name="judge_name",columnDefinition = "bpchar")
    protected  String judge_name;

    @Column(name="jocode",columnDefinition = "bpchar")
    protected  String jocode;

    @Override
    public UUID getID() {
        return null;
    }

    public Short getJudge_code() {
        return judge_code;
    }

    public void setJudge_code(Short judge_code) {
        this.judge_code = judge_code;
    }

    public String getJudge_name() {
        return judge_name;
    }

    public void setJudge_name(String judge_name) {
        this.judge_name = judge_name;
    }

    public String getJocode() {
        return jocode;
    }

    public void setJocode(String jocode) {
        this.jocode = jocode;
    }
}
