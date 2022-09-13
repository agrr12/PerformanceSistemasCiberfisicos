package pscf;

public class CACHE_MD extends Memoria {

    RAM ram;
    CACHE_LINE[] CACHE_LINES;
    int primeiroEnderecoRam;
    boolean isModificada;
    int ultimaLinhaModificada;

    public CACHE_MD(int capacidade, int qtdCacheLines, RAM ram) {
        super(capacidade);
        this.ram = ram;
        CACHE_LINES = new CACHE_LINE[qtdCacheLines];
        for(int i=0; i<qtdCacheLines; i++) {
        	CACHE_LINES[i] = new CACHE_LINE(new int[capacidade/qtdCacheLines]);
        }
        isModificada = false;
    }

    private static int[] decodeCPUMessage(int endereco){
        //Capacidade da memória principal: 8M palavras* 2^23
        //Capacidade total da memória cache: 4096 palavras* = 2^12/2^6 (6 dígitos)
        // Tamanho da cache line: 64 palavras* (isto é, K = 64) 2^6 (6 digitos)
    

        int w = endereco & 0b111111;
        int r = (endereco>>6) & 0b111111;
        int t = endereco >>12;
        int ramAddress = endereco;
        return new int [] {w,r,t, ramAddress};
    }
    
    private void copiarCacheParaRam(int w, int r, int ramAddress) {
    	int len = CACHE_LINES[r].getDados().length;
    	for (int i = 0; i < len; i++) {
    		ram.getDados()[ramAddress - (r-i)]=CACHE_LINES[r].getDados()[i];
  		}
    }
    
    //Checa se o endereço da RAM passado está na Cache
    private boolean contemEnderecoRam (int t, int r){
    	return CACHE_LINES[r].getT()==t;
    }
    //Copia o que está na RAM para a Cache
    private void copiarRamParaCache(int w, int r, int t, int ramAddress){
    	CACHE_LINES[r].t=t;
    	int len = CACHE_LINES[r].getDados().length;
    	for (int i = 0; i < len; i++) {
    		CACHE_LINES[r].getDados()[i]=ram.getDados()[ramAddress - (r-i)];
  		}
    }

    @Override
    int Read(int endereco) throws EnderecoInvalido {
        ram.VerificaEndereco(endereco);
        int[] enderecos = decodeCPUMessage(endereco);
        int w = enderecos[0];
        int r = enderecos[1];
        int t = enderecos[2];
        int enderecoRam = enderecos[3];
        if(contemEnderecoRam(t, r)){
            return CACHE_LINES[r].getDados()[w];
        }
        else{
            if(isModificada){
            	copiarCacheParaRam(w, r, enderecoRam);
                isModificada = false;
            }
            copiarRamParaCache(w, r, t, enderecoRam);
            return Read(endereco);
        }
    }

    @Override
    void Write(int endereco, int valor) throws EnderecoInvalido {
        ram.VerificaEndereco(endereco);
        int[] enderecos = decodeCPUMessage(endereco);
        int w = enderecos[0];
        int r = enderecos[1];
        int t = enderecos[2];
        int enderecoRam = enderecos[3];
        if(contemEnderecoRam(t, r)){
        	CACHE_LINES[r].getDados()[w]=valor;
            isModificada = true;
        }
        else{
            if(isModificada){
            	copiarCacheParaRam(w, r, enderecoRam);
                isModificada = false;
            }
            copiarRamParaCache(w, r, t, enderecoRam);
            Write(endereco, valor);
        }
    }
   
    //public static void main (String[] args) {
    	//int[] a = decodeCPUMessage(8000000);
    	//for (int i = 0; i < a.length; i++) {
    		//  System.out.println(a[i] + " !!! "+ Integer.toBinaryString(a[i]));
    		//}

   // }

}
