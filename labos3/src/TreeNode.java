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
	
}
