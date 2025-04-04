package util;

import java.util.ArrayList;

/**
 *
 * @author Pablo
 */
public class Paragrafo {    
    private ArrayList<String> palavras = new ArrayList<String>();

    public Paragrafo() {
    }

    public void addPalavra(String palavra) {
        palavras.add(palavra);
    }

    public int quantiadadePalavra() {
        return palavras.size();
    }

    public ArrayList<String> getPalavras() {
        return palavras;
    }    
}
