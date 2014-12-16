
/**
 * The class that actually analyzes (?)
 * @author Ivan Paljak
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
	
	// ------------------------------------------------
	// POCETAK IZRAZA
	// ------------------------------------------------
	
	private void primarniIzraz(TreeNode node) {
		TreeNode init = node.getChildAt(0);
		
		if (init.equals("IDN")) {
			if (!isDeclaredYet(scope, init.getName())) {
				printErrorMessage(node);
				return;
			}
			node.setType(init.getType());
			node.setLValue(init.getLValue());
		}
		if (init.equals("BROJ")) {
			// TODO vrijednost je u rasponu int
			boolean inRange = false;
			if (!inRange) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		}
		if (init.equals("ZNAK")) {
			// TODO rijesit javine magije za (un)escapeanje
			// dozvoljeno je '\t', '\n', '\0', '\'', '\"' i '\\' od dvoznacnih konstanti
			boolean correct = false;
			if (!correct) {
				printErrorMessage(node);
				return;
			}
			node.setType("char");
			node.setLValue(false);
		}
		if (init.equals("NIZ_ZNAKOVA")) {
			// TODO vidjet kad je ovo ispravno
			boolean correct = false;
			if (!correct) {
				printErrorMessage(node);
				return;
			}
			node.setType("nizchar");
			node.setConst();
			node.setLValue(false);
		}
		if (init.equals("L_ZAGRADA")) {
			izraz(node.getChildAt(1)); if (error) return;
			node.setType(node.getChildAt(1).getType());
			node.setLValue(node.getChildAt(1).getLValue());
		}
	}
	
	private void postfiksIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			primarniIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		}
		if (node.getChildAt(1).getContent().equals("L_UGL_ZAGRADA")) {
			postfiksIzraz(node.getChildAt(0));
			String X;
			boolean isConst;
			if (!node.getChildAt(0).getType().startsWith("niz")) {
				printErrorMessage(node);
				return;
			}
			X = node.getChildAt(0).getType().substring(3);
			isConst = node.getChildAt(0).isConst();
			izraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
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
			if (pIzraz.getTypes().size() != listArg.getTypes().size()) {
				printErrorMessage(node);
				return;
			}
			for (int i = 0; i < pIzraz.getTypes().size(); ++i) {
				 if (!isCastable(listArg.getTypeAt(i), pIzraz.getTypeAt(i))) {
					 printErrorMessage(node);
					 return;
				 }
			}
			node.setType(pIzraz.getType());
			node.setLValue(false);
		}
		if (node.getChildren().size() == 2) {
			postfiksIzraz(node.getChildAt(0)); if (error) return;
			TreeNode pIzraz = node.getChildAt(0);
			if (!pIzraz.getLValue() || !isCastable(pIzraz.getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else if (node.getChildAt(1).getContent().equals("<unarni_izraz>")) {
			unarniIzraz(node.getChildAt(1)); if (error) return;
			if (!node.getChildAt(1).getLValue() || !isCastable(node.getChildAt(1).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			node.setType("int");
			node.setLValue(false);
		} else {
			castIzraz(node.getChildAt(1)); if (error) return;
			if (!isCastable(node.getChildAt(1).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			imeTipa(node.getChildAt(1)); if (error) return;
			castIzraz(node.getChildAt(3)); if (error) return;
			if (!isCastable(node.getChildAt(3).getType(), node.getChildAt(1).getType())) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(1).getType());
			node.setLValue(false);
		}
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
	
	private void specifikatorTipa(TreeNode node) {
		if (node.getChildAt(0).equals("KR_VOID")) node.setType("void");
		if (node.getChildAt(0).equals("KR_CHAR")) node.setType("char");
		if (node.getChildAt(0).equals("KR_INT")) node.setType("int");
	}
	
	private void multiplikativniIzraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			castIzraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			multiplikativniIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			castIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			aditivniIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			multiplikativniIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			odnosniIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			aditivniIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			jednakosniIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			odnosniIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			binIIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			jednakosniIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			binXiliIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			binIIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			binIliIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			binXiliIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			logIIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			binIliIzraz(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			logIliIzraz(node.getChildAt(0)); if (error) return;
			if (!isCastable(node.getChildAt(0).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			logIIzraz(node.getChildAt(2)); if (error) return; 
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			postfiksIzraz(node.getChildAt(0)); if (error) return;
			if (!node.getChildAt(0).getLValue()) {
				printErrorMessage(node);
				return;
			} 
			izrazPridruzivanja(node.getChildAt(2)); if (error) return;
			if (!isCastable(node.getChildAt(2).getType(), node.getChildAt(0).getType())) {
				printErrorMessage(node);
				return;
			}
			node.setType(node.getChildAt(0).getType());
			node.setLValue(false);
		}
	}
	
	private void izraz(TreeNode node) {
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType());
			node.setLValue(node.getChildAt(0).getLValue());
		} else {
			izraz(node.getChildAt(0)); if (error) return;
			izrazPridruzivanja(node.getChildAt(2)); if (error) return;
			node.setType(node.getChildAt(2).getType());
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
	
	private void listaNaredbi(TreeNode node) {
		if (node.getChildren().size() == 1) {
			naredba(node.getChildAt(0));
		} else {
			listaNaredbi(node.getChildAt(0));
			naredba(node.getChildAt(1));
		}
	}
	
	private void naredba(TreeNode node) {
		TreeNode rightSide = node.getChildAt(0);
		if (rightSide.getContent().equals("<slozena_naredba>")) slozenaNaredba(rightSide);
		if (rightSide.getContent().equals("<izraz_naredba>")) izrazNaredba(rightSide);
		if (rightSide.getContent().equals("<naredba_grananja>")) naredbaGrananja(rightSide);
		if (rightSide.getContent().equals("<naredba_petlje>")) naredbaPetlje(rightSide);
		if (rightSide.getContent().equals("<naredba_skoka>")) naredbaSkoka(rightSide);
	}
	
	private void izrazNaredba(TreeNode node) {
		if (node.getChildren().size() == 1) {
			node.setType("int");
		} else {
			izraz(node.getChildAt(0)); if (error) return;
			node.setType(node.getChildAt(0).getType());
		}
	}
	
	private void naredbaGrananja(TreeNode node) {
		izraz(node.getChildAt(2)); if (error) return;
		if (!isCastable(node.getChildAt(2).getType(), "int")) {
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
			if (!isCastable(node.getChildAt(2).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			naredba(node.getChildAt(4)); if (error) return;
		}
		if (node.getChildren().size() == 6) {
			izrazNaredba(node.getChildAt(2)); if (error) return;
			izrazNaredba(node.getChildAt(3)); if (error) return;
			if (!isCastable(node.getChildAt(3).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			naredba(node.getChildAt(5)); if (error) return;
		}
		if (node.getChildren().size() == 7) {
			izrazNaredba(node.getChildAt(2)); if (error) return;
			izrazNaredba(node.getChildAt(3)); if (error) return;
			if (!isCastable(node.getChildAt(3).getType(), "int")) {
				printErrorMessage(node);
				return;
			}
			izraz(node.getChildAt(4)); if (error) return;
			naredba(node.getChildAt(6)); if (error) return;
		}
	}
	
	private void naredbaSkoka(TreeNode node) {
		// TODO izgleda malo sjebano
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

	private void deklaracijaParametara(TreeNode node) {
		imeTipa(node.getChildAt(0)); if (error) return;
		if (node.getChildAt(0).getType().equals("void")) {
			printErrorMessage(node);
			return;
		}
		if (node.getChildren().size() == 2) {
			node.setType(node.getChildAt(0).getType());
		} else {
			node.setType("niz" + node.getChildAt(0).getType());
		}
		node.setName(node.getChildAt(0).getName());
	}
	
	private void listaDeklaracija(TreeNode node) {
		if (node.getChildren().size() == 1) {
			deklaracija(node.getChildAt(0));
		} else {
			listaDeklaracija(node.getChildAt(0));
			deklaracija(node.getChildAt(1));
		}
	}
	
	private void deklaracija(TreeNode node) {
		imeTipa(node.getChildAt(0)); if (error) return;
		node.getChildAt(1).setType(node.getChildAt(0).getType());
		listaInitDeklaratora(node.getChildAt(1));
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
	
	
	private void initDeklarator(TreeNode node) {
		node.getChildAt(0).setType(node.getType());
		izravniDeklarator(node.getChildAt(0)); if (error) return;
		if (node.getChildren().size() == 1) {
			if (node.getChildAt(0).isConst()) {
				printErrorMessage(node);
				return;
			}
		} else {
			inicijalizator(node.getChildAt(2)); if (error) return;
			if (node.getChildAt(0).isArray()) {
				if (node.getChildAt(0).getArraySize() < node.getChildAt(2).getArraySize()) error = true;
				for (String type1 : node.getChildAt(2).getTypes()) {
					if (!isCastable(type1, node.getChildAt(0).getType())) error = true;
				}
				if (error) {
					printErrorMessage(node);
					return;
				}
			} else {
				if (!isCastable(node.getChildAt(2).getType(), node.getType())) {
					printErrorMessage(node);
					return;
				}
			}
		}
	}
	
	
	private void izravniDeklarator(TreeNode node) {
		if (node.getChildren().size() == 1) {
			if (node.getType().equals("void") || isDeclaredLocally(node.getName())) {
				printErrorMessage(node);
				return;
			}
			node.setDefined(false);
			scope.addChild(node);
			return;
		} 
		if (node.getChildAt(2).getContent().equals("BROJ")) {
			if (node.getType().equals("void") || isDeclaredLocally(node.getName())) {
				printErrorMessage(node);
				return;
			}
			if (node.getChildAt(2).getNumberValue() <= 0 || node.getChildAt(2).getNumberValue() > 1024) {
				printErrorMessage(node);
				return;
			}
			node.setType("niz" + node.getType());
			node.setArraySize(node.getChildAt(2).getNumberValue());
			scope.addChild(node);
		}
		if (node.getChildAt(2).getContent().equals("KR_VOID")) {
			TreeNode localDef = getLocalDeclaration(node.getChildAt(0).getName());
			if (localDef == null) {
				node.addType("void");
				scope.addChild(node);
			} else {
				if (localDef.getTypes().size() != 1 || !localDef.getTypeAt(0).equals("void")) {
					printErrorMessage(node);
					return;
				}
				node.addType("void");
			}
		} 
		if (node.getChildAt(2).getContent().equals("<lista_parametara>")) {
			listaParametara(node.getChildAt(2)); if (error) return;
			TreeNode localDec = getLocalDeclaration(node.getChildAt(0).getName());
			if (localDec == null) {
				if (!checkTypes(localDec, node.getChildAt(2))) {
					printErrorMessage(node);
					return;
				}
				node.setTypes(node.getChildAt(2).getTypes());
			} else {
				node.setTypes(node.getChildAt(2).getTypes());
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
				node.setType(node.getChildAt(0).getType());
			}
		} else {
			listaIzrazaPridruzivanja(node.getChildAt(1)); if (error) return;
			node.setArraySize(node.getChildAt(1).getArraySize());
			node.setTypes(node.getChildAt(1).getTypes());
		}
	}
	
	
	private void listaIzrazaPridruzivanja(TreeNode node){
		if (node.getChildren().size() == 1) {
			izrazPridruzivanja(node.getChildAt(0)); if (error) return;
			node.addType(node.getChildAt(0).getType());
			node.setArraySize(1);
		} else {
			listaIzrazaPridruzivanja(node.getChildAt(0)); if (error) return;
			izrazPridruzivanja(node.getChildAt(2)); if (error) return;
			node.setTypes(node.getChildAt(0).getTypes());
			node.addType(node.getChildAt(2).getType());
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
		return node.getContent().equals("NIZ_ZNAKOVA");
	} 
	
	private boolean checkTypes(TreeNode n1, TreeNode n2) {
		if (n1.getTypes().size() != n2.getTypes().size()) return false;
		for (int i = 0; i < n1.getTypes().size(); ++i) {
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
	private boolean isDeclaredYet(TableNode node, String name) {
		while (node != null) {
			for (TreeNode declaration : node.getDeclaredStuff()) {
				if (declaration.getName().equals(name)) return true;
			}
			node = node.getParent();
		}
		return false;
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
	
	// Helper functions END HERE
	
}
