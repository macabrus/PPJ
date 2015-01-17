import java.math.BigInteger;
import java.util.ArrayList;


/**
 * The class that actually analyzes (?)
 * @author Ivan Paljak
 *
 */
public class ActualAnalizator {
	
	private TreeNode root;
	private TableNode scope;
	
	private boolean error = false; 
	private boolean hasMain = false;
	
	private ArrayList<String> declaredFunctions;
	private ArrayList<String> definedFunctions;
	
	/**
	 * Constructs class from root of generative tree
	 * @param root Root of generative tree
	 */
	public ActualAnalizator(TreeNode root){
		this.root = root;
		scope = new TableNode();
		declaredFunctions = new ArrayList<String>();
		definedFunctions = new ArrayList<String>();
	}
	
	public void analyze() {
		prijevodnaJedinica(root);
	}
	
	// ------------------------------------------------
	// POCETAK IZRAZA
	// ------------------------------------------------
	
	private void primarniIzraz(TreeNode node) {
		TreeNode init = node.getChildAt(0);
		
		if (init.getContent().startsWith("IDN")) {
			if (!isDeclaredYet(init.getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType(init.getType(scope));
			node.setTypes(init.getTypes(scope));
			node.setName(init.getName());
			node.setLValue(init.getLValue(scope));
		}
		if (init.getContent().startsWith("BROJ")) {
			if (!isInt(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
		//	node.setName(init.getName());
			node.setLValue(false);
		}
		if (init.getContent().startsWith("ZNAK")) {
			if (!isChar(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType("char");
		//	node.setName(init.getName());
			node.setLValue(false);
		}
		if (init.getContent().startsWith("NIZ_ZNAKOVA")) {
			if (!isString(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType("nizchar");
			node.setConst();
		//	node.setName(init.getName());
			node.setLValue(false);
		}
		if (init.getContent().startsWith("L_ZAGRADA")) {
			izraz(node.getChildAt(1)); if (error) return;
			node.setType(node.getChildAt(1).getType(scope));
			node.setLValue(node.getChildAt(1).getLValue(scope));
		}
	}
	
	private void listaArgumenata(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0)); if (error) return;
			node.addType(node.getChildAt(0).getType(scope));
		} else {
			listaArgumenata(node.getChildAt(0)); if (error) return;
			izrazPridruzivanja(node.getChildAt(2)); if (error) return;
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.addType(node.getChildAt(2).getType(scope));
		}
	}
	
	private void postfiksIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			primarniIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
			return;
		}
		if (node.getChildAt(1).getContent().startsWith("L_UGL_ZAGRADA")) {
			postfiksIzraz(node.getChildAt(0)); if (error) return;
			String X;
			boolean isConst;
			if (!node.getChildAt(0).getType(scope).startsWith("niz")) {
				printErrorMessage(node);
				return;
			}
			X = node.getChildAt(0).getType(scope).substring(3);
			isConst = node.getChildAt(0).isConst();
			izraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType(X);
			node.setLValue(!isConst);
			return;
		}
		if (node.getChildren().size() == 3) {
			postfiksIzraz(node.getChildAt(0)); if (error) return;
			if (!node.getChildAt(0).isFunction() || !node.getChildAt(0).getTypeAt(0).equals("void")) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(0).getType(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(false);
		}
		if (node.getChildren().size() == 4){
			postfiksIzraz(node.getChildAt(0)); if (error) return; 
			listaArgumenata(node.getChildAt(2)); if (error) return;
			if (!node.getChildAt(0).isFunction()) {
				printErrorMessage(node);
				return;
			}
			TreeNode pIzraz = node.getChildAt(0);
			TreeNode listArg = node.getChildAt(2);
			if (pIzraz.getTypes(scope).size() != listArg.getTypes(scope).size()) {
				printErrorMessage(node);
				return;
			}
			for (int i = 0; i < pIzraz.getTypes(scope).size(); ++i) {
				 if (!isCastable(listArg.getTypeAt(i), pIzraz.getTypeAt(i))) {
					 printErrorMessage(node);
					 return;
				 }
			}
			node.setType(pIzraz.getType(scope));
			node.setLValue(false);
		}
		if (node.getChildren().size() == 2) {
			postfiksIzraz(node.getChildAt(0)); if (error) return;
			TreeNode pIzraz = node.getChildAt(0);
			if (!pIzraz.getLValue(scope) || !isCastable(pIzraz.getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void unarniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			postfiksIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else if (node.getChildAt(1).getContent().equals("<unarni_izraz>")) {
			unarniIzraz(node.getChildAt(1)); if (error) return;
			if (!node.getChildAt(1).getLValue(scope) || !isCastable(node.getChildAt(1).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		} else {
			castIzraz(node.getChildAt(1)); if (error) return;
			if (!isCastable(node.getChildAt(1).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void castIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			unarniIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));	
		} else {
			imeTipa(node.getChildAt(1)); if (error) return;
			castIzraz(node.getChildAt(3)); if (error) return;
			// TODO ovo vjerojatno nije ok, al otom potom... ifat cemo
			if (!isCastable(node.getChildAt(3).getType(scope), node.getChildAt(1).getType(scope)) &&
					!(node.getChildAt(3).getType(scope).equals("int") && node.getChildAt(1).getType(scope).equals("char")) ||
					node.getChildAt(3).isFunction() || node.getChildAt(1).isFunction()) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(1).getType(scope));
			node.setLValue(false);
		}
	}
	
	private void imeTipa(TreeNode node) {
		if (node.getChildren().size() == 1) {
			specifikatorTipa(node.getChildAt(0));
			node.setType(node.getChildAt(0).getType(scope));
		} else {
			specifikatorTipa(node.getChildAt(1));
			TreeNode specTip = node.getChildAt(1);
			if (specTip.getType(scope).equals("void")) printErrorMessage(node); if (error) return;
			node.setType(node.getChildAt(1).getType(scope));
			node.setConst();
		}
	}
	
	private void specifikatorTipa(TreeNode node) {
		if (node.getChildAt(0).getContent().startsWith("KR_VOID")) node.setType("void");
		if (node.getChildAt(0).getContent().startsWith("KR_CHAR")) node.setType("char");
		if (node.getChildAt(0).getContent().startsWith("KR_INT")) node.setType("int");
	}
	
	private void multiplikativniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			castIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));	
		} else {
			multiplikativniIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			castIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void aditivniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			multiplikativniIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			aditivniIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			multiplikativniIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void odnosniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			aditivniIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			odnosniIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			aditivniIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void jednakosniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			odnosniIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			jednakosniIzraz(node.getChildAt(0)); if (error) return;
			if (node.getChildAt(0).isFunction() || !isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			odnosniIzraz(node.getChildAt(2)); if (error) return;
			if (node.getChildAt(2).isFunction() || !isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void binIIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			jednakosniIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			binIIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			jednakosniIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void binXiliIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			binIIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			binXiliIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			binIIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void binIliIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			binXiliIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			binIliIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			binXiliIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void logIIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			binIliIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			logIIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			binIliIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void logIliIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			logIIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			logIliIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			logIIzraz(node.getChildAt(2)); if (error) return; 
			if (!isCastable(node.getChildAt(2).getType(scope), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
	}
	
	private void izrazPridruzivanja(TreeNode node) {
		if (node.getChildren().size() == 1) {
			logIliIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			postfiksIzraz(node.getChildAt(0)); if (error) return;
			if (!node.getChildAt(0).getLValue(scope)) {
				printErrorMessage(node);
				return;
			} 
			izrazPridruzivanja(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), node.getChildAt(0).getType(scope))) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(0).getType(scope));
			node.setLValue(false);
		}
	}
	
	private void izraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
			node.setLValue(node.getChildAt(0).getLValue(scope));
		} else {
			izraz(node.getChildAt(0)); if (error) return;
			izrazPridruzivanja(node.getChildAt(2)); if (error) return;
			node.setType(node.getChildAt(2).getType(scope));
			node.setLValue(false);
		}
	}

	// ------------------------------------------------
	// KRAJ IZRAZA
	// ------------------------------------------------
	
	// ------------------------------------------------
	// POCETAK NAREDBENE STRUKTURE PROGRAMA
	// ------------------------------------------------
	
	private void slozenaNaredba(TreeNode node) {
		TableNode copyOfScope = new TableNode(scope.getParent());
		copyOfScope.setDeclaredStuff(scope.getDeclaredStuff());
		if (scope.isInLoop()) copyOfScope.setLoop();
		
		TableNode newScope = new TableNode(copyOfScope);
		if (node.isInLoop() || scope.isInLoop()) newScope.setLoop();
		scope = newScope;
		
		if (node.isInLoop()) scope.setLoop();
		
		// System.out.println(node.getTypes(scope).size());
		// System.out.println(node.getNames().size());
		
		for (int i = 0; i < node.getTypes(scope).size(); ++i) {
			TreeNode newNode = new TreeNode(-1, "<" + node.getNames().get(i));
			newNode.setType(node.getTypes(scope).get(i));
			if (node.isInLoop()) newNode.setLoop();
			newNode.setName(node.getNames().get(i));
			scope.addChild(newNode);
		}
		
		// gadna brija neka
		
		if (node.getChildren().size() == 3) {
			listaNaredbi(node.getChildAt(1)); if (error) return;
		} else {
			if (node.isInLoop()) node.getChildAt(1).setLoop();
			listaDeklaracija(node.getChildAt(1)); if (error) return;
			listaNaredbi(node.getChildAt(2)); if (error) return;
		}
		scope = scope.getParent();
	}
	
	private void listaNaredbi(TreeNode node) {
		if (node.getChildren().size() == 1) {
			naredba(node.getChildAt(0)); if (error) return;
		} else {
			listaNaredbi(node.getChildAt(0)); if (error) return;
			naredba(node.getChildAt(1)); if (error) return;
		}
	}
	
	private void naredba(TreeNode node) {
		TreeNode rightSide = node.getChildAt(0);
		if (node.isInLoop()) rightSide.setLoop();
		if (rightSide.getContent().equals("<slozena_naredba>")) slozenaNaredba(rightSide);
		if (rightSide.getContent().equals("<izraz_naredba>")) izrazNaredba(rightSide);
		if (rightSide.getContent().equals("<naredba_grananja>")) naredbaGrananja(rightSide);
		if (rightSide.getContent().equals("<naredba_petlje>")) naredbaPetlje(rightSide);
		if (rightSide.getContent().equals("<naredba_skoka>")) naredbaSkoka(rightSide);
		if (error) return;
	}
	
	private void izrazNaredba(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.setType("int");
		} else {
			izraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType(scope));
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.setName(node.getChildAt(0).getName());
		}
	}
	
	private void naredbaGrananja(TreeNode node) {
		izraz(node.getChildAt(2)); if (error) return;
		if (!isCastable(node.getChildAt(2).getType(scope), "int") || node.getChildAt(2).isFunction()) {
			printErrorMessage(node);
			return;
		}
		naredba(node.getChildAt(4)); if (error) return;
		
		if (node.getChildren().size() > 5) {
			naredba(node.getChildAt(6)); if (error) return;
		}
	}
	
	private void naredbaPetlje(TreeNode node) {
		if (node.getChildren().size() == 5) {
			izraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(scope), "int") || node.getChildAt(2).isFunction()) {
				printErrorMessage(node);
				return;
			}
			node.getChildAt(4).setLoop();
			naredba(node.getChildAt(4)); if (error) return;
		}
		if (node.getChildren().size() == 6) {
			izrazNaredba(node.getChildAt(2)); if (error) return;
			izrazNaredba(node.getChildAt(3)); if (error) return;
			if (!isCastable(node.getChildAt(3).getType(scope), "int") || node.getChildAt(3).isFunction()) {
				printErrorMessage(node);
				return;
			}
			node.getChildAt(5).setLoop();
			naredba(node.getChildAt(5)); if (error) return;
		}
		if (node.getChildren().size() == 7) {
			izrazNaredba(node.getChildAt(2)); if (error) return;
			izrazNaredba(node.getChildAt(3)); if (error) return;
			if (!isCastable(node.getChildAt(3).getType(scope), "int") || node.getChildAt(3).isFunction()) {
				printErrorMessage(node);
				return;
			}
			izraz(node.getChildAt(4)); if (error) return;
			node.getChildAt(6).setLoop();
			naredba(node.getChildAt(6)); if (error) return;
		}
	}
	
	private void naredbaSkoka(TreeNode node) {
		// TODO izgleda malo cudno, zasad pretpostavlja da sve provjere prolaze
		if (node.getChildren().size() == 3) {
			izraz(node.getChildAt(1)); if (error) return;
			String type = getTypeOfCurrentFunction();
			if (!isCastable(node.getChildAt(1).getType(scope), type) || node.getChildAt(1).isFunction()) {
				printErrorMessage(node);
				return;
			}
		} else {
			if (node.getChildAt(0).getContent().startsWith("KR_RETURN")) {
				//ArrayList<String> parameters = getTypesOfCurrentFunction();
				if (!getTypeOfCurrentFunction().equals("void")) {
					printErrorMessage(node);
					return;
				}
			} else {
				if (!scope.isInLoop()) {
					printErrorMessage(node);
					return;
				}
	//			System.out.println("local scope: ");
	//			for (TreeNode decl : scope.getDeclaredStuff()) System.out.println(decl.getName());
			}
		}
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
	
	// ------------------------------------------------
	// KRAJ NAREDBENE STRUKTURE PROGRAMA
	// ------------------------------------------------
	
	// ------------------------------------------------
	// POCETAK DEKLARACIJA I DEFINICIJA
	// ------------------------------------------------
	
	private void definicijaFunkcije(TreeNode node) {
		imeTipa(node.getChildAt(0));
		if (node.getChildAt(0).isConst()) {
			printErrorMessage(node);
			return;
		}
		if (existsFunctionBefore(scope, node.getChildAt(1).getName(), node.getType(scope))){
			printErrorMessage(node);
			return;
		}
		if (node.getChildAt(3).getContent().startsWith("KR_VOID")) {
			if (conflictingDeclaration(scope, node.getChildAt(0).getName(), node.getChildAt(0).getType(scope))) {
				printErrorMessage(node);
				return;
			}
			node.setDefined(true);
			node.setName(node.getChildAt(1).getName());
			node.setType(node.getChildAt(0).getType(scope));
			node.addType("void");
			if (node.getName().equals("main") && node.getType(scope).equals("int")) hasMain = true;
			scope.addChild(node);
			definedFunctions.add(node.getName());
			slozenaNaredba(node.getChildAt(5)); if (error) return;
		} else {
			listaParametara(node.getChildAt(3)); if (error) return;
			if (conflictingDeclaration(scope, node.getFunctionName(), node.getType(scope))) {
				printErrorMessage(node);
				return;
			}
			node.setDefined(true);
			node.setType(node.getChildAt(0).getType(scope));
			node.setName(node.getChildAt(1).getName());
			node.getChildAt(5).setTypes(node.getChildAt(3).getTypes(scope));
			node.setTypes(node.getChildAt(3).getTypes(scope));			
			node.getChildAt(5).setNames(node.getChildAt(3).getNames());
			node.setNames(node.getChildAt(3).getNames());
			
			/*
			System.out.println("Types: ");
			for (String type : node.getChildAt(5).getTypes(scope)) System.out.println(type);
			
			System.out.println("Names: ");
			for (String name : node.getChildAt(5).getNames()) System.out.println(name);
			*/
			
			definedFunctions.add(node.getName());
			scope.addChild(node);
			slozenaNaredba(node.getChildAt(5)); if (error) return;
		}
		
	}
	
	private void listaParametara(TreeNode node) {
		if (node.getChildren().size() == 1) {
			deklaracijaParametara(node.getChildAt(0)); if (error) return;
			node.addType(node.getChildAt(0).getType(scope));
			node.addName(node.getChildAt(0).getName());
		} else {
			
			listaParametara(node.getChildAt(0)); if (error) return;
			deklaracijaParametara(node.getChildAt(2)); if (error) return;
			
			if(node.getChildAt(0).getNames().contains(node.getChildAt(2).getName())) {
				printErrorMessage(node);
				return;
			}
				
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.addType(node.getChildAt(2).getType(scope));
		
			node.setNames(node.getChildAt(0).getNames());
			node.addName(node.getChildAt(2).getName());
		
		}
	}	

	private void deklaracijaParametara(TreeNode node) {
		imeTipa(node.getChildAt(0)); if (error) return;
		if (node.getChildAt(0).getType(scope).equals("void")) {
			printErrorMessage(node);
			return;
		}
		if (node.getChildren().size() == 2) {
			node.setType(node.getChildAt(0).getType(scope));
		} else {
			node.setType("niz" + node.getChildAt(0).getType(scope));
		}
		node.setName(node.getChildAt(1).getName());
	//	scope.addChild(node);
	}
	
	private void listaDeklaracija(TreeNode node) {
		if (node.getChildren().size() == 1) {
			if (node.isInLoop()) node.getChildAt(0).setLoop();
			deklaracija(node.getChildAt(0));
		} else {
			if (node.isInLoop()) {
				node.getChildAt(0).setLoop();
				node.getChildAt(1).setLoop();
			}
			listaDeklaracija(node.getChildAt(0));
			deklaracija(node.getChildAt(1));
		}
	}
	
	private void deklaracija(TreeNode node) {
		imeTipa(node.getChildAt(0)); if (error) return;
		node.getChildAt(1).setType(node.getChildAt(0).getType(scope));
	//	System.out.println(node.getChildAt(0).isConst());
		if (node.getChildAt(0).isConst()) node.getChildAt(1).setConst();
		if (node.isInLoop()) node.getChildAt(1).setLoop();
		listaInitDeklaratora(node.getChildAt(1));
	}
	
	private void listaInitDeklaratora(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.getChildAt(0).setType(node.getType(scope));
			if(node.isConst()) node.getChildAt(0).setConst();
			if (node.isInLoop()) node.getChildAt(0).setLoop();
			initDeklarator(node.getChildAt(0)); if (error) return;
		} else {
			node.getChildAt(0).setType(node.getType(scope));
			if (node.isInLoop()) node.getChildAt(0).setLoop();
			if (node.isConst()) node.getChildAt(0).setConst();
			listaInitDeklaratora(node.getChildAt(0)); if (error) return;
			
			node.getChildAt(2).setType(node.getType(scope));
			if (node.isInLoop()) node.getChildAt(2).setLoop();
			if(node.isConst()) node.getChildAt(2).setConst();
			initDeklarator(node.getChildAt(2)); if (error) return;
		}
	}
	
	
	private void initDeklarator(TreeNode node) {
		node.getChildAt(0).setType(node.getType(scope));
		if (node.isConst()) node.getChildAt(0).setConst();
		if (node.isInLoop()) node.getChildAt(0).setLoop();
		izravniDeklarator(node.getChildAt(0)); if (error) return;
		if (node.getChildren().size() == 1) {
			if (node.getChildAt(0).isConst()) {
				printErrorMessage(node);
				return;
			}
		} else {
			inicijalizator(node.getChildAt(2)); if (error) return;
			if (node.getChildAt(0).isArray()) {
				if (node.getChildAt(0).getArraySize() <= node.getChildAt(2).getArraySize()) error = true;
				for (String type1 : node.getChildAt(2).getTypes(scope)) {
					//System.out.println(type1);
					if (!isCastable(type1, node.getChildAt(0).getType(scope).substring(3))) error = true;
				}
				if (error) {
					printErrorMessage(node);
					return;
				}
			} else {
				if (!isCastable(node.getChildAt(2).getType(scope), node.getChildAt(0).getType(scope))
						|| node.getChildAt(2).isFunction()) {
					printErrorMessage(node);
					return;
				}
			}
		}
	}
	
	
	private void izravniDeklarator(TreeNode node) {
		if (node.getChildren().size() == 1) {
			if (node.getType(scope).equals("void") || isDeclaredLocally(node.getChildAt(0).getName())) {
				printErrorMessage(node);
				return;
			}
			node.setDefined(false);
			node.setName(node.getChildAt(0).getName());
			scope.addChild(node);
			return;
		} 
		if (node.getChildAt(2).getContent().startsWith("BROJ")) {
			if (node.getType(scope).equals("void") || isDeclaredLocally(node.getName())) {
				printErrorMessage(node);
				return;
			}
			if (node.getChildAt(2).getNumberValue() <= 0 || node.getChildAt(2).getNumberValue() > 1024) {
				printErrorMessage(node);
				return;
			}
			node.setType("niz" + node.getType(scope));
			node.setArraySize(node.getChildAt(2).getNumberValue());
			node.setName(node.getChildAt(0).getName());
			scope.addChild(node);
		}
		if (node.getChildAt(2).getContent().startsWith("KR_VOID")) {
			TreeNode localDef = getLocalDeclaration(node.getChildAt(0).getName());
			if (localDef == null) {
				node.addType("void");
				node.setName(node.getChildAt(0).getName());
				scope.addChild(node);
				declaredFunctions.add(node.getName());
			} else {
				if (localDef.getTypes(scope).size() != 1 || !localDef.getTypeAt(0).equals("void")) {
					printErrorMessage(node);
					return;
				}
				node.setName(node.getChildAt(0).getName());
				node.addType("void");
			}
		} 
		if (node.getChildAt(2).getContent().equals("<lista_parametara>")) {
			listaParametara(node.getChildAt(2)); if (error) return;
			TreeNode localDec = getLocalDeclaration(node.getChildAt(0).getName());
			if (localDec != null) {
				if (!checkTypes(localDec, node.getChildAt(2))) {
					printErrorMessage(node);
					return;
				}
				node.setTypes(node.getChildAt(2).getTypes(scope));
				node.setName(node.getChildAt(0).getName());
			} else {
				node.setName(node.getChildAt(0).getName());
				node.setTypes(node.getChildAt(2).getTypes(scope));
				declaredFunctions.add(node.getName());
				scope.addChild(node);
			}
		}
	}
	
	
	private void inicijalizator(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0)); if (error) return;
			if (ideUNizZnakova(node.getChildAt(0))) {
				node.setArraySize(izracunajDuljinuZnakova(node));
				for (int i = 0; i < node.getArraySize(); ++i) node.addType("char");
			} else {
				node.setType(node.getChildAt(0).getType(scope));
				node.setTypes(node.getChildAt(0).getTypes(scope));
			}
			node.setName(node.getChildAt(0).getName());
		} else {
			listaIzrazaPridruzivanja(node.getChildAt(1)); if (error) return;
			node.setArraySize(node.getChildAt(1).getArraySize());
			node.setTypes(node.getChildAt(1).getTypes(scope));
			node.setName(node.getChildAt(1).getName());
		}
	}
	
	
	private void listaIzrazaPridruzivanja(TreeNode node){
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0)); if (error) return;
			node.addType(node.getChildAt(0).getType(scope));
			node.setArraySize(1);
		} else {
			listaIzrazaPridruzivanja(node.getChildAt(0)); if (error) return;
			izrazPridruzivanja(node.getChildAt(2)); if (error) return;
			node.setTypes(node.getChildAt(0).getTypes(scope));
			node.addType(node.getChildAt(2).getType(scope));
			node.setArraySize(node.getChildAt(0).getArraySize() + 1);
		}
	}
	
	// ------------------------------------------------
	// KRAJ DEKLARACIJA I DEFINICIJA
	// ------------------------------------------------
	
	// Helper functions START HERE
	
	private int izracunajDuljinuZnakova(TreeNode node) {
		while (!node.getChildren().isEmpty()) {
			node = node.getChildAt(0);
		}
		return node.getContent().split(" ")[2].length() - 2;	
	}
	
	private boolean ideUNizZnakova(TreeNode node) {
		while (!node.getChildren().isEmpty()) {
			if (node.getChildren().size() != 1) return false;
			node = node.getChildAt(0);
		}
		return node.getContent().startsWith("NIZ_ZNAKOVA");
	} 
	
	private boolean checkTypes(TreeNode n1, TreeNode n2) {
		if (n1.getTypes(scope).size() != n2.getTypes(scope).size()) return false;
		for (int i = 0; i < n1.getTypes(scope).size(); ++i) {
			if (!n1.getTypeAt(i).equals(n2.getTypeAt(i))) return false;
		}
		return true;
	}
	
	private TreeNode getLocalDeclaration(String name) {
 		for (TreeNode declaration : scope.getDeclaredStuff()) {
			if (declaration.getName().equals(name)) return declaration;
		}
		return null;
	}
	
	private boolean isDeclaredLocally(String name) {
		if (scope.getDeclaredStuff() == null) return false;
		for (TreeNode declaration : scope.getDeclaredStuff()) {
			if (declaration.getName().equals(name)) return true;
		}
		return false;
	}
	
	/** Method that checks whether something of the name <code>name</code> has been declared
	 * in local or whatever scope, up to the global one.
	 * @param name
	 * @return
	 */
	private boolean isDeclaredYet(String name) {
		TableNode node = new TableNode();
		node = scope;
		while (node != null) {
			for (TreeNode declaration : node.getDeclaredStuff()) {
				if (declaration.getName().equals(name)) return true;
			}
			node = node.getParent();
		}
		return false;
	}
	
	private boolean existsFunctionBefore(TableNode node, String funName, String funType) {
	
		//TableNode prevNode = node;
		//node = node.getParent();
		
		while (node != null) {
			for (TreeNode d : node.getDeclaredStuff()) {
				if (d.isFunction() && d.getFunctionName().equals(funName)
						&& d.isFunctionDefined()) return true;
			}
			//prevNode = node;
			node = node.getParent();
		}
	
		return false;
	}
	
	private boolean conflictingDeclaration(TableNode node, String funName, String funType) {
		
		while (node.getParent() != null) node = node.getParent();
		
		for (TreeNode d : node.getDeclaredStuff()) {
			if (d.isFunction() && d.getFunctionName().equals(funName) && 
					!d.getType(scope).equals(funType)) return true;
		}
		
		return false;
		
	}
	
	/**
	 * Checks whether type1 is implicitly castable into type2 (~)
	 * 
	 * @param type1 first type
	 * @param type2 second type
	 * @return :)
	 */
	private boolean isCastable(String type1, String type2) {
		return type1.equals(type2) || (type1.equals("char") && type2.equals("int"));
	}
	
	/**
	 * 
	 * @param node
	 */
	private void printErrorMessage(TreeNode node) {
		System.out.println(node.getContent() + " ::= " +  node);
		error = true;
	}
	
	private boolean isInt(String x) {
		BigInteger i = new BigInteger(x);
		return i.bitCount() < 32;
	} 
	
	private boolean isChar(String x){
		return x.length() == 3 || (x.charAt(1) == '\\' && "tn0'\"\\".indexOf(x.charAt(2)) != -1);
	}
	
	private boolean isString(String x) {
		for (int i = 1; i < x.length() - 1; ++i) {
			if (x.charAt(i) == '\\') {
				String s = "'";
				s += x.charAt(i) + x.charAt(i + 1);
				s += "'";
				if (!isChar(s)) return false;
			} 
		}
		return x.charAt(x.length() - 2) != '\\';
	}
	
	private String getTypeOfCurrentFunction() {
		TableNode node = new TableNode();
		node = scope;
		while (node != null) {
			for (int i = node.getDeclaredStuff().size() - 1; i >= 0; --i) {
				TreeNode declaration = node.getDeclaredStuff().get(i);
				if (declaration.isFunction() && declaration.isFunctionDefined()) {
					return declaration.getType(node);
				}
			}
			node = node.getParent();
		}
		return "";
	}
	
//	private ArrayList<String> getTypesOfCurrentFunction() {
//		TableNode node = new TableNode();
//		node = scope;
//		while (node != null) {
//			for (int i = node.getDeclaredStuff().size() - 1; i >= 0; --i) {
//				TreeNode declaration = node.getDeclaredStuff().get(i);
//				if (declaration.isFunction() && declaration.isFunctionDefined()) {
//					return declaration.getTypes(scope);
//				}
//			}
//			node = node.getParent();
//		}
//		return new ArrayList<String>();	
//	}
	
	public boolean gotError() {
		return error;
	}
	
	public boolean noMain() {
		return !hasMain;
	}
	
	public boolean functionError() {
	//	System.out.println("defined: ");
	//	for (String def : definedFunctions) System.out.println(def);
	//	System.out.println("declared");
	//	for (String dec : declaredFunctions) System.out.println(dec);
		return !definedFunctions.containsAll(declaredFunctions) && false;
		
	}
	
	// Helper functions END HERE
	
}
