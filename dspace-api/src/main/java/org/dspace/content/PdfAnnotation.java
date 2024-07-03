/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

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
@Table(name = "pdfAnnotation")
public class PdfAnnotation extends DSpaceObject implements DSpaceObjectLegacySupport {
    /**
     * Wild card for Dublin Core metadata qualifiers/languages
     */
    public static final String ANY = "*";
    @Column(name = "pdfAnnotation_id", insertable = false, updatable = false)
    private Integer legacyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bitstream_uuid")
    private Bitstream bitstream;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_uuid")
    private EPerson ePerson;
    @Column(name = "created_date", columnDefinition = "timestamp with time zone")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created_date;
    @Column(name = "annotation_str")
    private String annotationStr;

    @Column(name = "noteannotatiostr")
    private String noteannotatiostr;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private Item item;
    /**
     * Protected constructor, create object using:
     * {@link ItemService#create(Context, WorkspaceItem)}
     */
    protected PdfAnnotation() {

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
    protected PdfAnnotation(UUID uuid) {
        this.predefinedUUID = uuid;
    }

    @Override
    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public Bitstream getBitstream() {
        return bitstream;
    }

    public void setBitstream(Bitstream bitstream) {
        this.bitstream = bitstream;
    }

    public EPerson getePerson() {
        return ePerson;
    }

    public void setePerson(EPerson ePerson) {
        this.ePerson = ePerson;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public String getAnnotationStr() {
        return annotationStr;
    }

    public void setAnnotationStr(String annotationStr) {
        this.annotationStr = annotationStr;
    }

    public String getNoteannotatiostr() {
        return noteannotatiostr;
    }

    public void setNoteannotatiostr(String noteannotatiostr) {
        this.noteannotatiostr = noteannotatiostr;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
