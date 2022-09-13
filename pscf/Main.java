//
// Arquitetura von Neumann Básica
// PSCF
// Prof. Luiz Lima Jr.
//
// IO <- CPU <-> RAM
//

package pscf;

public class Main {

    public static void main(String[] args) {

        System.out.println("* Arquitetura von Neumann Básica - PSCF\n");

        // cria componentes da arquitetura

        IO io = new IO(System.out);
        RAM ram = new RAM(8388608);
        CACHE_MD cache = new CACHE_MD(4096, 64, ram);
        CPU cpu = new CPU(io, cache);

        try {

            // carrega "programa" na memória

            final int inicio = 10;

            ram.Write(inicio, 120);
            ram.Write(inicio+1, 128);

            // executa programa

            cpu.Run(inicio);

        } catch (EnderecoInvalido e) {
            System.err.println("Erro: " + e);
        }
    }
}

