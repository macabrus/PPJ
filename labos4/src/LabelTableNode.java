public class LabelTableNode {

	private String labela;
	private TreeNode value;
	private boolean isFunction;
	private boolean isEmpty;

	public LabelTableNode(String labela, TreeNode value) {
		super();
		this.labela = labela;
		this.value = value;
		this.isFunction = false;
		this.isEmpty = false;
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

	public boolean isFunction() {
		return isFunction;
	}

	public void setFunction(boolean isFunction) {
		this.isFunction = isFunction;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	public String getBytes() {
		String sol = "\t`DW ";
		int broj = Integer.parseInt(this.value.getName());
		int mask = (1 << 8) - 1;
		for (int i = 0; i < 4; i++) {
			sol += "%D " + (broj & mask) + ", ";
			broj >>= 8;
		}
		return sol.substring(0, sol.length() - 2) + "\n";
	}

}
