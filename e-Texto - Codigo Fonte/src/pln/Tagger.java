package pln;

import conexao.ConexaoBD;
import conexao.VetorPesquisa;
import util.Paragrafo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

import tagger.TestTagger;
import util.Arquivo;
import util.Toker;
import util.Util;

/**
 *
 * @author Pablo
 */
public class Tagger {

    private ArrayList<Paragrafo> paragrafosSaida;
    private String textoEntrada;
    private String simbolos = "|\"_|\\(_|_\\)|\\[_|\\_]";
    //private String simbolos = "";    
    private String padraoPOS = "(" + "_PREP\\+ART|_PREP\\+PD|_[A-Z]{1,4}" + simbolos + ")";
    private int contador = 0;
    private Arquivo arquivo = Arquivo.getInstance();
    private ConexaoBD conexao = ConexaoBD.getInstance();
    private boolean isShowTranslate;

    public Tagger(String textoEntrada, boolean isShowTranslate) {
        this.isShowTranslate = isShowTranslate;
        this.textoEntrada = textoEntrada;
        ArrayList<Paragrafo> paragrafos = tokenizaArquivo(); // 1ª Etapa - Coloca as palavras do texto em uma array classificadas por parágrafo/palavras
        preparaTexto(paragrafos); // 2ª Etapa - Acrescenta espaço após cada Token - Entrada para o MXPOST
        etiqueta(); // 3ª etapa - Cria um arquivo etiquetado a partir do arquivo "preparado"
        ArrayList<Paragrafo> paragrafosEtiquetados = tokenizaArquivoEtiquetado(); // 4ª etapa  - Coloca as palavras etiquetadas em uma array classificadas por parágrafo/palavras
        ArrayList<Paragrafo> paragrafosSemStopword = removeStopword(paragrafosEtiquetados); // 5ª etapa  - Remove as palavras inúteis do texto etiquetado
        ArrayList<Paragrafo> paragrafosComRadical = stemmer(paragrafosSemStopword); // 6ª etapa  - Aplica stemmer (radical) em cada palavra
        paragrafosSaida = verboInfinitivo(paragrafosComRadical); // 7ª etapa  - Trocar verbo no radical (português) pelo verbo no infinitivo (libras)
    }

    // ???? Não estou utilizando
    // 1ª Etapa - Tokenizar o texto
    /*public void tokeniza() {
     Toker token = new Toker();
     try {
     paragrafos = token.tokenizaTexto(textoEntrada); // faz a tokenizacao do arquivo de entrada
     } catch (Exception e) {
     JOptionPane.showMessageDialog(null, "Erro " + e, "Mensagem de Erro", JOptionPane.ERROR_MESSAGE);
     }
     }*/
    // 1ª Etapa - Coloca as palavras do texto em uma array classificadas por parágrafo/palavras
    private ArrayList<Paragrafo> tokenizaArquivo() {
        ArrayList<Paragrafo> paragrafos = new ArrayList<Paragrafo>();
        Toker token = new Toker();
        // Se é para mostrar a saída da tradução em passo a passo, cria arquivo TXT
        if(isShowTranslate){
            // Cria arquivo de entrada no diretório do programa
            String arquivoEntrada = arquivo.setArquivo(textoEntrada, ++contador + ".texto_entrada.txt");
            arquivo.setArquivoEntrada(arquivoEntrada);
        }
        try {
            paragrafos = token.tokenizaArquivo(textoEntrada); // faz a tokenizacao do arquivo de entrada
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro " + e, "Mensagem de Erro", JOptionPane.ERROR_MESSAGE);
        }
        return paragrafos;
    }

    // 2ª Etapa - Preparar o texto (Acrescentar espaço após cada Token) - Entrada para o MXPOST
    private void preparaTexto(ArrayList<Paragrafo> paragrafos) {
        String textoPreparado = "";
        for (int i = 0; i < paragrafos.size(); i++) { // parágrafo
            ArrayList<String> palavras = paragrafos.get(i).getPalavras();
            for (int j = 0; j < palavras.size(); j++) { // palavras da parágrafo                                
                textoPreparado += palavras.get(j) + " ";
            }
            textoPreparado += "\n";
        }
        //cria arquivo temporario para MXPOST e grava os tokens nesse arquivo            
        String arquivoPreparado = arquivo.setArquivo(textoPreparado, ++contador + ".texto_preparado.txt");
        arquivo.setArquivoPreparado(arquivoPreparado);
    }

    // 3ª Etapa - chama o MXPOST e faz a etiquetação do arquivo de entrada para o arquivo de saída
    // *** MXPOST desenvolvido por Rachel Virgínia Xavier Aires no ICMC/USP São Carlos
    private void etiqueta() {
        String in = arquivo.getArquivoPreparado(); // Arquivo de Entrada para o MXPOST
        // Cria arquivo com o texto etiquetado no diretório do programa
        String arquivoEtiquetado = arquivo.setArquivo("", ++contador + ".texto_etiquetado.txt");
        arquivo.setArquivoEtiquetado(arquivoEtiquetado);
        String out = arquivoEtiquetado;
        try {
            System.setIn(new FileInputStream(new File(in)));
            System.setOut(new PrintStream(new File(out)));
            System.setErr(new PrintStream(new File("MXPOST_console.txt")));

            TestTagger tagger = new TestTagger(); // Etiquetador MXPOST       
            String tagset[] = {"MXPOST/port"}; //Tem que passar o tagset. Usei o diretório MSPOST/port
            tagger.main(tagset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    // 4ª etapa  - Coloca as palavras etiquetas em uma array classificadas por parágrafo/palavras
    private ArrayList<Paragrafo> tokenizaArquivoEtiquetado() {
        Toker token = new Toker();
        ArrayList<Paragrafo> paragrafosEtiquetados = new ArrayList<Paragrafo>();
        try {
            paragrafosEtiquetados = token.tokenizaArquivoEtiquetado(arquivo.getArquivoEtiquetado()); // faz a tokenizacao do arquivo de entrada
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro " + e, "Mensagem de Erro", JOptionPane.ERROR_MESSAGE);
        }
        return paragrafosEtiquetados;
    }

    // 5ª Etapa - Aplica a remoção de stopword em cada parágrafo: desenvolvido por Maria Abadia Lacerda Dias (UNIVATES)
    private ArrayList<Paragrafo> removeStopword(ArrayList<Paragrafo> paragrafosEtiquetados) {
        ArrayList<Paragrafo> paragrafosSemStopword = new ArrayList<Paragrafo>();
        String textoSemStopwords = "";
        String stopwordsRevovidas = "";
        StopwordsPortuguese stop = new StopwordsPortuguese();
        int contStop = 0;
        for (int i = 0; i < paragrafosEtiquetados.size(); i++) { // parágrafo
            // Parágrafo sem stopwords
            Paragrafo paragrafoSemStopword = new Paragrafo();
            ArrayList<String> palavras = paragrafosEtiquetados.get(i).getPalavras();
            if (!palavras.isEmpty()) {
                stopwordsRevovidas += "Stopwords removidas do parágrafo " + ++contStop + ": \n[";
            }
            for (int j = 0; j < palavras.size(); j++) { // palavras da parágrafo
                String palavraEtiquetada = palavras.get(j);
                // "da_PREP+ART", "desses_PREP+PD"
                String palavraSemEtiqueta = palavraEtiquetada.replaceAll(padraoPOS, "").trim(); // Remove as etiquetas part-of-speech
                if (!stop.isStopword(palavraSemEtiqueta)) { // Verifica se a palavra é stopword
                    textoSemStopwords += palavraSemEtiqueta + " "; // *** Trocar palavraSemEtiqueta por palavraEtiquetada sem desejar mostrar as palavras com etiquetas
                    paragrafoSemStopword.addPalavra(palavraEtiquetada);
                } else {
                    stopwordsRevovidas += palavraSemEtiqueta + "-";
                }
            }
            if (!palavras.isEmpty()) {
                stopwordsRevovidas += "]\n\n";
            }
            textoSemStopwords += "\n";
            paragrafosSemStopword.add(paragrafoSemStopword);
        }
        textoSemStopwords = substituiPontuacao(textoSemStopwords);
        // Se é para mostrar a saída da tradução em passo a passo, cria arquivo TXT
        if(isShowTranslate){                
            // Cria arquivo com texto sem as stopwords no diretório do programa        
            arquivo.setArquivo(textoSemStopwords, ++contador + ".texto_sem_stopwords.txt");
            // Cria arquivo com as stopwords que foram removidas de cada parágrafo
            arquivo.setArquivo(stopwordsRevovidas, ++contador + ".texto_stopwords_removidas.txt");
        }
        return paragrafosSemStopword;
    }

    // 6ª Etapa - Aplica remoção da raíz das palavras (stemmer) somente nos verbos: desenvolvido por Maria Abadia Lacerda Dias (UNIVATES)
    private ArrayList<Paragrafo> stemmer(ArrayList<Paragrafo> paragrafosSemStopword) {
        PortugueseStemmer radical = new PortugueseStemmer();
        ArrayList<Paragrafo> paragrafosComRadical = new ArrayList<Paragrafo>();
        String verboComRadical = "";
        String expressaoRegular = "(_VERB)";
        Pattern p = Pattern.compile(expressaoRegular);
        Matcher m;
        for (int i = 0; i < paragrafosSemStopword.size(); i++) { // parágrafo            
            Paragrafo paragrafoComRadical = new Paragrafo(); // Parágrafo sem stopwords
            ArrayList<String> palavras = paragrafosSemStopword.get(i).getPalavras();
            for (int j = 0; j < palavras.size(); j++) { // palavras da parágrafo
                String palavraEtiquetada = palavras.get(j);
                String palavraSemEtiqueta = palavraEtiquetada.replaceAll(padraoPOS, "").trim(); // Remove as etiquetas part-of-speech
                m = p.matcher(palavraEtiquetada);
                if (m.find()) { // Verifica se a palavra é verbo. Se for, aplica o stemmer                   
                    String palavraComRadical = radical.stem(palavraSemEtiqueta);
                    verboComRadical += palavraComRadical + "_VERB" + " ";
                    paragrafoComRadical.addPalavra(palavraComRadical + "_VERB");
                } else {
                    paragrafoComRadical.addPalavra(palavraEtiquetada);
                }
            }
            verboComRadical += "\n";
            paragrafosComRadical.add(paragrafoComRadical);
        }
        // Se é para mostrar a saída da tradução em passo a passo, cria arquivo TXT
        if(isShowTranslate){        
            // Cria arquivo com texto sem as stopwords e com stemmer no diretório do programa        
            arquivo.setArquivo(verboComRadical, ++contador + ".texto_verbo_com_stemmer.txt");
        }
        return paragrafosComRadical;
    }

    // 7ª Etapa - Troca os verbos com stemmer (português) para os equivalentes no infinitivo (libras)
    private ArrayList<Paragrafo> verboInfinitivo(ArrayList<Paragrafo> paragrafosComRadical) {
        ArrayList<Paragrafo> paragrafosVerboInf = new ArrayList<Paragrafo>();
        String verboInfinitivo = "";
        String expressaoRegular = "(_VERB)";
        Pattern p = Pattern.compile(expressaoRegular);
        Matcher m;
        for (int i = 0; i < paragrafosComRadical.size(); i++) { // parágrafo            
            Paragrafo paragrafoInfinitivo = new Paragrafo(); // Parágrafo sem stopwords e com stemmer
            ArrayList<String> palavras = paragrafosComRadical.get(i).getPalavras();
            for (int j = 0; j < palavras.size(); j++) { // palavras da parágrafo
                String palavraEtiquetada = palavras.get(j);
                String palavraSemEtiqueta = palavraEtiquetada.replaceAll(padraoPOS, "").trim(); // Remove as etiquetas part-of-speech
                m = p.matcher(palavraEtiquetada);
                if (m.find()) { // Verifica se a palavra é verbo. Se for, consulta no BD o verbo com o radical (português) e troca pelo seu equivalente no infinitivo (libras)                                        
                    // String verbo_libras = verboDAO.verboInfinitivo(palavraSemEtiqueta);

                    String sql = "select verbo_infinitivo from verbo where verbo_radical = "
                            + Util.simpleQuote(palavraSemEtiqueta);
                    VetorPesquisa dados = new VetorPesquisa();
                    try {
                        conexao.query(sql, dados);
                    } catch (SQLException ex) {
                        Logger.getLogger(Tagger.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Vector linhas = dados.getLinhas();
                    String verbo_libras;

                    String palavraInfinitivo;
                    // Se o resultado do banco não for vazio ([]), pega o verbo infinitivo do banco
                    if (!"[]".equals(linhas.toString())) {
                        // Pega o verbo infinitivo do banco de dados HSQLDB. A função substring exclui os colchetes
                        verbo_libras = linhas.toString().substring(2, linhas.toString().length() - 2);
                        palavraInfinitivo = verbo_libras;
                    } else {
                        palavraInfinitivo = palavraSemEtiqueta;
                    }
                    verboInfinitivo += palavraInfinitivo + "_VERB" + " ";
                    paragrafoInfinitivo.addPalavra(palavraInfinitivo + "_VERB");
                } else {
                    paragrafoInfinitivo.addPalavra(palavraEtiquetada);
                }
            }
            verboInfinitivo += "\n";
            paragrafosVerboInf.add(paragrafoInfinitivo);
        }
        // Se é para mostrar a saída da tradução em passo a passo, cria arquivo TXT
        if(isShowTranslate){        
            // Cria arquivo com texto sem as stopwords e com stemmer no diretório do programa        
            arquivo.setArquivo(verboInfinitivo, ++contador + ".texto_verbo_infinitivo.txt");
        }
        return paragrafosVerboInf;
    }

    // Retorna o texto SEM etiquetas, sem stopword e com radical nos verbos
    public String getSaida() {
        String saida = "";
        for (int i = 0; i < paragrafosSaida.size(); i++) { // parágrafo                        
            ArrayList<String> palavras = paragrafosSaida.get(i).getPalavras();
            for (int j = 0; j < palavras.size(); j++) { // palavras da parágrafo
                String palavraEtiquetada = palavras.get(j);
                String palavraSemEtiqueta = palavraEtiquetada.replaceAll(padraoPOS, "").trim(); // Remove as etiquetas part-of-speech                
                saida += palavraSemEtiqueta + " ";
            }
            saida += "\n";
        }
        return substituiPontuacao(saida);
    }

    // Retorna o texto COM etiquetas, sem stopword e com radical nos verbos
    public String getSaidaEtiquetada() {
        String saida = "";
        for (int i = 0; i < paragrafosSaida.size(); i++) { // parágrafo                        
            ArrayList<String> palavras = paragrafosSaida.get(i).getPalavras();
            for (int j = 0; j < palavras.size(); j++) { // palavras da parágrafo
                String palavraEtiquetada = palavras.get(j);
                saida += palavraEtiquetada + " "; // *** Trocar palavraSemEtiqueta por palavraEtiquetada sem desejar mostrar as palavras comtiquetas
            }
            saida += "\n";
        }
        return saida;
    }

    private String substituiPontuacao(String texto) {
        texto = texto.replaceAll("\\s\\._\\.", ".");
        texto = texto.replaceAll("\\s\\,_\\,", ",");
        texto = texto.replaceAll("\\s\\;_\\;", ";");
        texto = texto.replaceAll("\\s\\:_\\:", ":");
        texto = texto.replaceAll("\\s\\?_\\?", "?");
        texto = texto.replaceAll("\\s\\!_\\!", "!");
        texto = texto.replaceAll("\\(\\s", "(");
        texto = texto.replaceAll("\\s\\)", ")");
        return texto;
    }

    // Abre arquivo com texto etiquetado e retorna o texto
    public String getTextoEtiquetado() {
        File file = new File(arquivo.getArquivoEtiquetado());
        try {
            String texto = new Scanner(file).useDelimiter("\\A").next();
            return texto;
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Arquivo Inexistente!!!", "Erro", JOptionPane.ERROR_MESSAGE);
            return "erro";
        }
    }
}
