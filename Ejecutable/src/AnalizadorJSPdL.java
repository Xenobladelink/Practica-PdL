import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class AnalizadorJSPdL {
	
	private static java.util.ArrayList<java.util.Hashtable<Integer,ElementoTS>> tablasSimbolos = new ArrayList<Hashtable<Integer, ElementoTS>>();
	private static ArrayList<Integer> despls = new ArrayList<Integer>();
	private static java.util.Scanner in;
	private static int numID = 0;
	private static int numFunc = 1;
	private static int numLinea = 1;
	private static boolean error = false;
	
	private static java.util.ArrayList<Pair<Token,Integer>> tokens = new ArrayList<Pair<Token,Integer>>();
	private static int ind = 0;
	
	private static Hashtable<String,String> traduccionTokens = new Hashtable<String,String>();
	
	

	@SuppressWarnings({ "resource", "deprecation" })
	public static void read(String ruta) throws Exception {	
		FileReader file = new FileReader(ruta);
		int carASCII = file.read();
		char carL;
		java.util.Hashtable<Integer,ElementoTS> tablaActual = new Hashtable<Integer,ElementoTS>();
		int idTabla=0;
		tablasSimbolos.add(idTabla,tablaActual);
		StructEstados aux = new StructEstados(0,0,"","");
		Token token = new Token("");
		boolean sigIDFUNC = false;
		boolean iDFUNC = false;
		boolean declarando = false;
		boolean argFUNC = false;
		Pair<Token, Integer> pair;
		FileOutputStream fichTokens = new FileOutputStream("tokens.txt");
		PrintStream ptokens = new PrintStream(fichTokens);
		while (carASCII!=-1) {
			carL = (char)carASCII;
			if(carL == '\n') numLinea++;
			error = transicionesEstados(carL,aux,tablaActual,declarando,argFUNC,error);
			if(error) {
				if(iDFUNC) {
					tablasSimbolos.add(tablaActual);
				}
				break;
			}
			if(aux.getEstado()>=8) {
				token = aux.getToken();
				pair = new Pair<Token,Integer>(token,numLinea);
				tokens.add(pair);
				if(token.getTipo().equals("ID") || token.getTipo().equals("CTE")) {
					ptokens.println("<"+token.getTipo()+","+token.getNum()+">");
				}
				else if (token.getTipo().equals("CAD")) {
					ptokens.println("<"+token.getTipo()+","+token.getCad()+">");
				}
				else {
					ptokens.println("<"+token.getTipo()+",>");
				}
				if(Character.isSpace(carL) || (aux.getEstado() != 8 && aux.getEstado() != 10)) {
					carASCII = file.read();
				}
				if(token.getTipo().equals("ID") && sigIDFUNC) {
					argFUNC=true;
					sigIDFUNC=false;
					iDFUNC=true;
					tablasSimbolos.set(0,tablaActual);
					tablaActual = new Hashtable<Integer,ElementoTS>();
				}
				if(argFUNC && token.getTipo().equals("CIERRPARENT")) {
					argFUNC=false;
				}
				if(token.getTipo().equals("FUNC")) {
					sigIDFUNC = true;
				}
				declarando = (token.getTipo().equals("LET") || sigIDFUNC);
				if(iDFUNC && token.getTipo().equals("CIERRLLAVE")) {
					iDFUNC=false;
					tablasSimbolos.add(tablaActual);
					tablaActual=tablasSimbolos.get(0);
				}
				aux = new StructEstados(0,0,"","");
			}
			else {
				carASCII = file.read();
			}
		}
		analisisSintacticoSemantico();
	}
	
	public static void imprimirTablaSimbolos(int idTabla, Hashtable<Integer,ElementoTS> tablaSimbolos, PrintStream pTS) throws FileNotFoundException {
		pTS.println("Tabla de simbolos #"+ idTabla +":");
		Object[] aux = tablaSimbolos.keySet().toArray();
		for(int i = aux.length-1 ; i >= 0 ; i--) {
			pTS.println("* LEXEMA: '"+tablaSimbolos.get(aux[i]).getLex()+"'");
			pTS.println("\t+ Tipo: '"+tablaSimbolos.get(aux[i]).getTipo()+"'");
			if(!tablaSimbolos.get(aux[i]).getTipo().equals("function")) {
				pTS.println("\t+ Despl: '"+tablaSimbolos.get(aux[i]).getDespl()+"'");
			} else {
				pTS.println("\t+ NumParam: '"+tablaSimbolos.get(aux[i]).getNumParam()+"'");
				if(tablaSimbolos.get(aux[i]).getNumParam() > 1) {
					for(int k = 0; k < tablaSimbolos.get(aux[i]).getNumParam(); k++) {
						pTS.println("\t\t+ TipoParam"+k+": '"+tablaSimbolos.get(aux[i]).getTipoParam(k)+"'");
//						pTS.println("\t+ ModoParam"+k+": '"+tablaSimbolos.get(aux[i]).getModoParam(k)+"'");
					}
				} else if(tablaSimbolos.get(aux[i]).getNumParam() == 1) { 
					pTS.println("\t\t+ TipoParam: '"+tablaSimbolos.get(aux[i]).getTipoParam(0)+"'");
//					pTS.println("\t+ ModoParam: '"+tablaSimbolos.get(aux[i]).getModoParam(0)+"'");
				}
				if(tablaSimbolos.get(aux[i]).getTipoRetorno()!=null) {
					pTS.println("\t+ TipoRetorno: '"+tablaSimbolos.get(aux[i]).getTipoRetorno()+"'");
				}
				pTS.println("\t+ EtiqFuncion: '"+tablaSimbolos.get(aux[i]).getEtiqFuncion()+"'");
//				if(tablaSimbolos.get(aux[i]).getParam()!=null) {
//					pTS.println("\t+ Param: '"+tablaSimbolos.get(aux[i]).getParam()+"'");
//				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean transicionesEstados(char carL, StructEstados aux, Hashtable<Integer,ElementoTS> tablaSimbolos, boolean declarando, boolean argFUNC, boolean error) throws Exception {
		int estado = aux.getEstado();
		int num = aux.getNum();
		String lex = aux.getLex();
		String cad = aux.getCad();
		switch(estado) {
		case 0:
			if (Character.isSpace(carL)) {
				estado = 0;
			}
			else if (Character.isDigit(carL)) {
				estado=1;
				num = Character.getNumericValue(carL);
			}
			else if (carL == '-') {
				estado = 2;
			}
			else if (Character.isLetter(carL) || carL == '_') {
				estado = 3;
				lex = Character.toString(carL);
			}
			else if (carL == '"') {
				cad += Character.toString(carL);
				estado = 4;
			}
			else if (carL == '&') {
				estado = 5;
			}
			else if (carL == '/') {
				estado = 6;
			}
			else if (carL == '=') {
				estado = 13;
				aux.setToken("ASIGN");
			}
			else if (carL == ',') {
				estado = 14;
				aux.setToken("COMA");
			}
			else if (carL == ';') {
				estado = 15;
				aux.setToken("POINTCOMA");
			}
			else if (carL == '(') {
				estado = 16;
				aux.setToken("ABRPARENT");
			}
			else if (carL == ')') {
				estado = 17;
				aux.setToken("CIERRPARENT");
			}
			else if (carL == '{') {
				estado = 18;
				aux.setToken("ABRLLAVE");
			}
			else if (carL == '}') {
				estado = 19;
				aux.setToken("CIERRLLAVE");
			}
			else if (carL == '<') {
				estado = 20;
				aux.setToken("LOWER");
			}
			else if (carL == '+') {
				estado = 21;
				aux.setToken("SUM");
			}
			else {
				error = true;
				System.out.println("Error léxico en la línea "+numLinea+": El carácter '" + carL + "' no es reconocido en este caso");
			}
			break;
		case 1:
			if (Character.isDigit(carL)) {
				num = num*10 + Character.getNumericValue(carL);
			}
			else {
				estado = 8;
				aux.setToken("CTE", num);
			}
			break;
		case 2:
			if (carL == '=') {
				estado = 9;
				aux.setToken("ARES");
			}
			else {
				error = true;
				System.out.println("Error léxico en la línea "+numLinea+": El carácter '" + carL + "' no es reconocido en este caso");
			}
			break;
		case 3:
			if(Character.isAlphabetic(carL) || Character.isDigit(carL) || carL == '_') {
				lex += Character.toString(carL);
			}
			else {
				estado = 10;
				switch (lex) {
				case "boolean":
					aux.setToken("BOOL");
					break;
				case "function":
					aux.setToken("FUNC");
					break;
				case "get":
					aux.setToken("GET");
					break;
				case "if":
					aux.setToken("IF");
					break;
				case "int":
					aux.setToken("INT");
					break;
				case "integer":
					aux.setToken("INT");
					break;
				case "let":
					aux.setToken("LET");
					break;
				case "put":
					aux.setToken("PUT");
					break;
				case "return":
					aux.setToken("RET");
					break;
				case "string":
					aux.setToken("STR");
					break;
				case "void":
					aux.setToken("VOID");
					break;
				case "while":
					aux.setToken("WHILE");
					break;
				default:
					int p;
					if(declarando || argFUNC) {
						p = buscarTS(lex,tablaSimbolos);
						if (p != -1) {
							error = true;
							System.out.println("Error semántico en la línea "+numLinea+": El identificador " + lex + " ya había sido declarado");
						}
						else {
							p = insertarTS(lex, tablaSimbolos);
						}
					}
					else {
						p = buscarTodasTS(lex, tablaSimbolos);
						if(p == -1) {
							error = true;
							System.out.println("Error semántico en la línea "+numLinea+": El identificador " + lex + " no había sido declarado");
						}	
					}
					aux.setToken("ID", p);
				}
			}
			break;
		case 4:
			if (carL != '"') {
				cad += Character.toString(carL);
			}
			else {
				estado = 11;
				cad += Character.toString(carL);
				aux.setToken("CAD",cad);
			}
			break;
		case 5:
			if (carL == '&') {
				estado = 12;
				aux.setToken("AND");
			}
			else {
				error = true;
				System.out.println("Error léxico en la línea "+numLinea+": El carácter '" + carL + "' no es reconocido en este caso");
			}
			break;
		case 6:
			if (carL == '/') {
				estado = 7;
			}
			else {
				error = true;
				System.out.println("Error léxico en la línea "+numLinea+": El carácter '" + carL + "' no es reconocido en este caso");
			}
			break;
		case 7:
			if (carL == '\n') {
				estado = 0;
			}
			break;
		default:
			error = true;
			System.out.println("Error: El estado recibido no es válido");
		}
		aux.setEstado(estado);
		aux.setNum(num);
		aux.setLex(lex);
		aux.setCad(cad);
		return error;
	}
	
	public static int buscarTodasTS(String nombre, Hashtable<Integer,ElementoTS> tablaActual) {
		int res = -1;
		Hashtable<Integer,ElementoTS> tablaSimbolos = tablasSimbolos.get(0);
		res = buscarTS(nombre,tablaSimbolos);
		if(res == -1) {
			res = buscarTS(nombre,tablaActual);
		}
		return res;
	}
	
	public static int buscarTS(String nombre, Hashtable<Integer,ElementoTS> tablaSimbolos) {
		Iterator<Integer> it = tablaSimbolos.keySet().iterator();
		int aux;
		int res = -1;
		while(it.hasNext() && res==-1) {
			aux = it.next();
			if(tablaSimbolos.get(aux).getLex().equals(nombre)) {
				res = aux;
			}
		}
		return res;
	}
	
	public static int insertarTS(String nombre, Hashtable<Integer,ElementoTS> tablaSimbolos) {
		ElementoTS elem = new ElementoTS(nombre);
		tablaSimbolos.put(numID, elem);
		numID++;
		return numID-1;

	}
	
	public static void analisisSintacticoSemantico() throws FileNotFoundException {
		FileOutputStream fichTS = new FileOutputStream("TablaSimbolos.txt");
		PrintStream pTS = new PrintStream(fichTS);
		ElementoTS elem = new ElementoTS();
		despls.add(0);
		FileOutputStream fichAS = new FileOutputStream("parse.txt");
		PrintStream pAS = new PrintStream(fichAS);
		pAS.print("descendente ");
		try {
			P(pAS, elem, 0, pTS);
			imprimirTablaSimbolos(0,tablasSimbolos.get(0),pTS);
			tablasSimbolos.remove(0);
			despls.remove(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (ExcepcionAnalizador e) {
			if(e.getMessage() != null) {
				System.out.println(e.getMessage());
			} else {
				for(int i = tablasSimbolos.size()-1; i >= 0; i--) {
					tablasSimbolos.remove(i);
				}
				for(int i = despls.size()-1; i >= 0; i--) {
					despls.remove(i);
				}
			}
			return;
		}
	}
	
	private static void equipara(String token) throws ExcepcionAnalizador{
		if(error && tokens.size() <= ind) {
			throw new ExcepcionAnalizador();
		}
		String aux = tokens.get(ind).getLeft().getTipo();
		ind++;
		if(!aux.equals(token)) {
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind-1).getRight()+": Se esperaba un"+traduccionTokens.get(token));
		}
	}
	
	private static String sig_tok() throws ExcepcionAnalizador {
		if(error && tokens.size() <= ind) {
			throw new ExcepcionAnalizador();
		} else if(ind == -1) {
			return "ERR";
		} else if(ind < tokens.size()) {
			return tokens.get(ind).getLeft().getTipo();
		} else {
			return "EOF";
		}
	}
	
	private static ElementoTS P(PrintStream pAS, ElementoTS elem, int ts, PrintStream pTS) throws FileNotFoundException, ExcepcionAnalizador {
			if(sig_tok() == "IF" || sig_tok() == "LET" || sig_tok() == "WHILE" || sig_tok() == "ID" || sig_tok() == "PUT" || sig_tok() == "GET" || sig_tok() == "RET") {
				pAS.print("1 ");
				B(pAS, elem, ts);
 				P(pAS, elem, ts, pTS);
				return elem;
			} else if(sig_tok() == "FUNC") {
				pAS.print("2 ");
				F(pAS, elem, ts, pTS);
				P(pAS, elem, ts, pTS);
				return elem;
			} else if(sig_tok() == "EOF") {
				pAS.print("3");
				return elem;
			} else if(sig_tok() == "ERR") {
				return null;
			} else {
				throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
			}
	}
	
	private static ElementoTS E(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		pAS.print("4 ");
		R(pAS,elem, ts);
		E1(pAS, elem, ts);
		return elem;
	}
	
	private static ElementoTS E1(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS E;
		if (sig_tok() == "AND") {
			pAS.print("5 ");
			equipara("AND");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			if(E.getTipo().equals("boolean")) {
				elem.setTipo(E.getTipo());
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-1).getRight()+": Solo se puede hacer and de booleanos");
			}
		} else {
			pAS.print("6 ");
		}
		return elem;
	}
	
	private static ElementoTS R(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		pAS.print("7 ");
		U(pAS, elem, ts);
		R1(pAS, elem, ts);
		return elem;
	}
	
	private static ElementoTS R1(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS R;
		if(sig_tok() == "LOWER") {
			pAS.print("8 ");
			equipara("LOWER");
			R(pAS, elem, ts);
			R = new ElementoTS(elem);
			if(R.getTipo().equals("int")) {
				elem.setTipo("boolean");
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-1).getRight()+": Solo se pueden comparar enteros");
			}
		} else {
			pAS.print("9 ");
		}
		return elem;
	}
	
	private static ElementoTS U(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		pAS.print("10 ");
		V(pAS, elem, ts);
		U1(pAS, elem, ts);
		return elem;
	}
	
	private static ElementoTS U1(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS U;
		if(sig_tok() == "SUM") {
			pAS.print("11 ");
			equipara("SUM");
			U(pAS, elem, ts);
			U = new ElementoTS(elem);
			if(U.getTipo().equals("int")) {
				elem.setTipo(U.getTipo());
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-1).getRight()+": Solo se pueden sumar enteros");
			}
		} else {
			pAS.print("12 ");
		}
		return elem;
	}
	
	private static ElementoTS V(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS V1;
		ElementoTS id;
		int indID;
		if(sig_tok() == "ID") {
			pAS.print("13 ");
			equipara("ID");
			indID = ind-1;
			V1(pAS, elem, ts);
			V1 = new ElementoTS(elem);
			id = tablasSimbolos.get(ts).get(tokens.get(indID).getLeft().getNum());
			if(id == null) id = tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum());
			if(id == null) {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind).getRight()+": La variable no ha sido declarada");
			}
			if(V1.getTipo().equals("tipo_ok")) {
				elem.setTipo(id.getTipo());
			} else if(!id.getTipo().equals("function")) {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Se le están pasando parámetros a un identificador que no es de tipo función");
			} else if(id.getNumParam() > V1.getNumParam()) {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Se le están pasando parámetros de menos a la función "+id.getLex());
			} else if(id.getNumParam() < V1.getNumParam()) {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Se le están pasando parámetros de más a la función "+id.getLex());
			} else if(!id.getTipoParam().equals(V1.getTipoParam())){
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Se le están pasando parámetros con tipos erróneos a la función "+id.getLex());
			} else {
				elem.setTipo(id.getTipoRetorno());
				elem.setTipoParam(V1.getTipoParam());
				elem.setNumParam(V1.getNumParam());
			}
		} else if(sig_tok() == "CTE") {
			pAS.print("14 ");
			equipara("CTE");
			elem.setTipo("int");
		} else if(sig_tok() == "CAD") {
			pAS.print("15 ");
			equipara("CAD");
			elem.setTipo("string");
		} else if(sig_tok() == "ABRPARENT") {
			pAS.print("16 ");
			equipara("ABRPARENT");
			E(pAS, elem, ts);
			equipara("CIERRPARENT");
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS V1(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		if(sig_tok() == "ABRPARENT") {
			pAS.print("17 ");
			equipara("ABRPARENT");
			L(pAS, elem, ts);
			equipara("CIERRPARENT");
		} else {
			pAS.print("18 ");
			elem.setTipo("tipo_ok");
		}
		return elem;
	}
	
	private static ElementoTS L(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS E;
		if(sig_tok() == "ID" || sig_tok() == "CTE" || sig_tok() == "CAD" || sig_tok() == "ABRPARENT") {
			pAS.print("19 ");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			elem.iniTipoParam(E.getTipo());
			elem.setNumParam(1);
			Q(pAS, elem, ts);
			return elem;
		} else {
			pAS.print("20 ");
			elem.iniTipoParam("vacio");
			elem.setNumParam(0);
			return elem;
		}
	}
	
	private static ElementoTS Q(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS E;
		if(sig_tok() == "COMA") {
			pAS.print("21 ");
			equipara("COMA");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			elem.addTipoParam(E.getTipo());
			elem.setNumParam(E.getNumParam()+1);
			Q(pAS, elem, ts);
			return elem;
		} else {
			pAS.print("22 ");
			return elem;
		}
	}
	
	private static ElementoTS X(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS E;
		if(sig_tok() == "ID" || sig_tok() == "CTE" || sig_tok() == "CAD" || sig_tok() == "ABRPARENT") {
			pAS.print("23 ");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			elem.setTipo(E.getTipo());
		} else {
			pAS.print("24 ");
			elem.setTipo("vacio");
		}
		return elem;
	}
	
	private static ElementoTS T(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		if(sig_tok() == "INT") {
			pAS.print("25 ");
			equipara("INT");
			elem.setTipo("int");
			elem.setDespl(1);
		} else if(sig_tok() == "BOOL") {
			pAS.print("26 ");
			equipara("BOOL");
			elem.setTipo("boolean");
			elem.setDespl(1);
		} else if(sig_tok() == "STR") {
			pAS.print("27 ");
			equipara("STR");
			elem.setTipo("string");
			elem.setDespl(64);
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS S(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS S1;
		ElementoTS id;
		ElementoTS E;
		ElementoTS X;
		int indID;
		if(sig_tok() == "ID") {
			pAS.print("28 ");
			equipara("ID");
			indID = ind-1;
			S1(pAS, elem, ts);
			S1 = new ElementoTS(elem);
			id = tablasSimbolos.get(ts).get(tokens.get(indID).getLeft().getNum());
			if(id == null) {
				id = tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum());
			}
			if(id.getTipo().equals(S1.getTipo())) {
				if(id.getTipo().equals("function")) {
					if(id.getNumParam() > S1.getNumParam()) {
						elem.setTipo("error");
						throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Se le están pasando parámetros de menos a la función "+id.getLex());
					} else if(id.getNumParam() < S1.getNumParam()) {
						elem.setTipo("error");
						throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Se le están pasando parámetros de más a la función "+id.getLex());
					} else if(!id.getTipoParam().equals(S1.getTipoParam())){
						elem.setTipo("error");
						throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Se le están pasando parámetros con tipos erróneos a la función "+id.getLex());
					} else {
						elem.setTipo(id.getTipoRetorno());
						elem.setTipoParam(S1.getTipoParam());
						elem.setNumParam(S1.getNumParam());
					}
				} else {
					elem.setTipo(S1.getTipo());
				}
			} else if(id.getTipo() == null) {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico: La variable no ha sido declarada");
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(indID).getRight()+": Se está asignando a la variable "+id.getLex()+" un tipo distinto al que posee");
			}
		} else if(sig_tok() == "PUT") {
			pAS.print("29 ");
			equipara("PUT");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			equipara("POINTCOMA");
			if(E.getTipo().equals("string") || E.getTipo().equals("int")) {
				elem.setTipo("tipo_ok");
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-1).getRight()+": Solo se puede hacer put con enteros o cadenas");
			}
		} else if(sig_tok() == "GET") {
			pAS.print("30 ");
			equipara("GET");
			equipara("ID");
			indID = ind-1;
			id = tablasSimbolos.get(ts).get(tokens.get(indID).getLeft().getNum());
			if(id == null) {
				id = tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum());
			}
			equipara("POINTCOMA");
			if(id.getTipo().equals("string") || id.getTipo().equals("int")) {
				elem.setTipo("tipo_ok");
			} else if(id.getTipo() == null) {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico: La variable no ha sido declarada");
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(indID).getRight()+": Solo se puede hacer get con enteros y cadenas");
			}
		} else if(sig_tok() == "RET") {
			pAS.print("31 ");
			equipara("RET");
			X(pAS, elem, ts);
			X = new ElementoTS(elem);
			equipara("POINTCOMA");
			elem.setTipoRetorno(X.getTipo());
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS S1(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS E;
		if(sig_tok() == "ASIGN") {
			pAS.print("32 ");
			equipara("ASIGN");
			E(pAS, elem, ts);
			equipara("POINTCOMA");
		} else if(sig_tok() == "ARES") {
			pAS.print("33 ");
			equipara("ARES");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			equipara("POINTCOMA");
			if(E.getTipo().equals("int")) {
				elem.setTipo(E.getTipo());
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Solo se pueden restar enteros");
			}
		} else if(sig_tok() == "ABRPARENT") {
			pAS.print("34 ");
			equipara("ABRPARENT");
			L(pAS, elem, ts);
			equipara("CIERRPARENT");
			equipara("POINTCOMA");
			elem.setTipo("function");
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS B(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS E;
		ElementoTS D;
		ElementoTS T;
		ElementoTS C;
		int indID;
		if(sig_tok() == "IF") {
			pAS.print("35 ");
			equipara("IF");
			equipara("ABRPARENT");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			equipara("CIERRPARENT");
			D(pAS, elem, ts);
			D = new ElementoTS(elem);
			if(E.getTipo().equals("boolean")) {
				elem.setTipo(D.getTipo());
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-1).getRight()+": La condición del if debe ser booleana");
			}
		} else if(sig_tok() == "LET") {
			pAS.print("36 ");
			equipara("LET");
			equipara("ID");
			indID = ind-1;
			T(pAS, elem, ts);
			T = new ElementoTS(elem);
			tablasSimbolos.get(ts).get(tokens.get(indID).getLeft().getNum()).setTipo(T.getTipo());
			tablasSimbolos.get(ts).get(tokens.get(indID).getLeft().getNum()).setDespl(despls.get(ts));
			despls.set(ts, despls.get(ts)+T.getDespl());
			elem.setTipo("tipo_ok");
			equipara("POINTCOMA");
		} else if(sig_tok() == "WHILE") {
			pAS.print("37 ");
			equipara("WHILE");
			equipara("ABRPARENT");
			E(pAS, elem, ts);
			E = new ElementoTS(elem);
			equipara("CIERRPARENT");
			equipara("ABRLLAVE");
			C(pAS, elem, ts);
			C = new ElementoTS(elem);
			equipara("CIERRLLAVE");
			if(E.getTipo().equals("boolean")) {
				elem.setTipo(C.getTipo());
			} else {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-1).getRight()+": La condición del while debe ser booleana");
			}
		} else if(sig_tok() == "ID" || sig_tok() == "PUT" || sig_tok() == "GET" || sig_tok() == "RET") {
			pAS.print("38 ");
			S(pAS, elem, ts);
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS D(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		if(sig_tok() == "ABRLLAVE") {
			pAS.print("39 ");
			equipara("ABRLLAVE");
			C(pAS, elem, ts);
			equipara("CIERRLLAVE");
		} else if(sig_tok() == "IF" || sig_tok() == "LET" || sig_tok() == "WHILE" || sig_tok() == "ID" || sig_tok() == "PUT" || sig_tok() == "GET" || sig_tok() == "RET") {
			pAS.print("40 ");
			B(pAS, elem, ts);
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS F(PrintStream pAS, ElementoTS elem, int ts, PrintStream pTS) throws ExcepcionAnalizador, FileNotFoundException {
		ElementoTS H;
		ElementoTS A;
		ElementoTS C;
		int indID;
		if(sig_tok() == "FUNC") {
			pAS.print("41 ");
			equipara("FUNC");
			equipara("ID");
			indID = ind-1;
			H(pAS, elem, ts);
			H = new ElementoTS(elem);
			tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum()).setEtiqFuncion(numFunc);
			tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum()).setTipo("function");
			tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum()).setTipoRetorno(H.getTipo());
			numFunc++;
			despls.add(0);
			equipara("ABRPARENT");
			A(pAS, elem, 1);
			A = new ElementoTS(elem);
			tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum()).setNumParam(A.getNumParam());
			tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum()).setTipoParam(A.getTipoParam());
			elem.setTipoParam(new ArrayList<String>());
			equipara("CIERRPARENT");
			equipara("ABRLLAVE");
			C(pAS, elem, 1);
			C = new ElementoTS(elem);
			equipara("CIERRLLAVE");
			if(!C.getTipoRetorno().equals(H.getTipo())) {
				elem.setTipo("error");
				throw new ExcepcionAnalizador("Error semántico en la línea "+tokens.get(ind-2).getRight()+": Retorno con un tipo distinto al de la función "+tablasSimbolos.get(0).get(tokens.get(indID).getLeft().getNum()).getLex());
			}
			imprimirTablaSimbolos(numFunc-1,tablasSimbolos.get(1),pTS);
			tablasSimbolos.remove(1);
			despls.remove(1);
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS H(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS T;
		if(sig_tok() == "INT" || sig_tok() == "BOOL" || sig_tok() == "STR") {
			pAS.print("42 ");
			T(pAS, elem, ts);
			T = new ElementoTS(elem);
			elem.setTipo(T.getTipo());
			elem.setDespl(T.getDespl());
		} else if(sig_tok() == "VOID") {
			pAS.print("43 ");
			equipara("VOID");
			elem.setTipo("vacio");
			elem.setDespl(0);
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
		return elem;
	}
	
	private static ElementoTS A(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS T;
		if(sig_tok() == "INT" || sig_tok() == "BOOL" || sig_tok() == "STR") {
			pAS.print("44 ");
			T(pAS, elem, ts);
			T = new ElementoTS(elem);
			equipara("ID");
			tablasSimbolos.get(ts).get(tokens.get(ind-1).getLeft().getNum()).setTipo(T.getTipo());
			tablasSimbolos.get(ts).get(tokens.get(ind-1).getLeft().getNum()).setDespl(despls.get(ts));
			despls.set(ts, despls.get(ts)+T.getDespl());
			elem.iniTipoParam(T.getTipo());
			elem.setNumParam(1);
			K(pAS, elem, ts);
			return elem;
		} else if(sig_tok() == "VOID") {
			pAS.print("45 ");
			equipara("VOID");
			elem.iniTipoParam("vacio");
			elem.setNumParam(0);
			return elem;
		} else {
			elem.setTipo("error");
			throw new ExcepcionAnalizador("Error sintáctico en la línea "+tokens.get(ind).getRight()+": No se esperaba un"+traduccionTokens.get(sig_tok()));
		}
	}
	
	private static ElementoTS K(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS T;
		if(sig_tok() == "COMA") {
			pAS.print("46 ");
			equipara("COMA");
			T(pAS, elem, ts);
			T = new ElementoTS(elem);
			equipara("ID");
			tablasSimbolos.get(ts).get(tokens.get(ind-1).getLeft().getNum()).setTipo(T.getTipo());
			tablasSimbolos.get(ts).get(tokens.get(ind-1).getLeft().getNum()).setDespl(despls.get(ts));
			despls.set(ts, despls.get(ts)+T.getDespl());
			elem.addTipoParam(T.getTipo());
			elem.setNumParam(elem.getNumParam()+1);
			K(pAS, elem, ts);
			return elem;
		} else {
			pAS.print("47 ");
			return elem;
		}
	}
	
	private static ElementoTS C(PrintStream pAS, ElementoTS elem, int ts) throws ExcepcionAnalizador {
		ElementoTS B;
		ElementoTS C;
		if(sig_tok() == "IF" || sig_tok() == "LET" || sig_tok() == "WHILE" || sig_tok() == "ID" || sig_tok() == "PUT" || sig_tok() == "GET" || sig_tok() == "RET") {
			pAS.print("48 ");
			B(pAS, elem, ts);
			B = new ElementoTS(elem);
			C(pAS, elem, ts);
			C = new ElementoTS(elem);
			if(B.getTipo().equals("tipo_ok") && C.getTipo().equals("tipo_ok")) {
				elem.setTipo("tipo_ok");
			} else {
				elem.setTipo(B.getTipo());
			}
		} else {
			pAS.print("49 ");
			if(elem.getTipoRetorno() == null) {
				elem.setTipoRetorno("vacio");
			}
		}
		return elem;
	}
	
	public static void main (String[] args) throws Exception {
		traduccionTokens.put("BOOL", " boolean");
		traduccionTokens.put("FUNC", " function");
		traduccionTokens.put("GET", " get");
		traduccionTokens.put("IF", " if");
		traduccionTokens.put("INT", " int");
		traduccionTokens.put("LET", " let");
		traduccionTokens.put("PUT", " put");
		traduccionTokens.put("RET", " return");
		traduccionTokens.put("STR", " string");
		traduccionTokens.put("VOID", " void");
		traduccionTokens.put("WHILE", " while");
		traduccionTokens.put("CTE", "a constante entera");
		traduccionTokens.put("CAD", "a cadena");
		traduccionTokens.put("ID", " identificador");
		traduccionTokens.put("ARES", " -=");
		traduccionTokens.put("ASIGN", " =");
		traduccionTokens.put("COMA", " ,");
		traduccionTokens.put("POINTCOMA", " ;");
		traduccionTokens.put("ABRPARENT", " (");
		traduccionTokens.put("CIERRPARENT", " )");
		traduccionTokens.put("ABRLLAVE", " {");
		traduccionTokens.put("CIERRLLAVE", " }");
		traduccionTokens.put("SUM", " +");
		traduccionTokens.put("AND", " &&");
		traduccionTokens.put("LOWER", " <");
		in = new java.util.Scanner(System.in);
		String route = in.nextLine();
		read(route);
	}
}
