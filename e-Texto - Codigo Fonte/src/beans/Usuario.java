package beans;

/**
 *
 * @author Pablo
 */
public class Usuario {

    private String login;
    private String senha;

    public Usuario(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }

    public String getSenha() {
        return senha;
    }

    public String getLogin() {
        return login;
    }
}
