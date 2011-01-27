package rina.flowallocator.api;

/**
* Interface between FAI and FA
* @author elenitrouva
*
*/
public interface ReleaseResources {

/**
* Called by a FAI to forward de-allocate requests to the FA
* @param portId
*/
public void forwardDeAllocateRequest(int portId) ;

}

