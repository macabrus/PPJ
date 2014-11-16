import java.util.ArrayList;

/**
 * Class which models a generative tree
 * 
 * @author Ivan Paljak
 */
public class TreeNode {

	private String nodeContent;
	private ArrayList<TreeNode> children;

	public TreeNode(String nodeContent) {
		this.nodeContent = nodeContent;
		children = new ArrayList<TreeNode>();
	}

	public String getNodeContent() {
		return this.nodeContent;
	}

	public ArrayList<TreeNode> getChildren() {
		return this.children;
	}

	public void addChild(TreeNode node) {
		children.add(node);
	}

	public void printIndent(int indent) {
		for (int i = 0; i < indent; ++i) System.out.print(" ");
	}
	
	public void printSubtree(int indent) {
		printIndent(indent);
		System.out.println(this.nodeContent);
		for (int i = children.size() - 1; i >= 0; i--)
			children.get(i).printSubtree(indent + 1);
}

}
