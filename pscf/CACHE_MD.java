package pscf;

public class CACHE_MD extends Memoria {

    RAM ram;
    CACHE_LINE[] CACHE_LINES;
    boolean isModificada;
    int k;

    public CACHE_MD(int capacidade, int qtdCacheLines, RAM ram) {
        super(capacidade);
        this.ram = ram;
        k = capacidade/qtdCacheLines;
        CACHE_LINES = new CACHE_LINE[qtdCacheLines]; //Cria um vetor de Cache Lines
        for(int i=0; i<qtdCacheLines; i++) { //Instancia cada cache line no vetor
        	CACHE_LINES[i] = new CACHE_LINE(new int[k]);
        }
        isModificada = false;
    }

    private static int[] decodificarEndereco(int endereco){
        //Capacidade da memória principal: 8M palavras* 2^23 ->23 dígitos necessários para representar endereços
        //Capacidade total da memória cache: 4096 palavras* = 2^12/2^6 (6 dígitos) ->
        // Tamanho da cache line: 64 palavras* (isto é, K = 64) 2^6 (6 digitos)

        int w = endereco & 0b111111; //Primeiros seis dígitos
        int r = (endereco>>6) & 0b111111;
        int t = endereco >>12; //11 dígitos finais
        int s = endereco >>6; //17 dígitos finais
        return new int [] {w,r,t, s};
    }
    
    private void copiarCacheParaRam(int s, int r, int primeiroDaRam) {
    	int len = CACHE_LINES[r].getDados().length;
    	for (int i = 0; i < len; i++) {
            //Copia cada dado na cache line para sua posição correspondente na RAM
            //A primeira posição na RAM é s0
    		ram.getDados()[primeiroDaRam+i]=CACHE_LINES[r].getDados()[i];
  		}
    }
    
    //Checa se o endereço da RAM passado está na Cache
    private boolean contemEnderecoRam (int t, int r){
        return CACHE_LINES[r].getT()==t;
    }

    //Copia o que está na RAM para a Cache
    private void copiarRamParaCache(int r, int s, int t, int primeiroDaRam){
    	CACHE_LINES[r].t=t; //Atualiza a Tag na Cache Line
    	int len = CACHE_LINES[r].getDados().length; //Registra o tamanho da cacheLine
    	for (int i = 0; i < len; i++) {
            //Copia cada dado no bloco de RAM para sua respectiva Cache Line.
            //A primeira posição na RAM é s0
    		CACHE_LINES[r].getDados()[i]=ram.getDados()[primeiroDaRam+i];
  		}
    }


    @Override
    int Read(int endereco) throws EnderecoInvalido {
        ram.VerificaEndereco(endereco);
        //Obtém w, r, t e s
        int[] enderecos = decodificarEndereco(endereco);
        int w = enderecos[0];
        int r = enderecos[1];
        int t = enderecos[2];
        int s = enderecos[3];
        int primeiroDaRam = k *((int)endereco/k);
        //Caso 1 --Cache Hit
        if(contemEnderecoRam(t, r)){
            return CACHE_LINES[r].getDados()[w];
        }
        //Caso 2 --Cache Miss
        else{
            //Se a Cache foi modificada, os novos dados são movidos para RAM
            if(isModificada){
            	copiarCacheParaRam(w, r, primeiroDaRam);
                isModificada = false;
            }
            copiarRamParaCache(r, s,t, primeiroDaRam);
            int a = CACHE_LINES[r].getDados()[w];
            int b = ram.getDados()[endereco];
            return CACHE_LINES[r].getDados()[w];
        }
    }

    @Override
    void Write(int endereco, int valor) throws EnderecoInvalido {
        ram.VerificaEndereco(endereco);
        //Obtém w, r, t e s
        int[] enderecos = decodificarEndereco(endereco);
        int w = enderecos[0];
        int r = enderecos[1];
        int t = enderecos[2];
        int s = enderecos[3];
        int primeiroDaRam = k *((int)endereco/k);
        //Caso 1 --Cache Hit

        if(contemEnderecoRam(t, r)){
        	CACHE_LINES[r].getDados()[w]=valor;
            isModificada = true;
        }
        //Caso 2 --Cache Miss
        else{
            //Se a Cache foi modificada, os novos dados são movidos para RAM
            if(isModificada){
            	copiarCacheParaRam(w, r, primeiroDaRam);
                isModificada = false;
            }
            copiarRamParaCache(r, s, t, primeiroDaRam);
            CACHE_LINES[r].getDados()[w]=valor;
            isModificada = true;
        }
    }

}
