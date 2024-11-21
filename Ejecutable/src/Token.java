
public class Token {

	private String tipo;
	private int num;
	private String cad;
	
	public Token(String tipo) {
		this.tipo = tipo;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public void setNum(int num) {
		this.num = num;
	}
	
	public void setCad(String cad) {
		this.cad = cad;
	}
	
	public String getTipo() {
		return this.tipo;
	}
	
	public int getNum() {
		return this.num;
	}
	
	public String getCad() {
		return this.cad;
	}
}
