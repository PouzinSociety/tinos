package rina.flowallocator.api;

import java.util.HashMap;
import java.util.Map;
 
/**
* Flow object definition
* @author elenitrouva
*
*/
public class FlowObject {

Map<String, Object> flow = new HashMap<String, Object>();

// Source_Naming_Info:
// Application-Process-Name: String
public static final String srcApplicationProcessName = "source Application Process Name";
// API-identifier: Integer Optional
public static final String srcApplicationProcessInstance_id = "source API-identifier";
// AE-identifier: String Optional
public static final String srcAE_id = "source AE-identifier";
// AEI-id: Integer Optional
public static final String srcAEInstance_id = "source AEI-id";

// Destination_Naming_Info:
// Application-Process-Name: String
public static final String dstApplicationProcessName = "destination Application Process Name";
// API-identifier: Integer Optional
public static final String dstApplicationProcessInstance_id = "destination API-identifier";
// AE-identifier: String Optional
public static final String dstAE_id = "destination AE-identifier";
// AEI-id: Integer Optional
public static final String dstAEInstance_id = "destination AEI-id";

// Source-Port-id: Port-id-length
public static final String srcPort_id = "Source-Port-id";
// Destination-Port-id: Port-id-length
public static final String dstPort_id = "Destination-Port-id";
// Destination-Address: Addr-Length
public static final String dstAddr = "Destination-Address";
// Source-Address: Addr-Length
public static final String srcAddr = "Source-Address";
// CurrentFlow-id: index
public static final String currentFlow_id = "CurrentFlow-id";
// Flow-id: Struct Array[ ];
// QoS-id: QoS-idLength
public static final String qos_id = "QoS-id";
// Destination-CEP-id: Port-id-Length
public static final String dstCEP_id = "Destination-CEP-id";
// Source-CEP-id: Port-id-length
public static final String srcCEP_id = "Source-CEP-id";
// State: 8 bits
public static final String state = "state";
// QoS Params: List
public static final String qosParams = "QoS parameters";
// Policies: QoS-Set-id
public static final String qosSet_id = "QoS-Set-id";
// Access Control: Capability
public static final String access = "access control";
// MaxCreateFlowRetries: Integer /* Maximum number of retries to create the flow
// before giving up. */
public static final String maxCreateFlowRetries = "Maximum CreateFlow Retries";
// CreateFlowRetries: Integer /* Current number of retries. */
public static final String CreateFlowRetries = "Current number of CreateFlow Retries";
// Hop Count: Byte Intger /*While the search rules that generate the forwarding table
// should allow for a natural termination condition, it seems wise to have the means to enforce termination. */
public static final String hopCount = "hop count";



public FlowObject() {
flow.put(hopCount, null);

flow.put(srcApplicationProcessName, null);
flow.put(srcApplicationProcessInstance_id, null);
flow.put(srcAE_id, null);
flow.put(srcAEInstance_id, null);

flow.put(dstApplicationProcessName, null);
flow.put(dstApplicationProcessInstance_id, null);
flow.put(dstAE_id, null);
flow.put(dstAEInstance_id, null);

flow.put(srcPort_id, null);
flow.put(dstPort_id, null);
flow.put(srcAddr, null);
flow.put(dstAddr, null);
flow.put(currentFlow_id, null);

flow.put(qos_id, null);
flow.put(srcCEP_id, null);
flow.put(dstCEP_id, null);
flow.put(state, null);
flow.put(qosParams, null);
flow.put(qosSet_id, null);
flow.put(access, null);
flow.put(maxCreateFlowRetries, null);
flow.put(CreateFlowRetries, null);

}

}