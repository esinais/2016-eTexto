package beans;

/**
 *
 * @author Pablo
 */
public class Verbo {

    private String verboRadical;
    private String verboInfinitivo;
    private int codigo;

    public Verbo(String verboRadical, String verboInfinitivo) {
        this.verboRadical = verboRadical;
        this.verboInfinitivo = verboInfinitivo;        
    }
    public Verbo(int codigo, String verboRadical, String verboInfinitivo) {
        this.codigo = codigo;
        this.verboRadical = verboRadical;
        this.verboInfinitivo = verboInfinitivo;        
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getVerboRadical() {
        return verboRadical;
    }

    public String getVerboInfinitivo() {
        return verboInfinitivo;
    }

    public int getCodigo() {
        return codigo;
    }
}
