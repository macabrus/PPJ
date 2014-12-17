import java.util.ArrayList;


/**
 * Class which models a single node of a generative tree.
 * 
 * @author Ivan Paljak
 */
public class TreeNode {
	
	private int depth;
	private String content;

	private ArrayList<TreeNode> children;

	// -------------------------------------------
	
	private boolean lValue;
	private boolean isConstant;
	private boolean isDefined;
	
	private int arraySize = -1;
	
	private String type = "";
	private String name = "";
	
	private ArrayList<String> types = new ArrayList<String>(); 
	private ArrayList<String> names = new ArrayList<String>();
	
	/**
	 * Node constructor from its contents 
	 */
	public TreeNode(int depth, String content) {
		this.depth = depth;
		this.content = content;
		this.children = new ArrayList<TreeNode>();
	}
	
	/**
	 * Getter method for <code>depth</code> field.
	 * @return depth of node
	 */
	public int getDepth() {
		return this.depth;
	}
	
	/**
	 * Getter method for <code>children</code> field
	 * @return children of node
	 */
	public ArrayList<TreeNode> getChildren() {
		return this.children;
	}
	
	/**
	 * Returns child at index
	 * 
	 * @param i index
	 * @return :)
	 */
	public TreeNode getChildAt(int i){
		return children.get(i);
	}
	
	public String getTypeAt(int i) {
		return types.get(i);
	}
	
	/**
	 * Adds child to a list of children of specific node.
	 * @param node Child to be added. 
	 */
	public void addChild(TreeNode node) {
		this.children.add(node);
	}

	/**
	 * Method which outputs the textual representation of node's subtree onto stdout.
	 */
	public void printSubtree(int indent) {
		for (int i = 0; i < indent; ++i) System.out.print(" ");
		System.out.println(content);
		for (TreeNode node : children) node.printSubtree(indent + 1);
	}
	
	/**
	 * Getter for content field
	 * @return content
	 */
	public String getContent() {
		return this.content;
	}
	
	/**
	 * :)
	 * @return :)
	 */
	public boolean isConst() {
		return isConstant;
	}
	
	public boolean isFunction() {
		return types.size() != 0;
	}
	
	public boolean isFunctionDefined() {
		if (!this.isFunction()) System.err.println("Greska koja se nebi smjela dogodit!");
		return this.isDefined;
	}
	
	public String getFunctionName() {
		if (!this.isFunction()) System.err.println("Greska koja se nebi smjela dogodit!");
		return this.getName();
		// return this.getChildAt(2).getContent();
 	}
	
	public String getType(TableNode scope) {
		if (this.getContent().startsWith("IDN")) {
			TableNode node = new TableNode();
			node = scope;
			while (node != null) {
				for (TreeNode declaration : node.getDeclaredStuff()) {
					if (declaration.getName().equals(this.getName())) return declaration.getType(null);
				}
				node = node.getParent();
			}
		}
		return type;
	}
	
	public String getName() {
		if (this.getContent().charAt(0) != '<') return this.getContent().split(" ")[2];
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setType(String type) {
		if (this.type.equals("niz")) {
			this.type += type; 
		} else {
			this.type = type;
		}
	}
	
	public void setDefined(boolean x) {
		isDefined = x;
	}
	
	public ArrayList<String> getTypes(TableNode scope) {
		if (this.getContent().startsWith("IDN")) {
			TableNode node = new TableNode();
			node = scope;
			while (node != null) {
				for (TreeNode declaration : node.getDeclaredStuff()) {
					if (declaration.getName().equals(this.getName())) return declaration.getTypes(null);
				}
				node = node.getParent();
			}
		}
		return this.types;
	}
	
	public ArrayList<String> getNames() {
		return this.names;
	}
	
	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}
	
	public void setNames(ArrayList<String> names) {
		this.names = names;
	}
	
	public void addType(String type) {
		this.types.add(type);
	} 

	public void addName(String name) {
		this.names.add(name);
	} 
	
	public void setConst() {
		this.isConstant = true;
	}
	
	public void setArraySize(int x) {
		arraySize = x;
	}
	
	public int getArraySize() {
		return this.arraySize;
	}
	
	public boolean isArray() {
		return type.startsWith("niz"); 
		// return arraySize != -1;
	}
	
	public boolean getLValue(TableNode scope) {
		if (this.getContent().startsWith("IDN")) {
			TableNode node = new TableNode();
			node = scope;
			while (node != null) {
				for (TreeNode declaration : node.getDeclaredStuff()) {
					if (declaration.getName().equals(this.getName())) 
						return (declaration.getType(scope).equals("int") || declaration.getType(scope).equals("char")) &&
								!declaration.isFunction();
				}
				node = node.getParent();
			}
		}
		return lValue;
	}
	
	public void setLValue(boolean x) {
		lValue = x;
	}
	
	public int getNumberValue() {
		String[] dubrovnik = content.split(" ");
		if (dubrovnik[2].length() > 4) return 1000000000;
		return Integer.parseInt(dubrovnik[2]);
	}
	
	@Override
	public String toString() {
		String ret = "";
		for (TreeNode child : children) ret += child.getContent() + " ";
		return ret;
	}
	
}
