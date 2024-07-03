/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.content.comparator.NameAscendingComparator;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.hibernate.proxy.HibernateProxyHelper;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.*;

/**
 * Class representing an item in DSpace.
 * <P>
 * This class holds in memory the item Dublin Core metadata, the bundles in the
 * item, and the bitstreams in those bundles. When modifying the item, if you
 * modify the Dublin Core or the "in archive" flag, you must call
 * <code>update</code> for the changes to be written to the database.
 * Creating, adding or removing bundles or bitstreams has immediate effect in
 * the database.
 *
 * @author Robert Tansley
 * @author Martin Hald
 */
@Entity
@Table(name = "documenttype")
public class DocumentType extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "doctype_id", insertable = false, updatable = false)
    private Integer legacyId;
    @Column(name = "documenttypename")
    private String documenttypename;

    /**
     * Protected constructor, create object using:
     * {@link ItemService#create(Context, WorkspaceItem)}
     */
    protected DocumentType() {

    }

    @Override
    public int getType() {
        return Constants.DOCTYPE;
    }

    @Override
    public String getName() {
        return "";
    }

    /**
     * Takes a pre-determined UUID to be passed to the object to allow for the
     * restoration of previously defined UUID's.
     *
     * @param uuid Takes a uuid to be passed to the Pre-Defined UUID Generator
     */
    protected DocumentType(UUID uuid) {
        this.predefinedUUID = uuid;
    }

    @Override
    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public String getDocumenttypename() {
        return documenttypename;
    }

    public void setDocumenttypename(String documenttypename) {
        this.documenttypename = documenttypename;
    }
}
