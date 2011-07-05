package rina.enrollment.api;

import rina.ipcprocess.api.IPCProcessComponent;
import rina.ribdaemon.api.RIBHandler;

/**
 * The enrollment task manages the members of the DIF. It implements the state machines that are used 
 * to join a DIF or to collaboarate with a remote IPC Process to allow him to join the DIF.
 * @author eduardgrasa
 *
 */
public interface EnrollmentTask extends IPCProcessComponent, RIBHandler{

}
