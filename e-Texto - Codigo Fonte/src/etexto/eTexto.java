package etexto;

import conexao.ConexaoBD;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.CannotRedoException;
import pln.Tagger;

public class eTexto extends javax.swing.JFrame {

    ConexaoBD conexao;
    boolean firstfocusdone = false;
    UndoManager undoManager = new UndoManager();
    String filepath = "";

    public eTexto() {
        initComponents();
        manualMenuItem.setVisible(false);
        manualToolBarButton.setVisible(false);
        showTranslateMenuItem.setSelected(false);
        showTranslateMenuItem.setVisible(false);        
        try {
            conexao = new ConexaoBD();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Não foi possivel conectar ao Banco de Dados"
                    + "\nTalvez já exista outra instância aberta", "e-Texto", JOptionPane.ERROR_MESSAGE);
            this.close();
        }
        jPanel1.setFocusable(true);

        this.setIconImage(getToolkit().getImage(getClass().getResource("/images/"
                + "logo.png")));
        //setExtendedState(JFrame.MAXIMIZED_BOTH);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmaSaida();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    confirmaSaida();
                }
            }
        });
    }

    public void updateButtons() {
        undoToolBarButton.setEnabled(undoManager.canUndo());
        undoMenuItem.setEnabled(undoManager.canUndo());
        redoToolBarButton.setEnabled(undoManager.canRedo());
        redoMenuItem.setEnabled(undoManager.canRedo());
    }

    public void translate() {
        if (firstfocusdone) {
            String text = inputTextArea.getText();
            //Tagger etiquetador = new Tagger(text, showTranslateMenuItem.isSelected());
            Tagger etiquetador = new Tagger(text, true);
            String textoSaida = etiquetador.getSaida();  // Pega o texto final e mostrar para o usuário
            /*else {
             textoSaida += "SAÍDA COM ETIQUETAS\n\n";
             textoSaida += etiquetador.getSaidaEtiquetada();  // Pega o texto final e mostrar para o usuário
             }*/
            outputTextArea.setText(textoSaida);
            JOptionPane.showMessageDialog(this, "Tradução realizada com sucesso!!!", "Informação", JOptionPane.INFORMATION_MESSAGE);
            inputTextArea.requestFocusInWindow();
        }
    }

    public void clear() {
        outputTextArea.setText("");
        inputTextArea.setText("");
        inputTextArea.requestFocusInWindow();
    }

    public void about() {
        new About(this).setVisible(true);
    }

    public void openFile() {
        if (saveMenuItem.isEnabled()) {
            int result = JOptionPane.showConfirmDialog(this, "Deseja salvar a tradução atual?", "e-Texto", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                saveAs();
            }
        }
        ////////////////////////////////////////////////////////////////////
        String path;
        AllFiles allFile = new AllFiles();

        JFileChooser openFile = new JFileChooser();

        File workingDirectory = new File(System.getProperty("user.dir"));
        openFile.setCurrentDirectory(workingDirectory);

        UIManager.put("FileChooser.filesOfTypeLabelText", "Arquivos do tipo:");
        SwingUtilities.updateComponentTreeUI(openFile);

        openFile.setAcceptAllFileFilterUsed(false);
        openFile.setFileFilter(allFile);
        openFile.setFileFilter(new TxtFile());
        openFile.setFileFilter(new DocFile());
        openFile.setFileFilter(new DocxFile());
        openFile.setFileFilter(allFile);

        int returnVal = openFile.showOpenDialog(this);
        ////////////////////////////////////////////////////////////////////        
        // JOptionPane.showMessageDialog(this, System.getProperty("file.encoding")); // Mostra o tipo de codificação do arquivo: UTF-8 (correto) ou cp1252
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            inputTextAreaFocusGained(null);
            path = openFile.getSelectedFile().getAbsolutePath();
            File file = new File(path);            
            try {
                BufferedReader arqIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String text = new Scanner(arqIn).useDelimiter("\\Z").next();
                inputTextArea.setText(text);
                outputTextArea.setText("");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(eTexto.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(eTexto.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ////////////////////////////////////////////////////////////////////        
    }

    public void saveAs() {
        String path;
        PrintWriter out;
        TxtFile txtFile = new TxtFile();

        JFileChooser saveFile = new JFileChooser();

        UIManager.put("FileChooser.filesOfTypeLabelText", "Salvar Como:");
        SwingUtilities.updateComponentTreeUI(saveFile);

        saveFile.setAcceptAllFileFilterUsed(false);
        saveFile.setFileFilter(txtFile);
        saveFile.setFileFilter(new DocFile());
        saveFile.setFileFilter(new DocxFile());
        saveFile.setFileFilter(txtFile);

        int returnVal = saveFile.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            int result = JOptionPane.YES_OPTION;

            FileFilter selectedFileFilter = saveFile.getFileFilter();
            String saveAs = selectedFileFilter.getDescription();

            path = saveFile.getSelectedFile().getAbsolutePath() + saveAs;
            File file = new File(path);

            if (file.exists()) {
                result = JOptionPane.showConfirmDialog(this, "Este arquivo já existe\n"
                        + "Deseja Substitui-lo?", "e-Texto", JOptionPane.YES_NO_CANCEL_OPTION);
            }
            ////////////////////////////////////////////////////////////////////
            if (result == JOptionPane.YES_OPTION) {
                try {
                    out = new PrintWriter(path);
                    String text = outputTextArea.getText();
                    String newline = System.getProperty("line.separator");
                    String replace = text.replace("\n", newline);

                    out.print(replace);
                    out.close();
                    saveMenuItem.setEnabled(false);
                    saveToolBarButton.setEnabled(false);
                    filepath = path;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(eTexto.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                saveAs();
            }
            ////////////////////////////////////////////////////////////////////
        }
    }

    public void save() {
        if ("".equals(filepath)) {
            saveAs();
        } else {
            try {
                PrintWriter out;
                out = new PrintWriter(filepath);
                String text = outputTextArea.getText();
                String newline = System.getProperty("line.separator");
                String replace = text.replace("\n", newline);

                out.print(replace);
                out.close();
                saveMenuItem.setEnabled(false);
                saveToolBarButton.setEnabled(false);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(eTexto.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addWord() {
        new AddVerb(this).setVisible(true);
    }

    public void manual() {
        // new Manual(this).setVisible(true);
    }

    public void decreaseFont() {
        if (inputTextArea.getFont().getSize() > 8) {
            inputTextArea.setFont(new java.awt.Font("Monospaced", 0, inputTextArea.getFont().getSize() - 2));
            outputTextArea.setFont(new java.awt.Font("Monospaced", 0, outputTextArea.getFont().getSize() - 2));
        }
    }

    public void increaseFont() {
        if (inputTextArea.getFont().getSize() < 44) {
            inputTextArea.setFont(new java.awt.Font("Monospaced", 0, inputTextArea.getFont().getSize() + 2));
            outputTextArea.setFont(new java.awt.Font("Monospaced", 0, outputTextArea.getFont().getSize() + 2));
        }
    }

    public void defaultFont() {
        inputTextArea.setFont(new java.awt.Font("Monospaced", 0, 14));
        outputTextArea.setFont(new java.awt.Font("Monospaced", 0, 14));
    }

    public void undo() {
        try {
            undoManager.undo();
        } catch (CannotRedoException cre) {
            cre.printStackTrace();
        }
        updateButtons();
    }

    public void redo() {
        try {
            undoManager.redo();
        } catch (CannotRedoException cre) {
            cre.printStackTrace();
        }
        updateButtons();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        cutMenuItemPopup = new javax.swing.JMenuItem();
        copyMenuItemPopup = new javax.swing.JMenuItem();
        pasteMenuItemPopup = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        clearMenuItemPopup = new javax.swing.JMenuItem();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        copyMenuItemPopup2 = new javax.swing.JMenuItem();
        clearMenuItemPopup2 = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        openToolBarButton = new javax.swing.JButton();
        saveToolBarButton = new javax.swing.JButton();
        saveAsToolBarButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        undoToolBarButton = new javax.swing.JButton();
        redoToolBarButton = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        translateToolBarButton = new javax.swing.JButton();
        clearToolBarButton = new javax.swing.JButton();
        addWordToolBarButton = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        manualToolBarButton = new javax.swing.JButton();
        aboutToolBarButton = new javax.swing.JButton();
        exitToolBarButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        inputTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jToolBar2 = new javax.swing.JToolBar();
        decreaseFontButton = new javax.swing.JButton();
        defaultFontButton = new javax.swing.JButton();
        increaseFontButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        translateButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        findMenuItem = new javax.swing.JMenuItem();
        replaceMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        fontMenuItem = new javax.swing.JMenu();
        decreaseFontMenuItem = new javax.swing.JMenuItem();
        defaultFontMenuItem = new javax.swing.JMenuItem();
        increaseFontMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        clearMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        translateMenuItem = new javax.swing.JMenuItem();
        showTranslateMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        addWordMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        manualMenuItem = new javax.swing.JMenuItem();

        cutMenuItemPopup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cut.png"))); // NOI18N
        cutMenuItemPopup.setText("Recortar");
        cutMenuItemPopup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutMenuItemPopupActionPerformed(evt);
            }
        });
        jPopupMenu1.add(cutMenuItemPopup);

        copyMenuItemPopup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/copy.png"))); // NOI18N
        copyMenuItemPopup.setText("Copiar");
        copyMenuItemPopup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemPopupActionPerformed(evt);
            }
        });
        jPopupMenu1.add(copyMenuItemPopup);

        pasteMenuItemPopup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/paste.png"))); // NOI18N
        pasteMenuItemPopup.setText("Colar");
        pasteMenuItemPopup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteMenuItemPopupActionPerformed(evt);
            }
        });
        jPopupMenu1.add(pasteMenuItemPopup);
        jPopupMenu1.add(jSeparator8);

        clearMenuItemPopup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clear.png"))); // NOI18N
        clearMenuItemPopup.setText("Limpar");
        clearMenuItemPopup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMenuItemPopupActionPerformed(evt);
            }
        });
        jPopupMenu1.add(clearMenuItemPopup);

        copyMenuItemPopup2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/copy.png"))); // NOI18N
        copyMenuItemPopup2.setText("Copiar");
        copyMenuItemPopup2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemPopup2ActionPerformed(evt);
            }
        });
        jPopupMenu2.add(copyMenuItemPopup2);

        clearMenuItemPopup2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clear.png"))); // NOI18N
        clearMenuItemPopup2.setText("Limpar");
        clearMenuItemPopup2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMenuItemPopup2ActionPerformed(evt);
            }
        });
        jPopupMenu2.add(clearMenuItemPopup2);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("e-Texto: Software Tradutor de Português para Português Sinalizado");

        jPanel1.setBackground(new java.awt.Color(225, 225, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jToolBar1.setBackground(new java.awt.Color(215, 215, 255));
        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setPreferredSize(new java.awt.Dimension(450, 41));

        openToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        openToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Open_file_toolbar.png"))); // NOI18N
        openToolBarButton.setToolTipText("Abrir (Ctrl + O)");
        openToolBarButton.setFocusable(false);
        openToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(openToolBarButton);

        saveToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        saveToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Save_file_toolbar.png"))); // NOI18N
        saveToolBarButton.setToolTipText("Salvar (Ctrl + S)");
        saveToolBarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        saveToolBarButton.setEnabled(false);
        saveToolBarButton.setFocusable(false);
        saveToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(saveToolBarButton);

        saveAsToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        saveAsToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Save_as_toolbar.png"))); // NOI18N
        saveAsToolBarButton.setToolTipText("Salvar como... (Ctrl + ?)");
        saveAsToolBarButton.setFocusable(false);
        saveAsToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveAsToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveAsToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(saveAsToolBarButton);
        jToolBar1.add(jSeparator4);

        undoToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        undoToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Undo_toolbar.png"))); // NOI18N
        undoToolBarButton.setToolTipText("Desfazer (Ctrl + Z)");
        undoToolBarButton.setEnabled(false);
        undoToolBarButton.setFocusable(false);
        undoToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        undoToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        undoToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(undoToolBarButton);

        redoToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        redoToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Redo_toolbar.png"))); // NOI18N
        redoToolBarButton.setToolTipText("Refazer (Ctrl + Y)");
        redoToolBarButton.setEnabled(false);
        redoToolBarButton.setFocusable(false);
        redoToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        redoToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        redoToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(redoToolBarButton);
        jToolBar1.add(jSeparator7);

        translateToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        translateToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/double_arrow_toolbar.png"))); // NOI18N
        translateToolBarButton.setToolTipText("Traduzir (F4)");
        translateToolBarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        translateToolBarButton.setEnabled(false);
        translateToolBarButton.setFocusable(false);
        translateToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        translateToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        translateToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translateToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(translateToolBarButton);

        clearToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        clearToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clear_toolbar.png"))); // NOI18N
        clearToolBarButton.setToolTipText("Limpar (Ctrl + L)");
        clearToolBarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        clearToolBarButton.setEnabled(false);
        clearToolBarButton.setFocusable(false);
        clearToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(clearToolBarButton);

        addWordToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        addWordToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus_toolbar.png"))); // NOI18N
        addWordToolBarButton.setToolTipText("Adicionar Verbo (F5)");
        addWordToolBarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWordToolBarButton.setFocusable(false);
        addWordToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addWordToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addWordToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(addWordToolBarButton);
        jToolBar1.add(jSeparator5);

        manualToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        manualToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/manual_toolbar.png"))); // NOI18N
        manualToolBarButton.setToolTipText("Exibir Ajuda");
        manualToolBarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        manualToolBarButton.setFocusable(false);
        manualToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        manualToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        manualToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(manualToolBarButton);

        aboutToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        aboutToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about_toolbar.png"))); // NOI18N
        aboutToolBarButton.setToolTipText("Sobre o e-Texto (F1)");
        aboutToolBarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        aboutToolBarButton.setFocusable(false);
        aboutToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        aboutToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        aboutToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(aboutToolBarButton);

        exitToolBarButton.setBackground(new java.awt.Color(215, 215, 255));
        exitToolBarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit_toolbar.png"))); // NOI18N
        exitToolBarButton.setToolTipText("Sair");
        exitToolBarButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        exitToolBarButton.setFocusable(false);
        exitToolBarButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exitToolBarButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exitToolBarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitToolBarButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(exitToolBarButton);

        inputTextArea.setColumns(20);
        inputTextArea.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        inputTextArea.setLineWrap(true);
        inputTextArea.setRows(5);
        inputTextArea.setText("Digitar texto aqui");
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setComponentPopupMenu(jPopupMenu1);
        inputTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inputTextAreaFocusGained(evt);
            }
        });
        inputTextArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                inputTextAreaUndoableEditHappened(e);
            }
        });

        inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public void removeUpdate(DocumentEvent e) {
                inputTextAreaRemoveUpdate(e);
            }

            public void insertUpdate(DocumentEvent e) {
                inputTextAreaInsertUpdate(e);
            }

            public void changedUpdate(DocumentEvent e) {

            }
        });
        jScrollPane1.setViewportView(inputTextArea);

        outputTextArea.setEditable(false);
        outputTextArea.setBackground(new java.awt.Color(240, 240, 240));
        outputTextArea.setColumns(20);
        outputTextArea.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        outputTextArea.setLineWrap(true);
        outputTextArea.setRows(5);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setComponentPopupMenu(jPopupMenu2);
        jScrollPane2.setViewportView(outputTextArea);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("Português");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setText("Português Sinalizado");

        jToolBar2.setBackground(new java.awt.Color(215, 215, 255));
        jToolBar2.setBorder(null);
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        decreaseFontButton.setBackground(new java.awt.Color(215, 215, 255));
        decreaseFontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fontminus.png"))); // NOI18N
        decreaseFontButton.setToolTipText("Diminuir Fonte (Ctrl + Shift + <)");
        decreaseFontButton.setFocusable(false);
        decreaseFontButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        decreaseFontButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        decreaseFontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decreaseFontButtonActionPerformed(evt);
            }
        });
        jToolBar2.add(decreaseFontButton);

        defaultFontButton.setBackground(new java.awt.Color(215, 215, 255));
        defaultFontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/font.png"))); // NOI18N
        defaultFontButton.setToolTipText("Tamanho Padrão (Ctrl + Espaço)");
        defaultFontButton.setFocusable(false);
        defaultFontButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        defaultFontButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        defaultFontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultFontButtonActionPerformed(evt);
            }
        });
        jToolBar2.add(defaultFontButton);

        increaseFontButton.setBackground(new java.awt.Color(215, 215, 255));
        increaseFontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fontplus.png"))); // NOI18N
        increaseFontButton.setToolTipText("Aumentar fonte (Ctrl + Shift + >)");
        increaseFontButton.setFocusable(false);
        increaseFontButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        increaseFontButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        increaseFontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                increaseFontButtonActionPerformed(evt);
            }
        });
        jToolBar2.add(increaseFontButton);

        jLabel5.setBackground(new java.awt.Color(215, 215, 255));
        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Acessibilidade");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        translateButton.setFont(new java.awt.Font("Comic Sans MS", 1, 14)); // NOI18N
        translateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/double_arrow_Button.png"))); // NOI18N
        translateButton.setText("Traduzir");
        translateButton.setToolTipText("Traduzir (F4)");
        translateButton.setEnabled(false);
        translateButton.setFocusable(false);
        translateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        translateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        translateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(translateButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(827, 827, 827)
                                .addComponent(jLabel4))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(jLabel4))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))))
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(translateButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        fileMenu.setMnemonic('A');
        fileMenu.setText("Arquivo");
        fileMenu.setToolTipText("");
        fileMenu.setBorderPainted(true);

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Open_file.png"))); // NOI18N
        openMenuItem.setMnemonic('A');
        openMenuItem.setText("Abrir");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Save_file.png"))); // NOI18N
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Salvar");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Save_as.png"))); // NOI18N
        saveAsMenuItem.setMnemonic('C');
        saveAsMenuItem.setText("Salvar como...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator2);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit.png"))); // NOI18N
        exitMenuItem.setText("Sair");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Editar");

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Undo.png"))); // NOI18N
        undoMenuItem.setMnemonic('D');
        undoMenuItem.setText("Desfazer");
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Redo.png"))); // NOI18N
        redoMenuItem.setMnemonic('R');
        redoMenuItem.setText("Refazer");
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(redoMenuItem);
        editMenu.add(jSeparator6);

        findMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        findMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/search.png"))); // NOI18N
        findMenuItem.setMnemonic('L');
        findMenuItem.setText("Localizar...");
        findMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(findMenuItem);

        replaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        replaceMenuItem.setMnemonic('S');
        replaceMenuItem.setText("Substituir...");
        replaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(replaceMenuItem);
        editMenu.add(jSeparator9);

        fontMenuItem.setMnemonic('T');
        fontMenuItem.setText("Tamanho da Fonte");
        fontMenuItem.setToolTipText("F");

        decreaseFontMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        decreaseFontMenuItem.setMnemonic('D');
        decreaseFontMenuItem.setText("Diminuir Fonte");
        decreaseFontMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decreaseFontMenuItemActionPerformed(evt);
            }
        });
        fontMenuItem.add(decreaseFontMenuItem);

        defaultFontMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        defaultFontMenuItem.setMnemonic('T');
        defaultFontMenuItem.setText("Tamanho Padrão");
        defaultFontMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultFontMenuItemActionPerformed(evt);
            }
        });
        fontMenuItem.add(defaultFontMenuItem);

        increaseFontMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PERIOD, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        increaseFontMenuItem.setMnemonic('A');
        increaseFontMenuItem.setText("Aumentar Fonte");
        increaseFontMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                increaseFontMenuItemActionPerformed(evt);
            }
        });
        fontMenuItem.add(increaseFontMenuItem);

        editMenu.add(fontMenuItem);
        editMenu.add(jSeparator10);

        clearMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        clearMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clear.png"))); // NOI18N
        clearMenuItem.setMnemonic('i');
        clearMenuItem.setText("Limpar");
        clearMenuItem.setEnabled(false);
        clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(clearMenuItem);

        jMenuBar1.add(editMenu);

        toolsMenu.setMnemonic('F');
        toolsMenu.setText("Ferramentas");

        translateMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        translateMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/double_arrow.png"))); // NOI18N
        translateMenuItem.setMnemonic('T');
        translateMenuItem.setText("Traduzir");
        translateMenuItem.setEnabled(false);
        translateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translateMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(translateMenuItem);

        showTranslateMenuItem.setSelected(true);
        showTranslateMenuItem.setText("Mostrar Tradução em Arquivo TXT");
        toolsMenu.add(showTranslateMenuItem);
        toolsMenu.add(jSeparator3);

        addWordMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        addWordMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus.png"))); // NOI18N
        addWordMenuItem.setMnemonic('A');
        addWordMenuItem.setText("Adicionar Verbo");
        addWordMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(addWordMenuItem);

        jMenuBar1.add(toolsMenu);

        helpMenu.setMnemonic('u');
        helpMenu.setText("Ajuda");

        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about.gif"))); // NOI18N
        aboutMenuItem.setMnemonic('S');
        aboutMenuItem.setText("Sobre o e-Texto");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        manualMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/manual.gif"))); // NOI18N
        manualMenuItem.setMnemonic('E');
        manualMenuItem.setText("Exibir Ajuda");
        manualMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(manualMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void translateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_translateMenuItemActionPerformed
        translate();
    }//GEN-LAST:event_translateMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        confirmaSaida();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        about();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void clearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMenuItemActionPerformed
        clear();
    }//GEN-LAST:event_clearMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        openFile();
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        save();
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void addWordMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWordMenuItemActionPerformed
        addWord();
    }//GEN-LAST:event_addWordMenuItemActionPerformed

    private void inputTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inputTextAreaFocusGained
        if (!firstfocusdone) {
            inputTextArea.setText("");
            firstfocusdone = true;
        }
    }//GEN-LAST:event_inputTextAreaFocusGained

    private void inputTextAreaUndoableEditHappened(UndoableEditEvent e) {
        if (firstfocusdone) {
            undoManager.addEdit(e.getEdit());
            updateButtons();
        }
    }

    private void inputTextAreaRemoveUpdate(DocumentEvent e) {
        if ("".equals(inputTextArea.getText())) {
            translateButton.setEnabled(false);
            translateMenuItem.setEnabled(false);
            translateToolBarButton.setEnabled(false);
        }
        if ("".equals(inputTextArea.getText()) && "".equals(outputTextArea.getText())) {
            clearMenuItem.setEnabled(false);
            clearToolBarButton.setEnabled(false);
            saveMenuItem.setEnabled(false);
            saveToolBarButton.setEnabled(false);
            /*findMenuItem.setEnabled(false);
             replaceMenuItem.setEnabled(false);
             fontMenuItem.setEnabled(false);
             decreaseFontButton.setEnabled(false);
             defaultFontButton.setEnabled(false);
             increaseFontButton.setEnabled(false);*/
        }
    }

    private void inputTextAreaInsertUpdate(DocumentEvent e) {
        translateButton.setEnabled(true);
        translateMenuItem.setEnabled(true);
        translateToolBarButton.setEnabled(true);
        clearMenuItem.setEnabled(true);
        clearToolBarButton.setEnabled(true);
        saveMenuItem.setEnabled(true);
        saveToolBarButton.setEnabled(true);
        /*findMenuItem.setEnabled(true);
         replaceMenuItem.setEnabled(true);
         fontMenuItem.setEnabled(true);
         decreaseFontButton.setEnabled(true);
         defaultFontButton.setEnabled(true);
         increaseFontButton.setEnabled(true);*/
    }

    private void exitToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitToolBarButtonActionPerformed
        confirmaSaida();
    }//GEN-LAST:event_exitToolBarButtonActionPerformed

    private void aboutToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutToolBarButtonActionPerformed
        about();
    }//GEN-LAST:event_aboutToolBarButtonActionPerformed

    private void manualToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualToolBarButtonActionPerformed
        manual();
    }//GEN-LAST:event_manualToolBarButtonActionPerformed

    private void addWordToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWordToolBarButtonActionPerformed
        addWord();
    }//GEN-LAST:event_addWordToolBarButtonActionPerformed

    private void clearToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearToolBarButtonActionPerformed
        clear();
    }//GEN-LAST:event_clearToolBarButtonActionPerformed

    private void translateToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_translateToolBarButtonActionPerformed
        translate();
    }//GEN-LAST:event_translateToolBarButtonActionPerformed

    private void saveToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveToolBarButtonActionPerformed
        save();
    }//GEN-LAST:event_saveToolBarButtonActionPerformed

    private void openToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openToolBarButtonActionPerformed
        openFile();
    }//GEN-LAST:event_openToolBarButtonActionPerformed

    private void decreaseFontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decreaseFontButtonActionPerformed
        decreaseFont();
    }//GEN-LAST:event_decreaseFontButtonActionPerformed

    private void increaseFontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_increaseFontButtonActionPerformed
        increaseFont();
    }//GEN-LAST:event_increaseFontButtonActionPerformed

    private void defaultFontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultFontButtonActionPerformed
        defaultFont();
    }//GEN-LAST:event_defaultFontButtonActionPerformed

    private void increaseFontMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_increaseFontMenuItemActionPerformed
        increaseFont();
    }//GEN-LAST:event_increaseFontMenuItemActionPerformed

    private void decreaseFontMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decreaseFontMenuItemActionPerformed
        decreaseFont();
    }//GEN-LAST:event_decreaseFontMenuItemActionPerformed

    private void defaultFontMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultFontMenuItemActionPerformed
        defaultFont();
    }//GEN-LAST:event_defaultFontMenuItemActionPerformed

    private void translateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_translateButtonActionPerformed
        translate();
    }//GEN-LAST:event_translateButtonActionPerformed

    private void undoToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoToolBarButtonActionPerformed
        undo();
    }//GEN-LAST:event_undoToolBarButtonActionPerformed

    private void redoToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoToolBarButtonActionPerformed
        redo();
    }//GEN-LAST:event_redoToolBarButtonActionPerformed

    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        undo();
    }//GEN-LAST:event_undoMenuItemActionPerformed

    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        redo();
    }//GEN-LAST:event_redoMenuItemActionPerformed

    private void manualMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualMenuItemActionPerformed
        manual();
    }//GEN-LAST:event_manualMenuItemActionPerformed

    private void findMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findMenuItemActionPerformed
        FindReplace find = new FindReplace(this, 0);
        find.setVisible(true);
    }//GEN-LAST:event_findMenuItemActionPerformed

    private void replaceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceMenuItemActionPerformed
        FindReplace replace = new FindReplace(this, 1);
        replace.setVisible(true);
    }//GEN-LAST:event_replaceMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        saveAs();
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void confirmaSaida() {
        if (outputTextArea.getText().isEmpty()) {
            // int result = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", "e-Texto", JOptionPane.YES_NO_CANCEL_OPTION);
            //if (result == JOptionPane.YES_OPTION) {
            close();
            //}
        } else if ((!inputTextArea.getText().equals("Digitar texto aqui") && !inputTextArea.getText().isEmpty()) || !outputTextArea.getText().isEmpty()) {
            if (saveMenuItem.isEnabled()) {
                int result = JOptionPane.showConfirmDialog(this, "Deseja salvar a tradução atual antes de sair?", "e-Texto", JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    saveAs();
                } else if (result == JOptionPane.NO_OPTION) {
                    close();
                }
            }
        } else {
            close();
        }
    }

    private void close() {
        try {
            conexao.close();
        } catch (SQLException ex) {
            Logger.getLogger(eTexto.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.dispose();
    }

    private void saveAsToolBarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsToolBarButtonActionPerformed
        saveAs();
    }//GEN-LAST:event_saveAsToolBarButtonActionPerformed

    private void clearMenuItemPopupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMenuItemPopupActionPerformed
        inputTextArea.setText("");
    }//GEN-LAST:event_clearMenuItemPopupActionPerformed

    private void clearMenuItemPopup2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMenuItemPopup2ActionPerformed
        outputTextArea.setText("");
    }//GEN-LAST:event_clearMenuItemPopup2ActionPerformed

    private void cutMenuItemPopupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutMenuItemPopupActionPerformed
        inputTextArea.cut();
    }//GEN-LAST:event_cutMenuItemPopupActionPerformed

    private void copyMenuItemPopupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemPopupActionPerformed
        inputTextArea.copy();
    }//GEN-LAST:event_copyMenuItemPopupActionPerformed

    private void pasteMenuItemPopupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteMenuItemPopupActionPerformed
        inputTextArea.paste();
    }//GEN-LAST:event_pasteMenuItemPopupActionPerformed

    private void copyMenuItemPopup2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemPopup2ActionPerformed
        outputTextArea.copy();
    }//GEN-LAST:event_copyMenuItemPopup2ActionPerformed

    public static void main(String args[]) {
        /* Set the Windows look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Windows (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(eTexto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(eTexto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(eTexto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(eTexto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new eTexto().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton aboutToolBarButton;
    private javax.swing.JMenuItem addWordMenuItem;
    private javax.swing.JButton addWordToolBarButton;
    private javax.swing.JMenuItem clearMenuItem;
    private javax.swing.JMenuItem clearMenuItemPopup;
    private javax.swing.JMenuItem clearMenuItemPopup2;
    private javax.swing.JButton clearToolBarButton;
    private javax.swing.JMenuItem copyMenuItemPopup;
    private javax.swing.JMenuItem copyMenuItemPopup2;
    private javax.swing.JMenuItem cutMenuItemPopup;
    private javax.swing.JButton decreaseFontButton;
    private javax.swing.JMenuItem decreaseFontMenuItem;
    private javax.swing.JButton defaultFontButton;
    private javax.swing.JMenuItem defaultFontMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton exitToolBarButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem findMenuItem;
    private javax.swing.JMenu fontMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton increaseFontButton;
    private javax.swing.JMenuItem increaseFontMenuItem;
    public javax.swing.JTextArea inputTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JMenuItem manualMenuItem;
    private javax.swing.JButton manualToolBarButton;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JButton openToolBarButton;
    public javax.swing.JTextArea outputTextArea;
    private javax.swing.JMenuItem pasteMenuItemPopup;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JButton redoToolBarButton;
    private javax.swing.JMenuItem replaceMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JButton saveAsToolBarButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton saveToolBarButton;
    private javax.swing.JCheckBoxMenuItem showTranslateMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JButton translateButton;
    private javax.swing.JMenuItem translateMenuItem;
    private javax.swing.JButton translateToolBarButton;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JButton undoToolBarButton;
    // End of variables declaration//GEN-END:variables

    class DocFile extends FileFilter {
        //Type of file that should be display in JFileChooser will be set here  
        //We choose to display only directory and text file  

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".doc");
        }
        //Set description for the type of file that should be display  

        public String getDescription() {
            return ".doc";
        }
    }

    class DocxFile extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".docx");
        }

        @Override
        public String getDescription() {
            return ".docx";
        }
    }

    class TxtFile extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return ".txt";
        }
    }

    class AllFiles extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt")
                    || f.getName().toLowerCase().endsWith(".doc") || f.getName().toLowerCase().endsWith(".docx");
        }

        @Override
        public String getDescription() {
            return "Todos documentos de texto";
        }
    }
}
