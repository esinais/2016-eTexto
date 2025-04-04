package util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

/**
 *
 * @author Pablo
 */
public class Arquivo {

    private String diretorio = System.getProperty("user.dir");
    private String arquivoEntrada;
    private String arquivoPreparado;
    private String arquivoEtiquetado;
    private static Arquivo instance;

    public Arquivo() {
        instance = this;
    }

    public static Arquivo getInstance() {
        if (instance == null) {
            instance = new Arquivo();
        }
        return instance;
    }

    public String setArquivo(String texto, String nomeArquivo) {
        String pasta = diretorio + "\\out\\";
        File file = new File(pasta); // ajfilho é uma pasta!
        if (!file.exists()) {
            file.mkdir(); //mkdir() cria somente um diretório, mkdirs() cria diretórios e subdiretórios.
        }        
        String arquivo = pasta + nomeArquivo;
        try {
            PrintWriter out = new PrintWriter(new FileWriter(arquivo));
            out.print(texto);
            out.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro " + e, "Mensagem de Erro", JOptionPane.ERROR_MESSAGE);
        }
        return arquivo;
    }

    public void setArquivoPreparado(String arquivoPreparado) {
        this.arquivoPreparado = arquivoPreparado;
    }

    public String getArquivoPreparado() {
        return arquivoPreparado;
    }

    public void setArquivoEntrada(String arquivoEntrada) {
        this.arquivoEntrada = arquivoEntrada;
    }

    public String getArquivoEntrada() {
        return arquivoEntrada;
    }

    public void setArquivoEtiquetado(String arquivoEtiquetado) {
        this.arquivoEtiquetado = arquivoEtiquetado;
    }

    public String getArquivoEtiquetado() {
        return arquivoEtiquetado;
    }

    public String getDiretorio() {
        return diretorio;
    }
}
