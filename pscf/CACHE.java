package pscf;

public class CACHE extends Memoria{

    RAM ram;
    int[] dados;
    int primeiroEnderecoRam;
    boolean isModificada;


    public CACHE(int capacidade, RAM ram) {
        super(capacidade);
        this.dados = new int[capacidade];
        this.ram = ram;
        boolean isModificada = false;
        this.primeiroEnderecoRam = -1; //-1 representa cache vazia
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
}
