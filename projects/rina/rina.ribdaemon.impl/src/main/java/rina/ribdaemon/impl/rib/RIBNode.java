package rina.ribdaemon.impl.rib;

import java.util.ArrayList;
import java.util.List;

import rina.ribdaemon.api.RIBHandler;

/**
 * Represents a node of the RIB class. The RIBNode is also a container, and
 * can be thought of as instrumentation to determine the location of a given object
 * in the RIB.
 */
public class RIBNode{
	
	/**
	 * The entity that manages the information associated to this RIB node
	 */
    private RIBHandler ribHandler = null;
    
    /**
     * The list of children of this node
     */
    private List<RIBNode> children = null;
    
    /**
     * The class of the object
     */
    private String objectClass = null;
    
    /**
     * The instance id of the stored object
     */
    private long objectInstance = -1;
    
    /**
     * The name of the stored object
     */
    private String objectName = null;
 
    /**
     * Default ctor.
     */
    public RIBNode() {
        super();
    }
 
    /**
     * Convenience ctor to create a RIBNode with an instance of data.
     * @param data an instance of an object.
     */
    public RIBNode(String objectClass, String objectName, long objectInstance) {
        this();
        setObjectClass(objectClass);
        setObjectInstance(objectInstance);
        setObjectName(objectName);
    }
     
    /**
     * Return the children of RIBNode. The RIB is represented by a single
     * root RIBNode whose children are represented by a List<RIBNode>. Each of
     * these RIBNode elements in the List can have children. The getChildren()
     * method will return the children of a RIBNode.
     * @return the children of RIBNode
     */
    public List<RIBNode> getChildren() {
        if (this.children == null) {
            return new ArrayList<RIBNode>();
        }
        return this.children;
    }
 
    /**
     * Sets the children of a RIBNode object. See docs for getChildren() for
     * more information.
     * @param children the List<RIBNode> to set.
     */
    public void setChildren(List<RIBNode> children) {
        this.children = children;
    }
 
    /**
     * Returns the number of immediate children of this RIBNode.
     * @return the number of immediate children.
     */
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }
     
    /**
     * Adds a child to the list of children for this RIBNode. The addition of
     * the first child will create a new List<RIBNode>.
     * @param child a RIBNode object to set.
     */
    public void addChild(RIBNode child) {
        if (children == null) {
            children = new ArrayList<RIBNode>();
        }
        children.add(child);
    }
     
    /**
     * Inserts a RIBNode at the specified position in the child list. Will     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
     * @param index the position to insert at.
     * @param child the RIBNode object to insert.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void insertChildAt(int index, RIBNode child) throws IndexOutOfBoundsException {
        if (index == getNumberOfChildren()) {
            // this is really an append
            addChild(child);
            return;
        } else {
            children.get(index); //just to throw the exception, and stop here
            children.add(index, child);
        }
    }
     
    /**
     * Remove theRIBNode element at index index of the List<RIBNode>.
     * @param index the index of the element to delete.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }
 
    public RIBHandler getRIBHandler() {
        return this.ribHandler;
    }
 
    public void setRIBHandler(RIBHandler ribHandler) {
        this.ribHandler = ribHandler;
    }
     
    public String getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public long getObjectInstance() {
		return objectInstance;
	}

	public void setObjectInstance(long objectInstance) {
		this.objectInstance = objectInstance;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(getObjectName().toString()).append(",[");
        int i = 0;
        for (RIBNode e : getChildren()) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(e.getObjectName().toString());
            i++;
        }
        sb.append("]").append("}");
        return sb.toString();
    }
}
