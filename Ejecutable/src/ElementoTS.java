import java.util.ArrayList;

public class ElementoTS {

	private String lex;
	private String tipo;
	private int despl;
	private int numParam;
	private ArrayList<String> tipoParam = new ArrayList<String>();
//	private ArrayList<String> modoParam = new ArrayList<String>();
	private String tipoRetorno;
	private int etiqFuncion;
//	private String param;
//	private boolean id;
	
	public ElementoTS(String lex) {
		this.lex = lex;
		this.tipo = null;
		this.despl = 0;
		this.numParam = 0;
		this.tipoRetorno = null;
		this.etiqFuncion = -1;
//		this.param = null;
//		this.id = false;
	}
	
	public ElementoTS() {
		this.lex = null;
		this.tipo = null;
		this.despl = 0;
		this.numParam = 0;
		this.tipoRetorno = null;
		this.etiqFuncion = -1;
//		this.param = null;
	}
	
	public ElementoTS(ElementoTS elem) {
		this.lex = elem.getLex();
		this.tipo = elem.getTipo();
		this.despl = elem.getDespl();
		this.numParam = elem.getNumParam();
		this.tipoParam = elem.getTipoParam();
		this.tipoRetorno = elem.getTipoRetorno();
		this.etiqFuncion = elem.getEtiqFuncion();
//		this.param = elem.getParam();
	}
	
	public void setDespl(int despl) {
		this.despl = despl;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public void setNumParam(int numParam) {
		this.numParam = numParam;
	}
	
	public void addTipoParam(String tipoParam) {
		this.tipoParam.add(tipoParam);
	}
	
	public void iniTipoParam(String tipoParam) {
		this.tipoParam = new ArrayList<String>();
		this.tipoParam.add(tipoParam);
	}
	
	public void setTipoParam(ArrayList<String> tipoParam) {
		this.tipoParam = tipoParam;
	}
	
//	public void addModoParam(String modoParam) {
//		this.modoParam.add(modoParam);
//	}
//	
//	public void setModoParam(int ind, String modoParam) throws ExcepcionParam {
//		if(ind >= numParam) {
//			throw new ExcepcionParam("Se está intentando añadir un parámetro fuera del límite");
//		}
//		this.modoParam.set(ind, modoParam);
//	}
//	
//	public void setModoParam(ArrayList<String> modoParam) {
//		this.modoParam = modoParam;
//	}
	
	public void setTipoRetorno(String tipoRetorno) {
		this.tipoRetorno = tipoRetorno;
	}
	
	public void setEtiqFuncion(int etiqFuncion) {
		this.etiqFuncion = etiqFuncion;
	}
	
//	public void setParam(String param) {
//		this.param = param;
//	}
	
//	public void setID(boolean id) {
//		this.id = id;
//	}
	
	public String getTipo() {
		return this.tipo;
	}
	
	public int getDespl() {
		return this.despl;
	}
	
	public int getNumParam() {
		return this.numParam;
	}
	
	public String getTipoParam(int ind) {
		return this.tipoParam.get(ind);
	}
	
	public ArrayList<String> getTipoParam() {
		return this.tipoParam;
	}
	
//	public String getModoParam(int ind) {
//		return this.modoParam.get(ind);
//	}
//	
//	public ArrayList<String> getModoParam() {
//		return this.modoParam;
//	}
	
	public String getTipoRetorno() {
		return this.tipoRetorno;
	}
	
	public int getEtiqFuncion() {
		return this.etiqFuncion;
	}
	
//	public String getParam() {
//		return this.param;
//	}
	
	public String getLex() {
		return this.lex;
	}
	
//	public boolean getId() {
//		return this.id;
//	}
}
