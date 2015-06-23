/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mpi.vasco.txstore.baseserver;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.Operation;

/**
 *
 * @author aclement
 */
public interface ServerApplication {
    
    public Result execute(Operation op);
    
}
