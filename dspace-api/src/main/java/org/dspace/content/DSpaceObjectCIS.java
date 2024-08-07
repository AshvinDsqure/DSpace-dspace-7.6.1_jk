/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.core.ReloadableEntity;

import java.io.Serializable;
import java.util.UUID;

/**
 * Abstract base class for DSpace objects
 */

public abstract class DSpaceObjectCIS implements Serializable, ReloadableEntity<UUID> {

}
