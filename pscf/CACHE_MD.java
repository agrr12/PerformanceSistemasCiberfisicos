package pscf;

public class CACHE_MD extends Memoria {

    RAM ram;
    int[][] dados;
    int primeiroEnderecoRam;
    boolean isModificada;

    public CACHE_MD(int capacidade, int cacheLine) {
        super(capacidade);
        dados = new int[capacidade/cacheLine][cacheLine];
        isModificada = false;
    }

    private int[] decodeCPUMessage(int binaryAddress){

        //Capacidade da memória principal: 8M palavras* 2^23
        //Capacidade total da memória cache: 4096 palavras* = 2^12/2^6 (6 dígitos)
        // Tamanho da cache line: 64 palavras* (isto é, K = 64) 2^6 (6 digitos)

        int w = Integer.parseInt(Integer.toBinaryString(binaryAddress & 0b0000000000111111),2);
        int r = Integer.parseInt(Integer.toBinaryString((binaryAddress>>6) & 0b0000111111000000),2);
        int t = Integer.parseInt(Integer.toBinaryString(binaryAddress >>12),2);
        return new int [] {w,r,t};

    }
    //Checa se o endereço da RAM passado está na Cache
    private boolean contemEnderecoRam (int endereco){
        return endereco>=primeiroEnderecoRam && endereco< (primeiroEnderecoRam+capacidade);
    }
    //Copia o que está na Cache para a RAM
    private void copiarCacheParaRam(){
        for(int indexCache = 0; indexCache<capacidade; indexCache++){
            this.ram.getDados()[primeiroEnderecoRam+indexCache] = this.dados[indexCache];
        }
    }
    //Copia o que está na RAM para a Cache
    private void copiarRamParaCache(int novoPrimeiroEnderecoRam){
        primeiroEnderecoRam = novoPrimeiroEnderecoRam; //Atualiza o endereço inicial da RAM na cache
        //Se necessário, corrige o endereço inicial da RAM para preencher toda a cache
        if (novoPrimeiroEnderecoRam+capacidade>ram.capacidade){
            primeiroEnderecoRam = novoPrimeiroEnderecoRam - (ram.capacidade - novoPrimeiroEnderecoRam);
        }
        for(int indexCache = 0; indexCache<capacidade; indexCache++){
            this.dados[indexCache] = this.ram.getDados()[primeiroEnderecoRam+indexCache];
        }
    }

    @Override
    int Read(int endereco) throws EnderecoInvalido {
        ram.VerificaEndereco(endereco);
        if(contemEnderecoRam(endereco) && primeiroEnderecoRam!=-1){
            int ramIndex = endereco - primeiroEnderecoRam; //Pega o index do vetor da Cache
            return dados[ramIndex];
        }
        else{
            if(isModificada){
                copiarCacheParaRam();
                isModificada = false;
            }
            copiarRamParaCache(endereco);
            return Read(endereco);
        }
    }

    @Override
    void Write(int endereco, int valor) throws EnderecoInvalido {
        ram.VerificaEndereco(endereco);
        if(contemEnderecoRam(endereco)  && primeiroEnderecoRam!=-1){
            int ramIndex = endereco - primeiroEnderecoRam;;
            dados[ramIndex]=valor;
            isModificada = true;
        }
        else{
            if(isModificada){
                copiarCacheParaRam();
                isModificada = false;
            }
            copiarRamParaCache(endereco);
            Write(endereco, valor);
        }
    }

    public static void main (String[] args){
        int a = 0b1011;

        System.out.println(Integer.toBinaryString(a>>1));
        System.out.println(Integer.parseInt(Integer.toBinaryString(a),2));
    }
}
