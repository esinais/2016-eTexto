package conexao;

import java.util.Vector;

/** Esta classe é responsável por amarzenar em linhas e colunas, os
* resultados obtidos através de consultas no banco
*/
public class VetorPesquisa {

    // Vetores para captura dos valores de linhas e colunas
    private Vector colunas = new Vector();
    private Vector linhas = new Vector();


    // Encapsulamento dos Vetores para posterior acesso por outras classes
    public Vector getColunas() {
    return colunas;
    }

    public void setColunas(Vector colunas) {
    this.colunas = colunas;
    }

    public Vector getLinhas() {
    return linhas;
    }

    public void setLinhas(Vector linhas) {
    this.linhas = linhas;
    }

}
