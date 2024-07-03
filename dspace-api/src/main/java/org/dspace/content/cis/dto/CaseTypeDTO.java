/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dto;

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

public class CaseTypeDTO{
    protected Short case_type;
    protected String type_name;

    public CaseTypeDTO(Short case_type, String type_name) {
        this.case_type = case_type;
        this.type_name = type_name;
    }

    public UUID getID() {
        return null;
    }

    public Short getCase_type() {
        return case_type;
    }

    public void setCase_type(Short case_type) {
        this.case_type = case_type;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

}
