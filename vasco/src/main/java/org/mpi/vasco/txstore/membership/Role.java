/*
import replicationlayer.core.util.Debug;
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mpi.vasco.txstore.membership;

/**
 *
 * @author chengli
 */
public enum Role {
    PROXY, COORDINATOR, REMOTECOORDINATOR, STORAGE;
	// the remaining are more "application" side
	// they should not be included
        //USER, APPPROXY;
}
