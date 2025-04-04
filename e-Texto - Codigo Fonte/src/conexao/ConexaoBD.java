package conexao;

import java.sql.*;
import java.util.Vector;

public class ConexaoBD {

    private static ConexaoBD instance = null;
    private Connection con;

    public ConexaoBD() throws Exception {
        instance = this;
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:file:dados/verbos/verbos", "SA", "");
        } catch (Exception e) {
            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            throw e;
        }
    }

    public static ConexaoBD getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return con;
    }
    
    public void close() throws SQLException {
        con.close(); // encerraConexão
    }

    /**
     * Executa comandos SQL (CREATE, DROP, INSERT e UPDATE).
     */
    public synchronized int update(String expression) throws SQLException {
        Statement st = con.createStatement(); // statements        
        int res = st.executeUpdate(expression); // executa Updates
        st.close();
        return res;
    }
    
    /**
     * Realiza consultas no banco (SQL SELECT).
     */
    public synchronized void query(String expression, VetorPesquisa vetor) throws SQLException {
        Statement st = con.createStatement(); // manipula os dados obtidos numa consulta
        ResultSet rs = st.executeQuery(expression); // executa consultas; 

        armazenarDados(rs, vetor); // armazena dados em vetores

        st.close(); // fecha Statement
    }

    /**
     * Armazena resultados da consulta em vetores para o método query().
     */
    public void armazenarDados(ResultSet rs, VetorPesquisa vetor) throws SQLException {
        // vetores locais para armazenar os dados
        Vector colunas = new Vector();
        Vector linhas = new Vector();

        ResultSetMetaData rsmd = rs.getMetaData(); // Armazena informações sobre o banco de dados e a consulta

        // Construção dos cabeçalhos das colunas, quantas existirem.
        for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
            colunas.addElement(rsmd.getColumnName(i));
        }

        // Construção das linhas
        for (; rs.next();) { // posiciona no primeiro registro e percorre até não encontrar registros
            linhas.addElement(proximaLinha(rs, rsmd));
        }

        // Seta os vetores da classe VetorPesquisa
        vetor.setColunas(colunas);
        vetor.setLinhas(linhas);

    }

    /**
     * Este método tem por finalidade percorrer a tabela, usando as informações
     * obtidas no loop anterior. Além de verificar o tipo de elemento que há na
     * linha da tabela.
     *
     * @param rs
     * @param rsmd
     * @return
     */
    private Vector proximaLinha(ResultSet rs, ResultSetMetaData rsmd) {
        Vector LinhaAtual = new Vector();

        // Verifica o tipo de elemento a ser adicionado no vetor
        try {
            for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                switch (rsmd.getColumnType(i)) {
                    case Types.VARCHAR:
                        LinhaAtual.addElement(rs.getString(i));
                        break;

                    case Types.TIMESTAMP:
                        LinhaAtual.addElement(rs.getDate(i));
                        break;

                    case Types.NUMERIC:
                        LinhaAtual.addElement(new Long(rs.getLong(i)));
                        break;

                    case Types.INTEGER:
                        LinhaAtual.addElement(new Integer(rs.getInt(i)));
                        break;

                    case Types.REAL:
                        LinhaAtual.addElement(rs.getFloat(i));
                        break;
                }
            }
        } catch (SQLException e) {
        }
        return LinhaAtual;
    } 
} 
