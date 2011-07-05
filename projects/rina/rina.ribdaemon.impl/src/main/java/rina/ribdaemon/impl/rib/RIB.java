package rina.ribdaemon.impl.rib;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import rina.ribdaemon.api.RIBDaemonException;

/**
 * Represents a Tree of RIBNodes. The Tree is represented as
 * a single rootElement which points to a List<RIBNode> of children. There is
 * no restriction on the number of children that a particular node may have.
 * This Tree provides a method to serialize the Tree into a List by doing a
 * pre-order traversal. It has several methods to allow easy updation of Nodes
 * in the Tree. When the RIB starts, it must populate its structure from an 
 * XML file containing the definition of the RIB (or optionally it can be 
 * hardcoded for the demo).
 */
public class RIB{
 
    private RIBNode rootElement = null;
     
    /**
     * Default ctor.
     */
    public RIB() {
        super();
        
        //TODO initialize the RIB (that is, create the structure without 
        //the handlers
        RIBNode rootElement = new RIBNode();
		rootElement.setObjectName("daf");
		this.setRootElement(rootElement);
    }
 
    /**
     * Return the root Node of the tree.
     * @return the root element.
     */
    public RIBNode getRootElement() {
        return this.rootElement;
    }
 
    /**
     * Set the root Element for the tree.
     * @param rootElement the root element to set.
     */
    public void setRootElement(RIBNode rootElement) {
        this.rootElement = rootElement;
    }
     
    /**
     * Returns the RIB as a List of RIBNode objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     * @return a List<RIBNode>.
     */
    public List<RIBNode> toList() {
        List<RIBNode> list = new ArrayList<RIBNode>();
        walk(rootElement, list);
        return list;
    }
     
    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     * @return the String representation of the Tree.
     */
    public String toString() {
        return toList().toString();
    }
     
    /**
     * Walks the Tree in pre-order style. This is a recursive method, and is
     * called from the toList() method with the root element as the first
     * argument. It appends to the second argument, which is passed by reference     * as it recurses down the tree.
     * @param element the starting element.
     * @param list the output of the walk.
     */
    private void walk(RIBNode element, List<RIBNode> list) {
        list.add(element);
        for (RIBNode data : element.getChildren()) {
            walk(data, list);
        }
    }
    
    /**
     * Finds a node in the RIB given the object instance id
     * @param objectInstance
     * @return
     */
    public RIBNode getRIBNode(long objectInstance){
    	return getRIBNode(rootElement, objectInstance);
    }
    
    private RIBNode getRIBNode(RIBNode element, long objectInstance){
    	RIBNode result = null;
    	
    	if (element.getObjectInstance() == objectInstance){
    		return element;
    	}
    	
    	for (RIBNode data : element.getChildren()) {
    		result = getRIBNode(data, objectInstance);
    		if (result != null){
    			return result;
    		}
        }
    	
    	return null;
    }
    
    /**
     * Given an objectname of the form "substring\0substring\0...substring" locate 
     * the RIBNode that corresponds to it
     * @param objectName
     * @return
     */
    public RIBNode getRIBNode(String objectName) throws RIBDaemonException{
    	StringTokenizer tokenizer = new StringTokenizer(objectName, "\0");
    	List<RIBNode> currentNodes = new ArrayList<RIBNode>();
    	currentNodes.add(rootElement);
    	RIBNode currentNode = null;
    	String currentToken = null;
    	boolean matches = false;
    	
    	while(tokenizer.hasMoreTokens()){
    		currentToken = tokenizer.nextToken();
    		for(int i=0; i<currentNodes.size(); i++){
    			currentNode = currentNodes.get(i);
    			if (currentNode.getObjectName().equals(currentToken)){
    				matches = true;
    				break;
    			}
    		}
    		
    		if (matches && tokenizer.hasMoreTokens()){
    			currentNodes = currentNode.getChildren();
    			if (currentNodes == null){
    				throw new RIBDaemonException(RIBDaemonException.OBJECTNAME_NOT_PRESENT_IN_THE_RIB);
    			}
    			matches = false;
    		}else if (matches){
    			return currentNode;
    		}else{
    			throw new RIBDaemonException(RIBDaemonException.OBJECTNAME_NOT_PRESENT_IN_THE_RIB);
    		}
    	}
    	
    	throw new RIBDaemonException(RIBDaemonException.OBJECTNAME_NOT_PRESENT_IN_THE_RIB);
    }
    
}
