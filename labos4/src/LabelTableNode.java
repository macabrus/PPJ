public class LabelTableNode {

	private String labela;
	private TreeNode value;

	public LabelTableNode(String labela, TreeNode value) {
		super();
		this.labela = labela;
		this.value = value;
	}

	public String getLabela() {
		return labela;
	}

	public void setLabela(String labela) {
		this.labela = labela;
	}

	public TreeNode getValue() {
		return value;
	}

	public void setValue(TreeNode value) {
		this.value = value;
	}

}
