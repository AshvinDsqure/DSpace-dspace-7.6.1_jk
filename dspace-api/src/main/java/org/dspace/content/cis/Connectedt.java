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
@Table(name = "connected_t",schema = "public")
public class Connectedt extends  DSpaceObjectCIS{
    @Id
    @Column(columnDefinition = "bpchar",name="cino",length = 4,unique = true, nullable = false, insertable = true, updatable = false)
    protected String cino;
    @Id
    @Column(columnDefinition = "bpchar",name="linkcino",length = 4,unique = true, nullable = false, insertable = true, updatable = false)
    protected String linkcino;
    @Column(name = "disposed")
    @Enumerated(EnumType.STRING) // Specify the enumeration type
    private Status disposed;
    @ManyToOne
    @JoinColumn(name = "cino", referencedColumnName = "cino")
    private Civilt civilt;
    @Override
    public UUID getID() {
        return null;
    }

    public String getCino() {
        return cino;
    }

    public void setCino(String cino) {
        this.cino = cino;
    }

    public String getLinkcino() {
        return linkcino;
    }

    public void setLinkcino(String linkcino) {
        this.linkcino = linkcino;
    }

    public Status getDisposed() {
        return disposed;
    }

    public void setDisposed(Status disposed) {
        this.disposed = disposed;
    }

    public Civilt getCivilt() {
        return civilt;
    }

    public void setCivilt(Civilt civilt) {
        this.civilt = civilt;
    }
}
