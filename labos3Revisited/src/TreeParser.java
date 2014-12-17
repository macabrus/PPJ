import java.util.ArrayList;
import java.util.Stack;


/**
 * This class constructs a tree from its text representation. 
 * 
 * @author Ivan Paljak
 */
public class TreeParser {

	private ArrayList<String> textTree;	
	private Stack<TreeNode> S; 
	
	/**
	 * Class constructor from <code>textTree</code> field.
	 * 
	 * @param textTree text representation of tree.
	 */
	public TreeParser(ArrayList<String> textTree) {
		this.textTree = textTree;
	}
	
	/**
	 * Method capable of parsing a tree from its textual representation and returning 
	 * its root node.
	 * @return Root node of parsed tree.
	 */
	public TreeNode getRoot() {
		
		S = new Stack<TreeNode>();
		
		for (String node : textTree) {
			
			int depth = getDepth(node);
			String trimmedNode = node.trim();
			
			if (S.isEmpty() || S.peek().getDepth() < depth) {
				S.push(new TreeNode(depth, trimmedNode));
			} else {
				do {
					TreeNode n = S.pop();
					S.peek().addChild(n);
				} while (S.peek().getDepth() != depth - 1);
				S.push(new TreeNode(depth, trimmedNode));
			}
			
		}
		
		while (S.size() != 1) {
			TreeNode n = S.pop();
			S.peek().addChild(n);
		}
		
		return S.peek();
	
	}
	
	/**
	 * Method which returns the depth of a node in the generative tree.
	 * @param node Tree node in its textual representation
	 * @return Depth of node
	 */
	private int getDepth(String node) {
		int ret = 0;
		while (node.charAt(ret) == ' ') ret++;
		return ret;
	}
	
}