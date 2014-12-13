import java.util.ArrayList;


/**
 * Class which models a table node...
 * 
 * @author Ivan Paljak
 */
public class TableNode {

	private TableNode parent;
	private ArrayList<TreeNode> declaredStuff; // jos nisam zicer dal je fakat TreeNode
	
	/**
	 * Empty constructor
	 */
	public TableNode() {
		parent = null;
		declaredStuff = new ArrayList<TreeNode>();
	}
	
	/**
	 * Constructor from parent
	 * @param parent
	 */
	public TableNode(TableNode parent){
		this.parent = parent;
		this.declaredStuff = new ArrayList<TreeNode>();
	}

	public TableNode getParent() {
		return parent;
	}

	public ArrayList<TreeNode> getDeclaredStuff() {
		return declaredStuff;
	}
	
	public void setDeclaredStuff(ArrayList<TreeNode> declaredStuff) {
		this.declaredStuff = declaredStuff;
	}
	
	public void addChild(TreeNode node) {
		declaredStuff.add(node);
	}
	
	
	
}
