/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dto;

import org.dspace.content.cis.Status;

/**
 * Class representing bitstreams stored in the DSpace system.
 * <P>
 * When modifying the bitstream metadata, changes are not reflected in the
 * database until <code>update</code> is called. Note that you cannot alter
 * the contents of a bitstream; you need to create a new bitstream.
 *
 * @author Robert Tansley
 */

public class ConnectedtDTO{
    protected String cino;
    protected String linkcino;

    private Status disposed;
    private CiviltDTO civilt;

    public ConnectedtDTO(String cino, String linkcino, Status disposed, CiviltDTO civilt) {
        this.cino = cino;
        this.linkcino = linkcino;
        this.disposed = disposed;
        this.civilt = civilt;
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

    public CiviltDTO getCivilt() {
        return civilt;
    }

    public void setCivilt(CiviltDTO civilt) {
        this.civilt = civilt;
    }
}
