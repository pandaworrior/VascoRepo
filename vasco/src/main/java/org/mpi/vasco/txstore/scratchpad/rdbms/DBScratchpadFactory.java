package org.mpi.vasco.txstore.scratchpad.rdbms;

interface DBScratchpadFactory extends org.mpi.vasco.txstore.scratchpad.ScratchpadFactory{

    // releases the scratchpad
    public void releaseScratchpad();
}