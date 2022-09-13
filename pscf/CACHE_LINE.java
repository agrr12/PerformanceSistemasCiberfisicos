package pscf;

public class CACHE_LINE {
	
	int t;
	int[] dados;
	public CACHE_LINE(int[] dados) {
		super();
		this.t = -1;
		this.dados = dados;
	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}

	public int[] getDados() {
		return dados;
	}

	public void setDados(int[] dados) {
		this.dados = dados;
	}
	
}
