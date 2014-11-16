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

	@Override
	public String toString() {
		String ret = "";
		ret += this.nodeContent + "\n";
		for (int i = children.size() - 1; i >= 0; i--)
			ret += " " + children.get(i).toString();
		// ret += "\n";
		return ret;
	}

}
