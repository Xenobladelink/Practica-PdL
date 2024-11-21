
public class StructEstados {

	private int estado;
	private int num;
	private String lex;
	private String cad;
	private Token token;
	
	public StructEstados(int estado, int num, String lex, String cad) {
		this.estado = estado;
		this.num = num;
		this.lex = lex;
		this.cad = cad;
		this.token = new Token(null);
	}
	
	public void setEstado(int estado) {
		this.estado = estado;
	}
	
	public void setNum(int num) {
		this.num = num;
	}
	
	public void setLex(String lex) {
		this.lex = lex;
	}
	
	public void setCad(String cad) {
		this.cad = cad;
	}
	
	public void setToken(String tipo) {
		this.token = new Token(tipo);
	}
	
	public void setToken(String tipo, int num) {
		this.token = new Token(tipo);
		this.token.setNum(num);
	}
	
	public void setToken(String tipo, String cad) {
		this.token = new Token(tipo);
		this.token.setCad(cad);
	}
	
	public int getEstado() {
		return this.estado;
	}
	
	public int getNum() {
		return this.num;
	}
	
	public String getLex() {
		return this.lex;
	}
	
	public String getCad() {
		return this.cad;
	}
	
	public Token getToken() {
		return this.token;
	}
}
