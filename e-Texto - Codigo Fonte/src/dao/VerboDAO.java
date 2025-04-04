package dao;

import beans.Verbo;
import conexao.ConexaoBD;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import util.Util;

/**
 *
 * @author Pablo
 */
public class VerboDAO {

    private static VerboDAO instance;
    private ConexaoBD conexaoBD = ConexaoBD.getInstance();

    public VerboDAO() {
    }

    public static VerboDAO getInstance() {
        if (instance == null) {
            instance = new VerboDAO();
        }
        return instance;
    }

    public int getMaxValue() {
        int key = 0;
        PreparedStatement result;
        try {
            result = conexaoBD.getConnection().prepareStatement("SELECT MAX(COD_VERBO) FROM VERBO");
            ResultSet rs = result.executeQuery();
            if (rs.next()) {
                key = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConexaoBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return key;
    }

    public synchronized boolean existeVerbo(String verbo) {
        /*PreparedStatement stmt = conexaoBD.getConnection().prepareStatement(sql);
            stmt.setString(1, verbo.toLowerCase());
            stmt.execute();
            stmt.close();*/
        String sql = "select * from verbo where verbo_infinitivo = " + Util.simpleQuote(verbo.toLowerCase());
        try {
            Statement st = conexaoBD.getConnection().createStatement(); // statements                            
            ResultSet rs = st.executeQuery(sql); // executa Query        
            if (rs.next()) {
                rs.close();
                return true;
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return false;
    }

    public boolean insere(Verbo verbo) {
        // DEFAULT FUNCIONOU - PEGA O VALOR DEFAULT A SER INSERIDO NO BD
        // IDENTITY()+1 NÃO FUNCIONOU
        String sql = "insert into verbo (cod_verbo, verbo_radical, verbo_infinitivo) "
                + "values (DEFAULT, " + Util.simpleQuote(verbo.getVerboRadical().toLowerCase())
                + ", " + Util.simpleQuote(verbo.getVerboInfinitivo().toLowerCase()) + ")";
        return update(sql);
    }

    public boolean atualiza(Verbo verbo) {
        // UPDATE do e-Sinais com o PUBLIC.TABELA
        // conexao.update("UPDATE PUBLIC.TABELA SET ENDERECO = '" + newPath + "' WHERE PALAVRA = '" + palavra.toLowerCase() + "'");
        String sql = "update verbo set verbo_radical = " + Util.simpleQuote(verbo.getVerboRadical())
                + ", verbo_infinitivo = " + Util.simpleQuote(verbo.getVerboInfinitivo())
                + " where cod_verbo = " + verbo.getCodigo();
        return update(sql);
    }

    public boolean exclue(int id) {
        String sql = "delete from verbo where cod_verbo = " + id;
        return update(sql);
    }

    private boolean update(String sql) {
        int res = 0;
        try {
            res = conexaoBD.update(sql);
            conexaoBD.update("COMMIT");
        } catch (SQLException ex) {
            Logger.getLogger(VerboDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (res > 0) { // significa atualização bem sucedida
            return true;
        } else {
            return false;
        }
    }

    public String verboInfinitivo(String verbo_port) {
        String sql = "select verbo_infinitivo from Verbo where verbo_radical = " + Util.simpleQuote(verbo_port);
        try {
            Statement st = conexaoBD.getConnection().createStatement(); // statements                
            ResultSet rs = st.executeQuery(sql); // executa Query                
            if (rs.next()) {
                String verbo = rs.getString("verbo_infinitivo");
                rs.close();
                return verbo;
            }
        } catch (SQLException ex) {
            Logger.getLogger(VerboDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "erro"; //  não existe verbo equivalente no banco de dados
    }

    public JTable selecionaVerbo(String verbo) {
        String sql = "";
        if (verbo.equals("")) {
            sql = "select cod_verbo as ID, verbo_infinitivo as \"Verbo no Infinitivo\", verbo_radical as \"Verbo com Radical\" from Verbo order by cod_verbo";
        } else {
            sql = "select cod_verbo as ID, verbo_infinitivo as \"Verbo no Infinitivo\", verbo_radical as \"Verbo com Radical\" from Verbo "
                    + "where verbo_infinitivo like '%" + verbo + "%' order by cod_verbo";
        }
        if (!sql.isEmpty()) {
            return verboToGrid(sql);
        } else {
            return null;
        }
    }

    // ::Tabela - Carrega os verbos no grid por meio de uma consula SQL
    private synchronized JTable verboToGrid(String sql) {
        JTable grid = new JTable();
        Statement st;
        ResultSet rs = null;
        try {
            st = conexaoBD.getConnection().createStatement(); // statements        
            rs = st.executeQuery(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        grid.setCellSelectionEnabled(false);
        grid.setColumnSelectionAllowed(false);
        grid.setDragEnabled(false);
        grid.setRowSelectionAllowed(true);
        //grid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        try {
            if (rs != null) {
                ResultSetMetaData metaData = rs.getMetaData();
                int colCount = metaData.getColumnCount();
                Vector cols = new Vector();
                for (int column = 0; column < colCount; column++) {
                    cols.addElement(metaData.getColumnLabel(column + 1));
                }
                Vector lines = new Vector();
                while (rs.next()) {
                    Vector regs = new Vector();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        regs.addElement(rs.getObject(i));
                    }
                    lines.addElement(regs);
                }
                grid.setModel(new MyDefaultTableModel(lines, cols));
                return grid;
            } else {
                return grid;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return grid;
        }
    }

    // ::Tabela - Definir linha e coluna padrão no grid
    public synchronized JTable carregaTabelaToGrid() {
        JTable grid = new JTable();
        grid.setCellSelectionEnabled(false);
        grid.setColumnSelectionAllowed(false);
        grid.setDragEnabled(false);
        grid.setRowSelectionAllowed(true);
        //grid.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        try {
            Vector cols = new Vector();
            cols.addElement("ID");
            cols.addElement("Verbo no Infinitivo");
            cols.addElement("Verbo com Radical");
            Vector lines = new Vector();
            grid.setModel(new MyDefaultTableModel(lines, cols));

        } catch (Exception e) {
            e.printStackTrace();
            return grid;
        }
        return grid;
    }

    public class MyDefaultTableModel extends DefaultTableModel {

        private MyDefaultTableModel(Vector rows, Vector cols) { // constructor
            super(rows, cols);

        }

        Class[] types = new Class[]{
            Integer.class, String.class, String.class // Modificar aqui
        };

        boolean[] canEdit = new boolean[]{
            false, false, false // Modificar aqui
        };

        @Override
        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit[columnIndex];
        }

        public void setCellEditable(boolean bEditar) {
            canEdit[1] = bEditar; // Possibilitar edição da coluna 1
            canEdit[2] = bEditar; // Possibilitar edição da coluna 2            
        }
    }    
}
