
/**
 * The class that actually analyzes (?)
 * @author Ivan
 *
 */
public class ActualAnalizator {
	
	private TreeNode root;
	private TableNode scope;
	
	private boolean error = false; 
	
	/**
	 * Constructs class from root of generative tree
	 * @param root Root of generative tree
	 */
	public ActualAnalizator(TreeNode root){
		this.root = root;
		scope = new TableNode();
	}
	
	public void analyze() {
		prijevodnaJedinica(root);
	} 
	
	private void prijevodnaJedinica(TreeNode node) {
		if (node.getChildren().size() == 1) {
			vanjskaDeklaracija(node.getChildAt(0)); if (error) return;
		} else {
			prijevodnaJedinica(node.getChildAt(0)); if (error) return;
			vanjskaDeklaracija(node.getChildAt(1)); if (error) return;
	
		}
	}
	
	private void vanjskaDeklaracija(TreeNode node) {
		if (node.getChildAt(0).getContent().equals("<definicija_funkcije>")) {
			definicijaFunkcije(node.getChildAt(0)); if (error) return;
		} else {
			deklaracija(node.getChildAt(0)); if (error) return;
		}
	}
	
	private void definicijaFunkcije(TreeNode node) {
		
		imeTipa(node.getChildAt(0));
		
		if (node.getChildAt(0).isConst()) {
			printErrorMessage(node);
			return;
		}
	
		if (existsFunctionBefore(scope, node.getFunctionName(), node.getType())){
			printErrorMessage(node);
			return;
		}
		
		if (node.getChildAt(3).getContent().equals("KR_VOID")) {
			
			if (conflictingDeclaration(scope, node.getFunctionName(), node.getType())) {
				printErrorMessage(node);
				return;
			}
		
			node.setDefined(true);
			node.setType(node.getChildAt(0).getType());
			
			slozenaNaredba(node.getChildAt(6)); if (error) return;
			
			scope.addChild(node);
			
			
		} else {

			listaParametara(node.getChildAt(3)); if (error) return;
			
			if (conflictingDeclaration(scope, node.getFunctionName(), node.getType())) {
				printErrorMessage(node);
				return;
			}
			
			node.setDefined(true);
			node.setType(node.getChildAt(0).getType());
			
			node.setTypes(node.getChildAt(3).getTypes());
			node.setNames(node.getChildAt(3).getNames());
			
			scope.addChild(node);
			slozenaNaredba(node.getChildAt(5)); if (error) return;
		
		}
		
	}
	
	private void deklaracija(TreeNode node) {
		imeTipa(node.getChildAt(0)); 
		node.getChildAt(1).setType(node.getChildAt(0).getType());
		listaInitDeklaratora(node.getChildAt(1));
	}
	
	private void imeTipa(TreeNode node) {
		specifikatorTipa(node.getChildAt(0));
		if (node.getChildren().size() == 1) {
			node.setType(node.getChildAt(0).getType());
		} else {
			TreeNode specTip = node.getChildAt(0);
			if (specTip.getType().equals("void")) printErrorMessage(node); if (error) return;
			node.setType(node.getChildAt(0).getType());
			node.setConst();
		}
	}
	
	private void slozenaNaredba(TreeNode node) {
		
		TableNode copyOfScope = new TableNode(scope.getParent());
		copyOfScope.setDeclaredStuff(scope.getDeclaredStuff());
		
		TableNode newScope = new TableNode(copyOfScope);
		scope = newScope;
		
		// gadna brija neka
		
		if (node.getChildren().size() == 3) {
			listaNaredbi(node.getChildAt(1)); if (error) return;
		} else {
			listaDeklaracija(node.getChildAt(1)); if (error) return;
			listaNaredbi(node.getChildAt(2)); if (error) return;
		}
		
		scope = scope.getParent();
		
	}
	
	private void listaParametara(TreeNode node) {
		if (node.getChildren().size() == 1) {
			deklaracijaParametara(node.getChildAt(0)); if (error) return;
			node.addType(node.getChildAt(0).getType());
			node.addName(node.getChildAt(0).getName());
		} else {
			
			listaParametara(node.getChildAt(0)); if (error) return;
			deklaracijaParametara(node.getChildAt(2)); if (error) return;
			
			if(node.getChildAt(0).getNames().contains(node.getChildAt(2).getName())) {
				printErrorMessage(node);
				return;
			}
				
			node.setTypes(node.getChildAt(0).getTypes());
			node.addType(node.getChildAt(2).getType());
		
			node.setNames(node.getChildAt(0).getNames());
			node.addName(node.getChildAt(2).getName());
		
		}
	}	
	
	private void listaInitDeklaratora(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.getChildAt(0).setType(node.getType());
			initDeklarator(node.getChildAt(0)); if (error) return;
		} else {
			
			node.getChildAt(0).setType(node.getType());
			listaInitDeklaratora(node.getChildAt(0)); if (error) return;
			
			node.getChildAt(2).setType(node.getType());
			initDeklarator(node.getChildAt(2)); if (error) return;
			
		}
	}
	
	private void specifikatorTipa(TreeNode node) {
		if (node.getChildAt(0).equals("KR_VOID")) node.setType("void");
		if (node.getChildAt(0).equals("KR_CHAR")) node.setType("char");
		if (node.getChildAt(0).equals("KR_INT")) node.setType("int");
	}
	
	private void listaNaredbi(TreeNode node) {
		if (node.getChildren().size() == 1) {
			naredba(node.getChildAt(0));
		} else {
			listaNaredbi(node.getChildAt(0));
			naredba(node.getChildAt(1));
		}
	}
	
	private void listaDeklaracija(TreeNode node) {ž
		if (node.getChildren().size() == 1) {
			deklaracija(node.getChildAt(0));
		} else {
			listaDeklaracija(node.getChildAt(0));
			deklaracija(node.getChildAt(1));
		}
	}
	
	private boolean existsFunctionBefore(TableNode node, String funName, String funType) {
	
		TableNode prevNode = node;
		node = node.getParent();
		while (node != null) {
			for (TreeNode d : node.getDeclaredStuff()) {
				if (d.isFunction() && d.getFunctionName().equals(funName)
						&& d.isFunctionDefined()) return true;
			}
			prevNode = node;
			node = node.getParent();
		}
	
		return false;
	}
	
	private boolean conflictingDeclaration(TableNode node, String funName, String funType) {
		
		while (node.getParent() != null) node = node.getParent();
		
		for (TreeNode d : node.getDeclaredStuff()) {
			if (d.isFunction() && d.getFunctionName().equals(funName) && 
					!d.getType().equals(funType)) return true;
		}
		
		return false;
		
	}
	
	/**
	 * 
	 * @param node
	 */
	private void printErrorMessage(TreeNode node) {
		System.out.println(node.getContent() + " ::= " +  node);
		error = true;
	}
	
}
