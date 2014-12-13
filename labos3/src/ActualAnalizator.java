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
	public ActualAnalizator(TreeNode root) {
		this.root = root;
		scope = new TableNode();
	}

	public void analyze() {
		prijevodnaJedinica(root);
	}

	private void prijevodnaJedinica(TreeNode node) {
		if (node.getChildren().size() == 1) {
			vanjskaDeklaracija(node.getChildAt(0));
			if (error)
				return;
		} else {
			prijevodnaJedinica(node.getChildAt(0));
			if (error)
				return;
			vanjskaDeklaracija(node.getChildAt(1));
			if (error)
				return;

		}
	}

	private void vanjskaDeklaracija(TreeNode node) {
		if (node.getChildAt(0).getContent().equals("<definicija_funkcije>")) {
			definicijaFunkcije(node.getChildAt(0));
			if (error)
				return;
		} else {
			deklaracija(node.getChildAt(0));
			if (error)
				return;
		}
	}

	private void definicijaFunkcije(TreeNode node) {

		imeTipa(node.getChildAt(0));

		if (node.getChildAt(0).isConst()) {
			printErrorMessage(node);
			return;
		}

		if (existsFunctionBefore(scope, node.getFunctionName(), node.getType())) {
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

			slozenaNaredba(node.getChildAt(6));
			if (error)
				return;

			scope.addChild(node);

		} else {

			listaParametara(node.getChildAt(3));
			if (error)
				return;

			if (conflictingDeclaration(scope, node.getFunctionName(), node.getType())) {
				printErrorMessage(node);
				return;
			}

			node.setDefined(true);
			node.setType(node.getChildAt(0).getType());

			node.setTypes(node.getChildAt(3).getTypes());
			node.setNames(node.getChildAt(3).getNames());

			scope.addChild(node);
			slozenaNaredba(node.getChildAt(5));
			if (error)
				return;

		}

	}

	private void deklaracija(TreeNode node) {
		imeTipa(node.getChildAt(0));
		if (error)
			return;
		node.getChildAt(1).setType(node.getChildAt(0).getType());
		listaInitDeklaratora(node.getChildAt(1));
	}

	private void imeTipa(TreeNode node) {
		specifikatorTipa(node.getChildAt(0));
		if (node.getChildren().size() == 1) {
			node.setType(node.getChildAt(0).getType());
		} else {
			TreeNode specTip = node.getChildAt(0);
			if (specTip.getType().equals("void"))
				printErrorMessage(node);
			if (error)
				return;
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
			listaNaredbi(node.getChildAt(1));
			if (error)
				return;
		} else {
			listaDeklaracija(node.getChildAt(1));
			if (error)
				return;
			listaNaredbi(node.getChildAt(2));
			if (error)
				return;
		}

		scope = scope.getParent();

	}

	private void deklaracijaParametra(TreeNode node) {
		imeTipa(node);
		if (error)
			return;
		if (node.getChildAt(0).getType().equals("void")) {
			printErrorMessage(node);
		}
		// postavi tip i ime
		if (node.getChildren().size() == 2) {
			node.setType(node.getChildAt(0).getType());
		} else {
			node.setType("niz" + node.getChildAt(0).getType());
		}
		node.setName(node.getChildAt(1).getName());
	}

	private void listaParametara(TreeNode node) {
		if (node.getChildren().size() == 1) {
			deklaracijaParametra(node.getChildAt(0));
			if (error)
				return;
			node.addType(node.getChildAt(0).getType());
			node.addName(node.getChildAt(0).getName());
		} else {

			listaParametara(node.getChildAt(0));
			if (error)
				return;
			deklaracijaParametra(node.getChildAt(2));
			if (error)
				return;

			if (node.getChildAt(0).getNames().contains(node.getChildAt(2).getName())) {
				printErrorMessage(node);
				return;
			}

			node.setTypes(node.getChildAt(0).getTypes());
			node.addType(node.getChildAt(2).getType());

			node.setNames(node.getChildAt(0).getNames());
			node.addName(node.getChildAt(2).getName());

		}
	}

	private void initDeklarator(TreeNode node) {
		// nasljedno svojstvo prvo
		node.getChildAt(0).setType(node.getType());
		izravniDeklarator(node);
		if (error)
			return;
		if (node.getChildren().size() == 1) {
			// sad tu treba biti prvoDijete.tip =/= const
			// "vazno je uociti da se provjerava izvedeno svojstvo tip, a ne nasljedno svojstvo ntip"
			// znaci da ih treba razlikovati -.-
		}
	}

	private void listaInitDeklaratora(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.getChildAt(0).setType(node.getType());
			initDeklarator(node.getChildAt(0));
			if (error)
				return;
		} else {

			node.getChildAt(0).setType(node.getType());
			listaInitDeklaratora(node.getChildAt(0));
			if (error)
				return;

			node.getChildAt(2).setType(node.getType());
			initDeklarator(node.getChildAt(2));
			if (error)
				return;

		}
	}

	private void specifikatorTipa(TreeNode node) {
		if (node.getChildAt(0).equals("KR_VOID"))
			node.setType("void");
		if (node.getChildAt(0).equals("KR_CHAR"))
			node.setType("char");
		if (node.getChildAt(0).equals("KR_INT"))
			node.setType("int");
	}

	private void izrazPridruzivanja(TreeNode node) {
		if (node.getChildren().size() == 1) {
			logIliIzraz(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType());
			node.setlValue(node.getChildAt(0).getlValue());
		} else {
			postfiksIzraz(node.getChildAt(0));
			if (error)
				return;
			izrazPridruzivanja(node.getChildAt(2));
			if (error)
				return;
			// provjeri ~
			if (!isImplicitlyCastable(node.getChildAt(2), node.getChildAt(0).getType())) {
				printErrorMessage(node);
			}
			node.setType(node.getChildAt(0).getType());
			node.setlValue(false);
		}
	}

	private void izraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0));
			if (error)
				return;
			node.setType(node.getChildAt(0).getType());
			node.setlValue(node.getChildAt(0).getlValue());
		} else {
			izraz(node.getChildAt(0));
			if (error)
				return;
			izrazPridruzivanja(node.getChildAt(2));
			if (error)
				return;
			node.setType(node.getChildAt(2).getType());
			// ne znam jel trebamo ovo kao boolean ili int
			node.setlValue(false);
		}
	}

	private void izrazNaredba(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.setType("int");
		} else {
			izraz(node.getChildAt(1));
			if (error)
				return;
			node.setType(node.getChildAt(1).getType());
		}
	}

	private void naredba(TreeNode node) {
		if (node.getChildAt(0).equals("<slozena_naredba>")) {
			slozenaNaredba(node.getChildAt(0));
			if (error)
				return;
		}
		if (node.getChildAt(0).equals("<izraz_naredba>")) {
			izrazNaredba(node.getChildAt(0));
			if (error)
				return;
		}
		if (node.getChildAt(0).equals("<naredba_grananja>")) {
			naredbaGrananja(node.getChildAt(0));
			if (error)
				return;
		}
		if (node.getChildAt(0).equals("<naredba_petlje>")) {
			naredbaPetlje(node.getChildAt(0));
			if (error)
				return;
		}
		if (node.getChildAt(0).equals("<naredba_skoka>")) {
			naredbaSkoka(node.getChildAt(0));
			if (error)
				return;
		}
	}

	private void listaNaredbi(TreeNode node) {
		if (node.getChildren().size() == 1) {
			naredba(node.getChildAt(0));
		} else {
			listaNaredbi(node.getChildAt(0));
			naredba(node.getChildAt(1));
		}
	}

	private void listaDeklaracija(TreeNode node) {
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
				if (d.isFunction() && d.getFunctionName().equals(funName) && d.isFunctionDefined())
					return true;
			}
			prevNode = node;
			node = node.getParent();
		}

		return false;
	}

	private boolean conflictingDeclaration(TableNode node, String funName, String funType) {
		while (node.getParent() != null)
			node = node.getParent();

		for (TreeNode d : node.getDeclaredStuff()) {
			if (d.isFunction() && d.getFunctionName().equals(funName) && !d.getType().equals(funType))
				return true;
		}

		return false;

	}

	/**
	 * 
	 * @param node
	 */
	private void printErrorMessage(TreeNode node) {
		System.out.println(node.getContent() + " ::= " + node);
		error = true;
	}

	/**
	 * Method that returns whether t1 can be implicitly cast to t2.
	 * @param t1
	 * @param t2
	 * @return Possible cast or not
	 */
	private boolean isImplicitlyCastable(TreeNode t1, String t2) {
		// const(T) ~ T
		if (!t1.getType().startsWith("niz") && t1.isConst()) {
			return true;
		}
		// char ~ int
		if (t1.getType().equals("char") && t2.equals("int")) {
			return true;
		}
		// niz(T) ~ niz(const(T)), ako T =/= const
		if (!t1.isConst() && t1.getType().startsWith("niz")) {
			return true;
		}
		return false;
	}

}
