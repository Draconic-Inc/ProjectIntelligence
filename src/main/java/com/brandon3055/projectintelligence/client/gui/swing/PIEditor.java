/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brandon3055.projectintelligence.client.gui.swing;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.ProcessHandlerClient;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.projectintelligence.client.DisplayController;
import com.brandon3055.projectintelligence.client.PIGuiHelper;
import com.brandon3055.projectintelligence.client.gui.ContentInfo;
import com.brandon3055.projectintelligence.client.gui.GuiContentSelect;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.docmanagement.*;
import com.brandon3055.projectintelligence.docmanagement.LanguageManager.PageLangData;
import com.brandon3055.projectintelligence.utils.LogHelper;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.brandon3055.projectintelligence.client.gui.ContentInfo.ContentType.*;
import static com.brandon3055.projectintelligence.client.gui.GuiContentSelect.SelectMode.*;

/**
 * @author brand
 */
public class PIEditor extends javax.swing.JFrame {

    private static String os = System.getProperty("os.name");
    private static Pattern versionValidator = Pattern.compile("^[\\d\\.]+$");
    private String selectedPageURI = "";
    private DefaultTreeModel treeModel;
    private DefaultListModel<ContentInfo> iconListModel = new DefaultListModel<>();
    private DefaultListModel<ContentRelation> relationListModel = new DefaultListModel<>();
    private DefaultListModel<String> aliasListModel = new DefaultListModel<>();
    private boolean reloading = false;
    private UndoManager undo = new UndoManager();
    private static Map<String, Integer> caretPositions = new HashMap<>();

    /**
     * Creates new form PIEditor
     */
    public PIEditor() {
        URL icon = PIEditor.class.getResource("/assets/projectintelligence/textures/editor_icon.png");
        if (icon != null) {
            setIconImage(Toolkit.getDefaultToolkit().createImage(icon));
        }

        initComponents();
        generateLineNumbers();
//        addContextMenuItems();
        pack();

        markdownWindow.getDocument().addUndoableEditListener(e -> undo.addEdit(e.getEdit()));
        markdownWindow.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == 90) {
                    if (e.isControlDown() && e.isShiftDown()) {
                        redo();
                    }
                    else if (e.isControlDown()) {
                        undo();
                    }
                }
            }
        });

        markdownWindow.setTabSize(2);

        LanguageManager.ALL_LANGUAGES.forEach(matchLangBox::addItem);
        matchLangBox.addItem("disabled");
        matchLangBox.setSelectedItem("disabled");

        iconList.setModel(iconListModel);
        relationList.setModel(relationListModel);
        aliasList.setModel(aliasListModel);
        DefaultTreeSelectionModel selectModel = new DefaultTreeSelectionModel();
        selectModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        pageTree.setSelectionModel(selectModel);

        markdownWindow.setWrapStyleWord(true);
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Editor Line Wrap");
        item.setSelected(PIConfig.editorLineWrap);
        item.addActionListener(e -> {
            PIConfig.editorLineWrap = item.isSelected();
            markdownWindow.setLineWrap(PIConfig.editorLineWrap);
            PIConfig.save();
        });
        jMenu3.add(item);

        loadLAFOps();

        reload();
    }

    private void loadLAFOps() {
        loadLAF(PIConfig.editorLAF, false);
        JMenu lafs = new JMenu("Editor Style");
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            JMenuItem item = new JMenuItem(info.getName());
            item.addActionListener(e -> loadLAF(info.getClassName(), true));
            lafs.add(item);
        }

        try {
            Class.forName("com.bulenkov.darcula.DarculaLaf");
            JMenuItem item = new JMenuItem("Darcula");
            item.addActionListener(e -> loadLAF("com.bulenkov.darcula.DarculaLaf", true));
            lafs.add(item);
        }
        catch (ClassNotFoundException ignored) {
        }


        jMenu3.add(lafs);
    }

    private void loadLAF(String lafClass, boolean save) {
        try {
            if (lafClass.isEmpty()) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                return;
            }
            UIManager.setLookAndFeel(lafClass);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Throwable e) {
            LogHelper.error("Failed to load look and feel: " + lafClass);
            e.printStackTrace();
            return;
        }

        if (save) {
            PIConfig.editorLAF = lafClass;
            ProcessHandlerClient.syncTask(PIConfig::save);
        }
    }

    private void undo() {
        try {
            if (undo.canUndo()) {
                undo.undo();
            }
        }
        catch (CannotUndoException ignored) {
        }
    }

    private void redo() {
        try {
            if (undo.canRedo()) {
                undo.redo();
            }
        }
        catch (CannotUndoException ignored) {
        }
    }

    @Override
    public void toBack() {
        if (os.equals("Linux")) return;
        super.toBack();
    }

    private void generateLineNumbers() {
        markdownWindow.setMargin(new Insets(0, 4, 0, 0));
        mdScrollPane.setRowHeaderView(new TextLineNumber(markdownWindow));
        mdScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void initComponents() {

        treeContextMenu = new JPopupMenu();
        mdContextMenu = new JPopupMenu();
        jPanel3 = new JPanel();
        jToolBar1 = new JToolBar();
        newModButton = new JButton();
        jSplitPane1 = new JSplitPane();
        mdScrollPane = new JScrollPane();
        markdownWindow = new JTextArea();
        jScrollPane6 = new JScrollPane();
        pageTree = new JTree();
        newPageButton = new JButton();
        jButton5 = new JButton();
        jButton2 = new JButton();
        jButton4 = new JButton();
        jButton11 = new JButton();
        jButton3 = new JButton();
        jButton1 = new JButton();
        jLabel1 = new JLabel();
        jButton13 = new JButton();
        jButton12 = new JButton();
        jButton6 = new JButton();
        tabbedPain = new JTabbedPane();
        modPanel = new JPanel();
        nameLabel1 = new JLabel();
        modNameField = new JTextField();
        JButton nameHelp1 = new JButton();
        JButton nameHelp2 = new JButton();
        modIdField = new JTextField();
        nameLabel2 = new JLabel();
        JButton nameHelp3 = new JButton();
        nameLabel3 = new JLabel();
        modVersionSelect = new JComboBox<>();
        createFromExistingModButton = new JButton();
        copyDocFromSelected = new JCheckBox();
        jSeparator1 = new JSeparator();
        jSeparator3 = new JSeparator();
        jButton10 = new JButton();
        jScrollPane5 = new JScrollPane();
        aliasList = new JList<>();
        JButton relationsHelp1 = new JButton();
        relationsLabel1 = new JLabel();
        JButton addRelation1 = new JButton();
        JButton editRelation1 = new JButton();
        JButton removeRelation1 = new JButton();
        pagePanel = new JPanel();
        nameLabel = new JLabel();
        nameField = new JTextField();
        idLabel = new JLabel();
        idField = new JTextField();
        weightLabel = new JLabel();
        JButton nameHelp = new JButton();
        JButton idHelp = new JButton();
        JButton weightHelp = new JButton();
        revisionLabel = new JLabel();
        JButton revHelp = new JButton();
        toggleHidden = new JCheckBox();
        jScrollPane3 = new JScrollPane();
        iconList = new JList<>();
        iconsLabel = new JLabel();
        JButton iconsHelp = new JButton();
        cycleIcons = new JCheckBox();
        jSeparator2 = new JSeparator();
        targetEnRev = new JLabel();
        JButton addIcon = new JButton();
        JButton removeIcon = new JButton();
        JButton editIcon = new JButton();
        relationsLabel = new JLabel();
        JButton relationsHelp = new JButton();
        jScrollPane4 = new JScrollPane();
        relationList = new JList<>();
        JButton removeRelation = new JButton();
        JButton editRelation = new JButton();
        JButton addRelation = new JButton();
        jSeparator4 = new JSeparator();
        weightField = new JSpinner();
        updateEnRev = new JButton();
        revisionField = new JSpinner();
        enRevField = new JSpinner();
        JButton revHelp1 = new JButton();
        jButton7 = new JButton();
        jButton8 = new JButton();
        targetEnRev1 = new JLabel();
        matchLangBox = new JComboBox<>();
        jMenuBar1 = new JMenuBar();
        jMenu1 = new JMenu();
        jMenu2 = new JMenu();
        jMenu3 = new JMenu();
        alwaysOnTop = new JCheckBoxMenuItem();
        changeLangButton = new JMenuItem();

        treeContextMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent evt) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
                treeMenuClose(evt);
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
                treeMenuOpen(evt);
            }
        });

        mdContextMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent evt) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
                mdMenuClose(evt);
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
                treeMenuOpen(evt);
                mdMenuOpen(evt);
            }
        });

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Project Intelligence Editor");

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        newModButton.setText("New Mod");
        newModButton.setToolTipText("Add documentation for a new mod.");
        newModButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newModAction(evt);
            }
        });

        jSplitPane1.setDividerLocation(200);

        mdScrollPane.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent evt) {
                mdScroll(evt);
            }
        });

        markdownWindow.setColumns(20);
        markdownWindow.setRows(5);
        markdownWindow.setComponentPopupMenu(mdContextMenu);
        markdownWindow.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                mdTextChange(evt);
            }
        });
        mdScrollPane.setViewportView(markdownWindow);

        jSplitPane1.setRightComponent(mdScrollPane);

        DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("JTree");
        DefaultMutableTreeNode treeNode2 = new DefaultMutableTreeNode("colors");
        DefaultMutableTreeNode treeNode3 = new DefaultMutableTreeNode("blue");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("violet");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("red");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("yellow");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new DefaultMutableTreeNode("sports");
        treeNode3 = new DefaultMutableTreeNode("basketball");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("soccer");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("football");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("hockey");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new DefaultMutableTreeNode("food");
        treeNode3 = new DefaultMutableTreeNode("hot dogs");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("pizza");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("ravioli");
        treeNode2.add(treeNode3);
        treeNode3 = new DefaultMutableTreeNode("banan");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        pageTree.setModel(new DefaultTreeModel(treeNode1));
        pageTree.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        pageTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                pageTreeMouseClicked(evt);
            }
        });
        pageTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent evt) {
                pageSelected(evt);
            }
        });
        jScrollPane6.setViewportView(pageTree);

        jSplitPane1.setLeftComponent(jScrollPane6);

        newPageButton.setText("New Page");
        newPageButton.setToolTipText("Add a new sub page to the selected page.");
        newPageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newPageAction(evt);
            }
        });

        jButton5.setText("New Local Doc");
        jButton5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newLocalDoc(evt);
            }
        });

        jButton2.setText("Image");
        jButton2.setActionCommand("image");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });

        jButton4.setText("Entity");
        jButton4.setActionCommand("entity");
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });

        jButton11.setText("Rule");
        jButton11.setActionCommand("rule");
        jButton11.setFocusable(false);
        jButton11.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton11.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton11.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });

        jButton3.setText("Link");
        jButton3.setActionCommand("link");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });

        jButton1.setText("Stack");
        jButton1.setActionCommand("stack");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });

        jLabel1.setText("Insert:");

        jButton13.setText("Recipe");
        jButton13.setActionCommand("recipe");
        jButton13.setFocusable(false);
        jButton13.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton13.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton13.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });

        jButton12.setText("Table");
        jButton12.setActionCommand("table");
        jButton12.setFocusable(false);
        jButton12.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton12.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton12.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });

        jButton6.setText("Open MD Reference");
        jButton6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                openMDRef(evt);
            }
        });

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSplitPane1).addGroup(jPanel3Layout.createSequentialGroup().addComponent(newModButton, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(newPageButton, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(jButton5).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jButton6).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jToolBar1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(41, 41, 41).addComponent(jLabel1).addComponent(jButton1).addComponent(jButton13).addComponent(jButton2).addComponent(jButton3).addComponent(jButton4).addComponent(jButton11).addComponent(jButton12).addContainerGap()))));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(newModButton).addComponent(newPageButton).addComponent(jButton5).addComponent(jButton6)).addComponent(jToolBar1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE).addGroup(jPanel3Layout.createSequentialGroup().addGap(2, 2, 2).addComponent(jLabel1)).addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(jButton13, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(jButton3, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(jButton4, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(jButton11, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(jButton12, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE).addContainerGap()));

        modPanel.setToolTipText("");

        nameLabel1.setText("Mod Name:");
        nameLabel1.setToolTipText("");

        modNameField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                modNameChange(evt);
            }
        });

        nameHelp1.setText("?");
        nameHelp1.setToolTipText("Click for more info about this field.");
        nameHelp1.setActionCommand("modName");
        nameHelp1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        nameHelp1.setMargin(new Insets(0, 4, 0, 4));
        nameHelp1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        nameHelp2.setText("?");
        nameHelp2.setToolTipText("Click for more info about this field.");
        nameHelp2.setActionCommand("modID");
        nameHelp2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        nameHelp2.setMargin(new Insets(0, 4, 0, 4));
        nameHelp2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        modIdField.setEditable(false);
        modIdField.setToolTipText("Mod id can not be chnaged. If the mods id has changed then add an alias for the new id.");

        nameLabel2.setText("Mod ID:");
        nameLabel2.setToolTipText("");

        nameHelp3.setText("?");
        nameHelp3.setToolTipText("Click for more info about the version system.");
        nameHelp3.setActionCommand("versionHelp");
        nameHelp3.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        nameHelp3.setMargin(new Insets(0, 4, 0, 4));
        nameHelp3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        nameLabel3.setText("Target Min Mod Version:");
        nameLabel3.setToolTipText("");

        modVersionSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                versionAction(evt);
            }
        });

        createFromExistingModButton.setText("Create documentation for new mod version");
        createFromExistingModButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newVersionAction(evt);
            }
        });

        copyDocFromSelected.setSelected(true);
        copyDocFromSelected.setText("Copy documentation from selected target version");

        jSeparator3.setOrientation(SwingConstants.VERTICAL);

        jButton10.setForeground(new Color(250, 90, 90));
        jButton10.setText("Delete Mod Documentation");
        jButton10.setToolTipText("Deletes this mod's documentation. If you are someone updating a mods documentation you should never need to touch this button. ");
        jButton10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteModAction(evt);
            }
        });

        aliasList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(aliasList);

        relationsHelp1.setText("?");
        relationsHelp1.setToolTipText("Click for more info about this field.");
        relationsHelp1.setActionCommand("aliases");
        relationsHelp1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        relationsHelp1.setMargin(new Insets(0, 4, 0, 4));
        relationsHelp1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        relationsLabel1.setText("Mod Aliases");

        addRelation1.setText("+");
        addRelation1.setToolTipText("Add Alias");
        addRelation1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        addRelation1.setMargin(new Insets(0, 4, 0, 4));
        addRelation1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addAlias(evt);
            }
        });

        editRelation1.setText("e");
        editRelation1.setToolTipText("Edit Selected Alias");
        editRelation1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        editRelation1.setMargin(new Insets(0, 4, 0, 4));
        editRelation1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editAlias(evt);
            }
        });

        removeRelation1.setText("x");
        removeRelation1.setToolTipText("Remove Selected Alias");
        removeRelation1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        removeRelation1.setMargin(new Insets(0, 4, 0, 4));
        removeRelation1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeAlias(evt);
            }
        });

        GroupLayout modPanelLayout = new GroupLayout(modPanel);
        modPanel.setLayout(modPanelLayout);
        modPanelLayout.setHorizontalGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addContainerGap().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(nameLabel1).addComponent(nameLabel2)).addGap(44, 44, 44).addComponent(modNameField, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(nameHelp1)).addGroup(modPanelLayout.createSequentialGroup().addGap(113, 113, 113).addComponent(modIdField, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(nameHelp2)).addGroup(modPanelLayout.createSequentialGroup().addContainerGap().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(createFromExistingModButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(copyDocFromSelected).addGroup(modPanelLayout.createSequentialGroup().addComponent(nameLabel3).addGap(18, 18, 18).addComponent(modVersionSelect, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)).addComponent(jSeparator1)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(nameHelp3))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(modPanelLayout.createSequentialGroup().addComponent(addRelation1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(editRelation1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(removeRelation1)).addGroup(GroupLayout.Alignment.LEADING, modPanelLayout.createSequentialGroup().addComponent(relationsLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(relationsHelp1)).addComponent(jScrollPane5, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 292, Short.MAX_VALUE).addComponent(jButton10).addContainerGap()));
        modPanelLayout.setVerticalGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addContainerGap().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, modPanelLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jButton10)).addComponent(jSeparator3).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(relationsLabel1).addComponent(relationsHelp1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(removeRelation1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(editRelation1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(addRelation1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel1).addComponent(modNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(nameHelp1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel2).addComponent(modIdField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(nameHelp2, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel3).addComponent(modVersionSelect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(1, 1, 1).addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(4, 4, 4).addComponent(createFromExistingModButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(copyDocFromSelected)).addComponent(nameHelp3, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)))).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));

        tabbedPain.addTab("Mod", modPanel);

        nameLabel.setText("Name:");
        nameLabel.setToolTipText("");

        nameField.setToolTipText("The name of the page or the mod if this is the root page");
        nameField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                nameChange(evt);
            }
        });

        idLabel.setText("Page ID:");

        idField.setEditable(false);
        idField.setToolTipText("This the the unique (within the context of the parant page) id for this page. This should be based on the page content e.g Draconium Ore would use the id draconiumOre");
        idField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                idChange(evt);
            }
        });

        weightLabel.setText("Sorting Weight:");

        nameHelp.setText("?");
        nameHelp.setToolTipText("Click for more info about this field.");
        nameHelp.setActionCommand("name");
        nameHelp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        nameHelp.setMargin(new Insets(0, 4, 0, 4));
        nameHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        idHelp.setText("?");
        idHelp.setToolTipText("Click for more info about this field.");
        idHelp.setActionCommand("id");
        idHelp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        idHelp.setMargin(new Insets(0, 4, 0, 4));
        idHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        weightHelp.setText("?");
        weightHelp.setToolTipText("Click for more info about this field.");
        weightHelp.setActionCommand("weight");
        weightHelp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        weightHelp.setMargin(new Insets(0, 4, 0, 4));
        weightHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        revisionLabel.setText("Revision:");

        revHelp.setText("?");
        revHelp.setToolTipText("Click for more info about this field.");
        revHelp.setActionCommand("revision");
        revHelp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        revHelp.setMargin(new Insets(0, 4, 0, 4));
        revHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        toggleHidden.setText("Hidden");
        toggleHidden.setToolTipText("Hidden pages do not show in the page list (Except when in etit mode) but can be accessed via links.");
        toggleHidden.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                toggleHidden(evt);
            }
        });

        iconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(iconList);

        iconsLabel.setText("Icon(s):");

        iconsHelp.setText("?");
        iconsHelp.setToolTipText("Click for more info about this field.");
        iconsHelp.setActionCommand("icons");
        iconsHelp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        iconsHelp.setMargin(new Insets(0, 4, 0, 4));
        iconsHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        cycleIcons.setText("Cycle Icons");
        cycleIcons.setToolTipText("");
        cycleIcons.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cycleIcons(evt);
            }
        });

        jSeparator2.setOrientation(SwingConstants.VERTICAL);

        targetEnRev.setText("Matches Rev:");

        addIcon.setText("+");
        addIcon.setToolTipText("Add icon");
        addIcon.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        addIcon.setMargin(new Insets(0, 4, 0, 4));
        addIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addIcon(evt);
            }
        });

        removeIcon.setText("x");
        removeIcon.setToolTipText("Remove Selected Icon");
        removeIcon.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        removeIcon.setMargin(new Insets(0, 4, 0, 4));
        removeIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeIcon(evt);
            }
        });

        editIcon.setText("e");
        editIcon.setToolTipText("Edit Selected Icon");
        editIcon.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        editIcon.setMargin(new Insets(0, 4, 0, 4));
        editIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editIcon(evt);
            }
        });

        relationsLabel.setText("Content Relations:");

        relationsHelp.setText("?");
        relationsHelp.setToolTipText("Click for more info about this field.");
        relationsHelp.setActionCommand("relations");
        relationsHelp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        relationsHelp.setMargin(new Insets(0, 4, 0, 4));
        relationsHelp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        relationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(relationList);

        removeRelation.setText("x");
        removeRelation.setToolTipText("Remove Selected Relation");
        removeRelation.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        removeRelation.setMargin(new Insets(0, 4, 0, 4));
        removeRelation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeRelation(evt);
            }
        });

        editRelation.setText("e");
        editRelation.setToolTipText("Edit Selected Relation");
        editRelation.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        editRelation.setMargin(new Insets(0, 4, 0, 4));
        editRelation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editRelation(evt);
            }
        });

        addRelation.setText("+");
        addRelation.setToolTipText("Add Relation");
        addRelation.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        addRelation.setMargin(new Insets(0, 4, 0, 4));
        addRelation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addRelation(evt);
            }
        });

        jSeparator4.setOrientation(SwingConstants.VERTICAL);

        weightField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                weightChanged(evt);
            }
        });

        updateEnRev.setText("Update to latest");
        updateEnRev.setToolTipText("Sets the target English Revision to the current english revision of this page.");
        updateEnRev.setActionCommand("revision");
        updateEnRev.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        updateEnRev.setMargin(new Insets(0, 4, 0, 4));
        updateEnRev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateMatchRev(evt);
            }
        });

        revisionField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                revisionChange(evt);
            }
        });

        enRevField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                matchRevChange(evt);
            }
        });

        revHelp1.setText("<html>Lang<br>Help</html>");
        revHelp1.setToolTipText("Click for more info about this field.");
        revHelp1.setActionCommand("matchRev");
        revHelp1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        revHelp1.setMargin(new Insets(0, 4, 0, 4));
        revHelp1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                helpAction(evt);
            }
        });

        jButton7.setText("Delete Page");
        jButton7.setToolTipText("");
        jButton7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deletePage(evt);
            }
        });

        jButton8.setText("<html>Import page content from alternate language</html>");
        jButton8.setToolTipText("");
        jButton8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                importContentFromLang(evt);
            }
        });

        targetEnRev1.setText("Matches Lang:");

        matchLangBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                matchLangChange(evt);
            }
        });

        GroupLayout pagePanelLayout = new GroupLayout(pagePanel);
        pagePanel.setLayout(pagePanelLayout);
        pagePanelLayout.setHorizontalGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addContainerGap().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(idLabel).addComponent(weightLabel).addComponent(nameLabel)).addGap(30, 30, 30)).addGroup(pagePanelLayout.createSequentialGroup().addComponent(revisionLabel).addGap(66, 66, 66))).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(targetEnRev).addComponent(targetEnRev1)).addGap(36, 36, 36))).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(idField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE).addComponent(nameField, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)).addGroup(GroupLayout.Alignment.TRAILING, pagePanelLayout.createSequentialGroup().addComponent(weightField, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(weightHelp).addGap(123, 123, 123))).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(nameHelp).addComponent(idHelp))).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(revisionField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE).addComponent(enRevField, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(revHelp).addComponent(updateEnRev))).addComponent(matchLangBox, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(revHelp1))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE).addGroup(pagePanelLayout.createSequentialGroup().addComponent(cycleIcons).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE).addComponent(addIcon).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(editIcon).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(removeIcon)).addGroup(pagePanelLayout.createSequentialGroup().addComponent(iconsLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(iconsHelp))).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(pagePanelLayout.createSequentialGroup().addComponent(addRelation).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(editRelation).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(removeRelation)).addGroup(GroupLayout.Alignment.LEADING, pagePanelLayout.createSequentialGroup().addComponent(relationsLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(relationsHelp)).addComponent(jScrollPane4, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(jButton8, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE).addComponent(toggleHidden).addComponent(jButton7, GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)).addContainerGap()));
        pagePanelLayout.setVerticalGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(pagePanelLayout.createSequentialGroup().addGap(4, 4, 4).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel).addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(nameHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(iconsHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(iconsLabel).addComponent(relationsLabel).addComponent(relationsHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(cycleIcons).addComponent(removeIcon, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(editIcon, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(addIcon, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))).addGroup(pagePanelLayout.createSequentialGroup().addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(removeRelation, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(editRelation, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(addRelation, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))).addGap(0, 0, Short.MAX_VALUE)).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(idField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(idHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(weightLabel).addComponent(weightHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(weightField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(revisionLabel).addComponent(revHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(revisionField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(enRevField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(targetEnRev).addComponent(updateEnRev, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(matchLangBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(targetEnRev1))).addGroup(pagePanelLayout.createSequentialGroup().addGap(1, 1, 1).addComponent(revHelp1)))))).addGroup(pagePanelLayout.createSequentialGroup().addContainerGap().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSeparator2).addComponent(jSeparator4).addGroup(pagePanelLayout.createSequentialGroup().addComponent(toggleHidden).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jButton8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton7, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))))).addContainerGap()));

        tabbedPain.addTab("Page Properties", pagePanel);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenu2.addMenuListener(new MenuListener() {
            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                editMenuSelect(evt);
            }
        });
        jMenuBar1.add(jMenu2);

        jMenu3.setText("Options");

        alwaysOnTop.setSelected(true);
        alwaysOnTop.setText("Always on top");
        alwaysOnTop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aotAction(evt);
            }
        });
        jMenu3.add(alwaysOnTop);

        changeLangButton.setText("Change Language");
        changeLangButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                changeLangAction(evt);
            }
        });
        jMenu3.add(changeLangButton);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(6, 6, 6).addComponent(tabbedPain)).addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(tabbedPain, GroupLayout.PREFERRED_SIZE, 212, GroupLayout.PREFERRED_SIZE).addGap(8, 8, 8)));

        setBounds(0, 0, 1222, 755);
    }

    public void reload() {
        reloading = true;
        alwaysOnTop.setSelected(PIConfig.editorAlwaysOnTop);
        setAlwaysOnTop(PIConfig.editorAlwaysOnTop);
        markdownWindow.setLineWrap(PIConfig.editorLineWrap);
        updateTree();
        reloading = false;
    }

    private void updateTree() {
        //Create a list of all currently expanded pages.

        java.util.List<String> expanded = streamTree(pageTree)//
                .filter(pageTree::isExpanded)//
                .map(pageTree::getPathForRow)//
                .map(TreePath::getLastPathComponent)//
                .map(Object::toString)//
                .collect(Collectors.toList());

        DocumentationPage selectedPage = getSelected();
        Singleton<Boolean> keepSelection = new Singleton<>(false);

        //Actually reload the tree
        DefaultMutableTreeNode modsNode = new DefaultMutableTreeNode("Root Page");
        pageTree.setModel(treeModel = new DefaultTreeModel(modsNode));
        treeModel.setAsksAllowsChildren(true);

        Map<String, ModStructurePage> structureMap = DocumentationManager.getModStructureMap();
        for (String modid : structureMap.keySet()) {
            modsNode.add(loadModPages(structureMap.get(modid)));
        }

        treeModel.reload();

        //Reset the tree state to what it was before the reload
        streamTree(pageTree).forEach(pageTree::expandRow);

        if (selectedPage != null) {
            streamTree(pageTree, true).forEachOrdered(row -> {
                Object component = pageTree.getPathForRow(row).getLastPathComponent();
                if (component instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) component).getUserObject() == selectedPage) {
                    keepSelection.set(true);
                    pageTree.setSelectionRow(row);
                }
            });
        }
        streamTree(pageTree, true).forEachOrdered(row -> {
            try {
                String node = pageTree.getPathForRow(row).getLastPathComponent().toString();
                if (!expanded.contains(node) && !node.equals("Root Page")) {
                    pageTree.collapseRow(row);
                }
            }
            catch (Throwable ignored) {
            }
        });

        if (selectedPage != null && keepSelection.get()) {
            setSelectedPage(selectedPage);
        }
        else {
            setSelectedPage(null);
        }
    }

    private DefaultMutableTreeNode loadModPages(DocumentationPage page) {
        DefaultMutableTreeNode pageNode = new DefaultMutableTreeNode(new TreePageContainer(page));
        List<DocumentationPage> pages = new ArrayList<>(page.getSubPages());
        pages.sort(Comparator.comparingInt(DocumentationPage::getSortingWeight));
        for (DocumentationPage subPage : pages) {
            pageNode.add(loadModPages(subPage));
        }

        return pageNode;
    }

    //region Field Listeners

    //Mod Fields

    private void modNameChange(KeyEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null) {
            ModStructurePage sp = DocumentationManager.getModPage(page.getModid());
            if (sp != null) {
                LanguageManager.setPageName(sp.getModid(), sp.getPageURI(), modNameField.getText(), LanguageManager.getUserLanguage());
                pageTree.updateUI();
            }
        }
    }

    private void versionAction(ActionEvent evt) {
        String newVersion = (String) modVersionSelect.getSelectedItem();
        if (newVersion == null || reloading) return;
        DocumentationPage page = getSelected();
        if (page != null) {
            String selected = page.getPageURI();
            if (newVersion.equals("[Default-Best-Match]")) {
                DocumentationManager.setModVersionOverride(page.getModid(), null);
            }
            else {
                DocumentationManager.setModVersionOverride(page.getModid(), newVersion);
            }
            setSelectedPage(DocumentationManager.getPage(selected));
        }
    }

    private void newVersionAction(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }
//        copyDocFromSelected
        String info = "Whenever a mod releases a new version that changes existing content or\n" + //
                "adds new content you must create a set of documentation for the new version.\n" + //
                "The way this normally works is you create a copy of the existing doc for the new version\n" + //
                "and then just edit/add documentation as needed for the new version.\n\n" + //
                "Please enter the minimum version that this modified documentation will apply to.\n" + //
                "If for example you are documentation specific changes to a mod this will be the\n" + //
                "version in which those changes were first released.\n" + //
                "Version should only contain numbers and colon's";
        String newVersion = JOptionPane.showInputDialog(this, info, "Select new version", JOptionPane.PLAIN_MESSAGE);
        if (newVersion == null) {
            return;
        }

        if (!versionValidator.matcher(newVersion).find()) {
            JOptionPane.showMessageDialog(this, "The specified version \"" + newVersion + "\" did not pass validation!\nPI only supports semantic versions (versions comprised of numbers and periods) e.g. 1.0.0\nVersions containing other characters are not supported.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DocumentationManager.sortedModVersionMap.getOrDefault(page.getModid(), new LinkedList<>()).contains(newVersion)) {
            JOptionPane.showMessageDialog(this, "That version already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DocumentationManager.stringToInt(newVersion.split("\\."));
            if (!FileHandler.FILE_NAME_VALIDATOR.test(newVersion)) {
                throw new Exception("");
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "The selected version is invalid! Please check your version and try again!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DocumentationManager.addNewDocVersion(page.getModid(), newVersion, copyDocFromSelected.isSelected() ? page.getModVersion() : null);
        }
        catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while trying to create the new version! " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAlias(ActionEvent evt) {
        DocumentationPage page = getSelected();
        ModStructurePage modPage;
        if (page == null || (modPage = DocumentationManager.getModPage(page.getModid())) == null) {
            return;
        }

        String newAlias = JOptionPane.showInputDialog(this, "Mod aliases are meant to me used as essentially alternate modid's for a mod.\nThis should only be needed in 1 or 2 situations. Ether a mod changes its id.\nOr your documenting a mod that is split into multiple parts that each have a different id.", "Add Alias", JOptionPane.PLAIN_MESSAGE);
        if (newAlias == null || newAlias.isEmpty() || modPage.getModAliases().contains(newAlias)) {
            return;
        }

        String selected = selectedPageURI;
        modPage.getModAliases().add(newAlias);
        DocumentationManager.saveDocToDisk(modPage);
        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(selected));
    }

    private void editAlias(ActionEvent evt) {
        DocumentationPage page = getSelected();
        ModStructurePage modPage;
        if (page == null || (modPage = DocumentationManager.getModPage(page.getModid())) == null) {
            return;
        }

        String selected = aliasList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an alias to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String modified = JOptionPane.showInputDialog(this, "Edit alias", selected);
        if (modified == null || modified.isEmpty()) {
            return;
        }

        modPage.getModAliases().remove(selected);
        modPage.getModAliases().add(modified);
        String s = selectedPageURI;
        DocumentationManager.saveDocToDisk(modPage);
        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(s));
    }

    private void removeAlias(ActionEvent evt) {
        DocumentationPage page = getSelected();
        ModStructurePage modPage;
        if (page == null || (modPage = DocumentationManager.getModPage(page.getModid())) == null) {
            return;
        }

        String selected = aliasList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an alias to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modPage.getModAliases().remove(selected);
        String s = selectedPageURI;
        DocumentationManager.saveDocToDisk(modPage);
        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(s));
    }

    private void deleteModAction(ActionEvent evt) {
        DocumentationPage page = getSelected();
        ModStructurePage modPage;
        if (page == null || (modPage = DocumentationManager.getModPage(page.getModid())) == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + modPage.getDisplayName() + " (id: " + modPage.getModid() + ") It will be lost forever! (A long time)", "Delete Mod?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == 1) {
            return;
        }

        confirm = JOptionPane.showConfirmDialog(this, "Seriously though. Are you Really Absolutely Sure you Really Want to Delete This Mod?", "Really Delete Mod?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == 1) {
            return;
        }

        DocumentationManager.deleteDoc(page);
        DocumentationManager.checkAndReloadDocFiles();
    }

    //Page Fields

    private boolean ignoreWeightChange = false;

    private void weightChanged(ChangeEvent evt) {
        if (ignoreWeightChange) return;
        long t = System.currentTimeMillis();
        DocumentationPage page = getSelected();
        if (page != null) {
            int weight = MathHelper.clip((Integer) weightField.getValue(), -2048, 2048);
            weightField.setValue(weight);
            page.setSortingWeight(weight);
            updateTree();
            setSelectedPage(page);
        }
        LogHelper.dev((System.currentTimeMillis() - t) + "ms");
    }

    private void nameChange(KeyEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null) {
            LanguageManager.setPageName(page.getModid(), page.getPageURI(), nameField.getText(), LanguageManager.getUserLanguage());
            pageTree.updateUI();
        }
    }

    private void idChange(KeyEvent evt) {
//        DocumentationPage page = getSelected();
//        if (page != null && !(page instanceof ModStructurePage)) {
//            page.setPageId(idField.getText());
//        }
    }

    private void revisionChange(ChangeEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null) {
            page.setRevision((Integer) revisionField.getValue());
        }
    }

    private void updateEnRev(ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void toggleHidden(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null) {
            page.setHidden(toggleHidden.isSelected());
        }
    }

    private void newPageAction(ActionEvent evt) {
        addPage(getSelected());
    }

    private void addPage(DocumentationPage parentPage) {
        addPage(parentPage, null);
    }

    private void addPage(DocumentationPage parentPage, String mdTemplate) {
        if (parentPage == null) return;

        String name = JOptionPane.showInputDialog(this, "Please choose a display name for this page.", "Choose page Name", JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            return;
        }

        String id = name.toLowerCase().replaceAll(" ", "_");

        id = (String) JOptionPane.showInputDialog(this, "Please enter the id for the new page. Id should use \"snake_case\"\n" +//
                "formatting and should be based on the content this page is for.\n" +//
                "e.g. a page for Draconium Ore would use the id draconium_ore\n\n" + //
                "Note: A possible id has been generated from the name you gave.\n" + //
                "Change this if you need to.", "Choose page ID", JOptionPane.PLAIN_MESSAGE, null, null, id);
        if (id == null) return;

        id = id.toLowerCase().replaceAll(" ", "_");

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Page id can not be empty!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DocumentationManager.getPage(parentPage.getPageURI() + (parentPage instanceof ModStructurePage ? "" : "/") + id) != null) {
            JOptionPane.showMessageDialog(this, "The selected page already contains a sub page with this id!\nPlease choose a different id.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DocumentationPage newPage = new DocumentationPage(parentPage, parentPage.getModid(), parentPage.getModVersion(), parentPage.isPackDoc());
        parentPage.getSubPages().add(newPage);
        newPage.setPageId(id);
        newPage.generatePageURIs(parentPage.getPageURI(), new HashMap<>());
        LanguageManager.setPageName(parentPage.getModid(), newPage.getPageURI(), name, LanguageManager.getUserLanguage());

        if (mdTemplate != null) {
            LogHelper.dev("Adding Template:\n" + mdTemplate);
            try {
                newPage.setRawMarkdown(mdTemplate);
            }
            catch (DocumentationPage.MDException e) {
                JOptionPane.showMessageDialog(this, "An error occurred!\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(newPage.getPageURI()));
    }

    private void newModAction(ActionEvent evt) {
        UINewDoc ui = new UINewDoc(this, Maps.filterEntries(ModHelperBC.getModNameMap(), input -> input != null && !PIGuiHelper.getSupportedMods().contains(input.getKey())), false);
        PIGuiHelper.centerWindowOnMC(ui);
        ui.setVisible(true);

        if (!ui.isCanceled()) {
            try {
                DocumentationManager.addMod(ui.getDocID(), ui.getDocName(), ui.getModVersion());
            }
            catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage(), "An Error Occurred!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void newLocalDoc(ActionEvent evt) {
        UINewDoc ui = new UINewDoc(this, Collections.emptyMap(), true);
        PIGuiHelper.centerWindowOnMC(ui);
        ui.setVisible(true);

        if (!ui.isCanceled()) {
            try {
                DocumentationManager.addLocalDoc(ui.getDocID(), ui.getDocName());
            }
            catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage(), "An Error Occurred!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeLangAction(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "This button is not implemented yet.\n" + "Please change language in game ether by changing the game language or by setting a different language in PI itself.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updatePageId(ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void importContentFromLang(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "This feature is not yet implemented!", "NYI", JOptionPane.ERROR_MESSAGE);
    }

    //endregion

    //region Icons

    private void cycleIcons(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null) {
            page.setCycle_icons(!page.cycle_icons());
            String sp = selectedPageURI;
            DocumentationManager.checkAndReloadDocFiles();
            setSelectedPage(DocumentationManager.getPage(sp));
        }
    }

    private void addIcon(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        Consumer<ContentInfo> addIconAction = new Consumer<ContentInfo>() {
            @Override
            public void accept(ContentInfo contentInfo) {
                DocumentationPage iconPage = DocumentationManager.getPage(page.getPageURI());
                if (contentInfo != null && !(iconPage instanceof RootPage)) {
                    iconPage.getIcons().add(contentInfo.getAsIconObj());
                    iconPage.saveToDisk();
//                    PIGuiHelper.reloadPageList();
                    setSelectedPage(DocumentationManager.getPage(selectedPageURI));
                }
                SwingUtilities.invokeLater(() -> toFront());
            }
        };

        PIGuiHelper.openContentChooser(null, ICON, addIconAction, ITEM_STACK, ENTITY, IMAGE);
        toBack();  //TODO override always on top and only do this if the py window is on top of the mc window.
    }

    private void editIcon(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        ContentInfo selected = iconList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an icon to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JsonObject selectedObj = selected.getAsIconObj();

        Consumer<ContentInfo> editIconAction = new Consumer<ContentInfo>() {
            @Override
            public void accept(ContentInfo contentInfo) {
                DocumentationPage iconPage = DocumentationManager.getPage(page.getPageURI());
                toFront();
                if (contentInfo != null && !(iconPage instanceof RootPage)) {
                    iconPage.getIcons().remove(selectedObj);
                    iconPage.getIcons().add(contentInfo.getAsIconObj());
                    iconPage.saveToDisk();
//                    PIGuiHelper.reloadPageList();
                    setSelectedPage(DocumentationManager.getPage(selectedPageURI));
                }
            }
        };

        PIGuiHelper.openContentChooser(selected, ICON, editIconAction, ITEM_STACK, ENTITY, IMAGE);
        toBack();  //TODO override always on top and only do this if the py window is on top of the mc window.
    }

    private void removeIcon(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        ContentInfo selected = iconList.getSelectedValue();
        int selectedIndex = iconList.getSelectedIndex();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an icon to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProcessHandlerClient.syncTask(() -> {
            page.getIcons().remove(selectedIndex);
            page.saveToDisk();
//            PIGuiHelper.reloadPageList();
            setSelectedPage(DocumentationManager.getPage(selectedPageURI));
        });
    }

    //endregion

    //region Relations / Match Version

    private void addRelation(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        Consumer<ContentInfo> addRelationAction = new Consumer<ContentInfo>() {
            @Override
            public void accept(ContentInfo contentInfo) {
                DocumentationPage docPage = DocumentationManager.getPage(page.getPageURI());
                toFront();
                if (contentInfo != null && !(docPage instanceof RootPage)) {
                    docPage.getRelations().add(contentInfo.asRelation());
                    docPage.saveToDisk();
//                    PIGuiHelper.reloadPageList();
                    DocumentationManager.clearRelationCache();
                    setSelectedPage(DocumentationManager.getPage(selectedPageURI));
                }
            }
        };

        PIGuiHelper.openContentChooser(null, RELATION, addRelationAction, ITEM_STACK, ENTITY, FLUID);
        toBack();
    }

    private void editRelation(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        ContentRelation selected = relationList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a relation to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ContentInfo selectedInfo = ContentInfo.fromRelation(selected);

        Consumer<ContentInfo> editRelationAction = new Consumer<ContentInfo>() {
            @Override
            public void accept(ContentInfo contentInfo) {
                DocumentationPage docPage = DocumentationManager.getPage(page.getPageURI());
                toFront();
                if (contentInfo != null && !(docPage instanceof RootPage)) {
                    docPage.getRelations().remove(selected);
                    docPage.getRelations().add(contentInfo.asRelation());
                    docPage.saveToDisk();
//                    PIGuiHelper.reloadPageList();
                    DocumentationManager.clearRelationCache();
                    setSelectedPage(DocumentationManager.getPage(selectedPageURI));
                }
            }
        };

        PIGuiHelper.openContentChooser(selectedInfo, RELATION, editRelationAction, ITEM_STACK, ENTITY, FLUID);
        toBack();
    }

    private void removeRelation(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        ContentRelation selected = relationList.getSelectedValue();
//        int selectedIndex = relationList.getSelectedIndex();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an icon to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProcessHandlerClient.syncTask(() -> {
            page.getRelations().remove(selected);
            page.saveToDisk();
//            PIGuiHelper.reloadPageList();
            DocumentationManager.clearRelationCache();
            setSelectedPage(DocumentationManager.getPage(selectedPageURI));
        });
    }

    //TODO this code to set language matching is a bit of a mess and needs to be re written.
    private void updateMatchRev(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }
        PageLangData data = LanguageManager.getLangData(page.getPageURI(), LanguageManager.getUserLanguage());
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please update the page name first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object item = matchLangBox.getSelectedItem();
        data.setMatchLang(item == null || item.equals("disabled") ? null : "" + item, (int) enRevField.getValue());
    }

    private void matchLangChange(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }
        PageLangData data = LanguageManager.getLangData(page.getPageURI(), LanguageManager.getUserLanguage());
        Object item = matchLangBox.getSelectedItem();
        String lang = item == null || item.equals("disabled") ? null : "" + item;
        if (data == null) {
            if (lang == null) return;
            JOptionPane.showMessageDialog(this, "Please update the page name first.. ", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        data.setMatchLang(lang, (int) enRevField.getValue());
        enRevField.setEnabled(lang != null);
    }

    private void matchRevChange(ChangeEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }
        PageLangData data = LanguageManager.getLangData(page.getPageURI(), LanguageManager.getUserLanguage());
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Please update the page name first...", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object item = matchLangBox.getSelectedItem();
        data.setMatchLang(item == null || item.equals("disabled") ? null : "" + item, (int) enRevField.getValue());
    }

    //endregion

    //region General/Selection

    private void pageSelected(TreeSelectionEvent evt) {
        Object component = pageTree.getLastSelectedPathComponent();
        if (component instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) component).getUserObject() instanceof TreePageContainer) {
            setSelectedPage(((TreePageContainer) ((DefaultMutableTreeNode) component).getUserObject()).getPage());
        }
        else {
            setSelectedPage(null);
        }
    }

    private void setSelectedPage(@Nullable DocumentationPage page) {
        reloading = true;
        if (!selectedPageURI.isEmpty()) {
            caretPositions.put(selectedPageURI, markdownWindow.getCaretPosition());
        }

        selectedPageURI = page == null ? "" : page.getPageURI();
        boolean isModPage = page instanceof ModStructurePage;
        boolean pageSelected = page != null;

        newPageButton.setEnabled(pageSelected);

        //Page Fields
        nameField.setEnabled(pageSelected && !isModPage);
        idField.setEnabled(pageSelected && !isModPage);
        weightField.setEnabled(pageSelected && !isModPage);
        revisionField.setEnabled(pageSelected);

        cycleIcons.setEnabled(pageSelected);
        iconList.setEnabled(pageSelected);
        relationList.setEnabled(pageSelected && !isModPage);
        toggleHidden.setEnabled(pageSelected && !isModPage);

        enRevField.setEnabled(false);//TODO
        updateEnRev.setEnabled(false);

        //Mod Fields
        modNameField.setEnabled(pageSelected);
        modIdField.setEnabled(pageSelected);
        modVersionSelect.setEnabled(pageSelected);

        if (pageSelected) {
            String modid = page.getModid();
            LinkedList<String> versions = DocumentationManager.sortedModVersionMap.get(modid);

            //Mod Fields
            modNameField.setText(LanguageManager.getPageLangName(page.getModid() + ":", page.getLocalizationLang()));
            modIdField.setText(modid);
            modVersionSelect.removeAllItems();
            modVersionSelect.addItem("[Default-Best-Match]");
            if (versions != null) versions.forEach(version -> modVersionSelect.addItem(version));
            modVersionSelect.setSelectedItem(page.getModVersion());

            //Page Fields
            nameField.setText(isModPage ? "" : LanguageManager.getPageLangName(page));
            idField.setText(isModPage ? "" : page.getPageId());
            ignoreWeightChange = true;
            weightField.setValue(page.getSortingWeight());
            ignoreWeightChange = false;
            cycleIcons.setSelected(page.cycle_icons());
            toggleHidden.setSelected(page.isHidden());

            tabbedPain.setSelectedIndex(page instanceof ModStructurePage ? 0 : 1);

            readMDFile(page);

            //This just makes sure the row is selected in the tree. This can be an issue when the page is not selected by clicking it in the tree.
            for (int i = 0; i < pageTree.getRowCount(); i++) {
                DefaultMutableTreeNode comp = (DefaultMutableTreeNode) pageTree.getPathForRow(i).getLastPathComponent();
                if (comp != null && comp.getUserObject() instanceof TreePageContainer) {
                    TreePageContainer container = (TreePageContainer) comp.getUserObject();
                    if (container.getPage() == page) {
                        if (!pageTree.isRowSelected(i)) {
                            pageTree.setSelectionRow(i);
                        }
                        break;
                    }
                }
            }
        }
        else {
            markdownWindow.setText("");
        }

        //Pack Dock
        boolean isPackPage = page != null && page.isPackDoc();
        modVersionSelect.setEnabled(!isPackPage);
        createFromExistingModButton.setEnabled(!isPackPage);
        revisionField.setEnabled(!isPackPage);
        matchLangBox.setEnabled(!isPackPage);
        targetEnRev.setEnabled(!isPackPage);


        reloadLists();
        reloading = false;

//        BoundedRangeModel model = mdScrollPane.getVerticalScrollBar().getModel();
//        double scrollPos = (double) model.getValue() / (double) (model.getMaximum() - model.getMinimum());
        if (page != null) {
            //This is a mess... The entire tree loading system in the editor needs a proper overhaul.
            ProcessHandlerClient.syncTask(() -> DisplayController.MASTER_CONTROLLER.openPage(getSelectedPageURI(), false));
        }
//        ProcessHandlerClient.syncTask(() -> TabManager.getActiveTab().updateScroll(scrollPos));
    }

    private synchronized String getSelectedPageURI() {
        return selectedPageURI;
    }

    private void readMDFile(DocumentationPage page) {
        File mdFile = page.getMarkdownFile();
        boolean set = false;

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(mdFile), StandardCharsets.UTF_8)) {
            markdownWindow.setText(IOUtils.toString(reader));
            int pos = caretPositions.getOrDefault(page.getPageURI(), 0);
            markdownWindow.setCaretPosition(MathHelper.clip(pos, 0, markdownWindow.getText().length() - 1));
            set = true;
        }
        catch (IOException ignored) {}

        if (!set) {
            markdownWindow.setText("");
        }
    }

    private void reloadLists() {
        DocumentationPage page = getSelected();
        iconListModel.clear();
        relationListModel.clear();
        aliasListModel.clear();

        if (page == null) {
            return;
        }

        PageLangData data = LanguageManager.getLangData(page.getPageURI(), LanguageManager.getUserLanguage());
        if (data != null) {
            if (data.matchLang != null) {
                matchLangBox.setSelectedItem(data.matchLang);
                enRevField.setValue(data.matchRev);
            }
            else {
                matchLangBox.setSelectedItem("disabled");
            }
            revisionField.setValue(data.pageRev);
            enRevField.setEnabled(data.matchLang != null);
            if (enRevField.isEnabled()) {
                revisionField.setValue(data.pageRev);
            }
        }
        else {
            matchLangBox.setSelectedItem("disabled");
            enRevField.setEnabled(false);
        }


        page.getIcons().forEach(jsonObject -> iconListModel.addElement(ContentInfo.fromIconObj(jsonObject)));

        ModStructurePage sp = DocumentationManager.getModPage(page.getModid());
        if (sp != null) {
            sp.getModAliases().forEach(alias -> aliasListModel.addElement(alias));
        }

        if (!(page instanceof ModStructurePage)) {
            page.getRelations().forEach(s -> relationListModel.addElement(s));
        }
    }

    private void deletePage(ActionEvent evt) {
        deletePage(getSelected());
    }

    private void deletePage(DocumentationPage page) {
        if (page != null && !(page instanceof ModStructurePage) && JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this page and its sub pages? \"" + page.getDisplayName() + "\" This can not be undone!", "Confirm Delete", JOptionPane.YES_NO_OPTION) == 0) {
            page.deletePage();
            reload();
            GuiProjectIntelligence.requiresEditReload = true;
        }
    }

    private void pageTreeMouseClicked(MouseEvent evt) {
        if (SwingUtilities.isRightMouseButton(evt)) {
            TreePath path = pageTree.getPathForLocation(evt.getX(), evt.getY());
            if (path == null) return;

            TreePageContainer container = (TreePageContainer) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
            generateTreeContext(container).forEach(comp -> treeContextMenu.add(comp));
            treeContextMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private java.util.List<JComponent> generateTreeContext(TreePageContainer container) {
        List<JComponent> menuItems = new LinkedList<>();
        JMenuItem item;

        JLabel label = new JLabel(container.getPage().getDisplayName());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        menuItems.add(label);
        menuItems.add(new JPopupMenu.Separator());

        item = new JMenuItem("Copy Page URI");
        item.addActionListener(e -> Utils.setClipboardString(container.pageURI));
        menuItems.add(item);

        long t = System.currentTimeMillis();

        if (!(container.getPage() instanceof RootPage)) {
            item = new JMenuItem("New Page");
            item.addActionListener(e -> addPage(container.getPage()));
            menuItems.add(item);
        }

//        LogHelper.dev("T1 " + (System.currentTimeMillis() - t));

        if (!container.isStructPage) {
            item = new JMenuItem("Delete Page");
            item.addActionListener(e -> deletePage(container.getPage()));
            menuItems.add(item);
        }

//        LogHelper.dev("T2 " + (System.currentTimeMillis() - t));

        if (!(container.getPage() instanceof RootPage)) {
            menuItems.add(new JPopupMenu.Separator());
            JMenu menu = new JMenu("Load Page Template");

            File templatesDir = new File(DocumentationManager.getDocDirectory().getParent(), "PageTemplates");
            if (templatesDir.exists()) {
                File[] files = templatesDir.listFiles((dir, name) -> name.endsWith(".template"));
                if (files != null) {
                    for (File file : files) {
                        JMenuItem tempItem = new JMenuItem(file.getName().replace(".template", ""));
                        tempItem.addActionListener(e -> {
                            try {
                                addPage(container.getPage(), FileUtils.readFileToString(file, Charsets.UTF_8));
                            }
                            catch (IOException e1) {
                                JOptionPane.showMessageDialog(this, "An error occurred!\n\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        menu.add(tempItem);
                    }
                }
            }

            menuItems.add(menu);
        }

//        LogHelper.dev("T3 " + (System.currentTimeMillis() - t));

        String clipboard = Utils.getClipboardString();
        if (!clipboard.isEmpty()) {
            item = new JMenuItem("Add page to templates");
            item.addActionListener(e -> {
                File templatesDir = new File(DocumentationManager.getDocDirectory().getParent(), "PageTemplates");
                String name = JOptionPane.showInputDialog("Please specify a name for the template.\n" + "Please note that currently the only way to remove a template is to manually delete it from disk.\n" + "Templates are stored in: " + templatesDir.getAbsolutePath());
                if (name != null) {
                    File file = new File(templatesDir, name + ".template");
                    if (file.exists()) {
                        int response = JOptionPane.showConfirmDialog(this, "There is already a template with that name. Do you wish to replace it?", "Replace", JOptionPane.YES_NO_OPTION);
                        if (response == 1) return;
                    }
                    try {
                        FileUtils.copyFile(container.getPage().getMarkdownFile(), file);
                    }
                    catch (IOException e1) {
                        JOptionPane.showMessageDialog(this, "An error occurred!\n\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
//                    FileUtils.writeStringToFile(file, container.getPage()., Charsets.UTF_8);
                }
            });
            menuItems.add(item);
        }

//        LogHelper.dev("T4 " + (System.currentTimeMillis() - t));


        return menuItems;
    }

    private void treeMenuOpen(PopupMenuEvent evt) {
    }

    private void treeMenuClose(PopupMenuEvent evt) {
        treeContextMenu.removeAll();
    }

    //endregion

    //region MD Editing

    private void mdTextChange(KeyEvent evt) {
        markdownWindow.setLineWrap(true);
        markdownWindow.setTabSize(2);
        markdownWindow.setWrapStyleWord(true);

        DocumentationPage page = getSelected();
        if (page == null) {
            markdownWindow.setText("");
        }
        else {
            ProcessHandlerClient.syncTask(() -> {
                try {
                    page.setRawMarkdown(markdownWindow.getText());
                }
                catch (DocumentationPage.MDException e) {
                    JOptionPane.showMessageDialog(this, "An error occurred!\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private void insertAction(ActionEvent evt) {
        String action = evt.getActionCommand();

        Consumer<ContentInfo> applyContentTag = contentInfo -> {
            if (contentInfo != null) {
                String tag = contentInfo.toMDTag();
                markdownWindow.insert(tag + " ", markdownWindow.getCaretPosition());
                markdownWindow.moveCaretPosition(markdownWindow.getCaretPosition() + tag.length());
                mdTextChange(null);
            }
            toFront();
        };

        switch (action) {
            case "stack":
                toBack();
                PIGuiHelper.openContentChooser(null, MD_CONTENT, applyContentTag, ITEM_STACK);
                break;
            case "recipe": {
                toBack();
                PIGuiHelper.openContentChooser(null, GuiContentSelect.SelectMode.PICK_STACK, contentInfo -> SwingUtilities.invokeLater(() -> {
                    toFront();
                    if (contentInfo == null) return;
                    MDTagDialog tagD = new MDTagDialog(this, MDTagDialog.TagType.RECIPE);
                    tagD.setStack(contentInfo.stack.toString());
                    tagD.setVisible(true);
                    markdownWindow.insert(tagD.getTag(), markdownWindow.getCaretPosition());
                    mdTextChange(null);
                }), ContentInfo.ContentType.ITEM_STACK);
                break;
            }
            case "image":
                toBack();
                PIGuiHelper.openContentChooser(null, MD_CONTENT, applyContentTag, IMAGE);
                break;
            case "link": {
                MDTagDialog tagD = new MDTagDialog(this, MDTagDialog.TagType.LINK);
                tagD.setVisible(true);
                markdownWindow.insert(tagD.getTag(), markdownWindow.getCaretPosition());
                break;
            }
            case "entity":
                toBack();
                PIGuiHelper.openContentChooser(null, MD_CONTENT, applyContentTag, ENTITY);
                break;
            case "rule": {
                MDTagDialog tagD = new MDTagDialog(this, MDTagDialog.TagType.RULE);
                tagD.setVisible(true);
                markdownWindow.insert(tagD.getTag(), markdownWindow.getCaretPosition());
                break;
            }
            case "table": {
                MDTagDialog tagD = new MDTagDialog(this, MDTagDialog.TagType.TABLE);
                tagD.setVisible(true);
                markdownWindow.insert(tagD.getTag(), markdownWindow.getCaretPosition());
                break;
            }
        }

        mdTextChange(null);
    }

    private void insertFormat(TextFormatting format) {
        int start = markdownWindow.getSelectionStart();
        String selection = markdownWindow.getSelectedText();
        String fs = format.toString();

        if (selection != null) {
            selection = fs + selection + fs;
            selection = selection.replace("\n", fs + "\n" + fs);
            markdownWindow.replaceSelection(selection);
        }
        else {
            markdownWindow.insert(fs, start);
        }

        mdTextChange(null);
    }

    private void mdMenuOpen(PopupMenuEvent evt) {
        generateMDContext().forEach(comp -> mdContextMenu.add(comp));
    }

    private void mdMenuClose(PopupMenuEvent evt) {
        mdContextMenu.removeAll();
    }

    private java.util.List<JComponent> generateMDContext() {
        List<JComponent> menuItems = new LinkedList<>();
        String selected = markdownWindow.getSelectedText();
        JMenuItem item;
        boolean hasEdit = false;

        if (selected != null && !selected.isEmpty()) {
            item = new JMenuItem("Copy");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
            item.addActionListener(e -> markdownWindow.copy());
            menuItems.add(item);

        }

        String clipboard = Utils.getClipboardString();
        if (!clipboard.isEmpty()) {
            item = new JMenuItem("Paste");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
            item.addActionListener(e -> markdownWindow.paste());
            menuItems.add(item);
            hasEdit = true;
        }

        if (undo.canUndo()) {
            item = new JMenuItem("Undo");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
            item.addActionListener(e -> undo());
            menuItems.add(item);
            hasEdit = true;
        }

        if (undo.canRedo()) {
            item = new JMenuItem("Redo");
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
            item.addActionListener(e -> redo());
            menuItems.add(item);
            hasEdit = true;
        }

        if (hasEdit) {
            menuItems.add(new JPopupMenu.Separator());
        }

        JMenu formatMenu = new JMenu("Apply Format");
        menuItems.add(formatMenu);

        for (TextFormatting format : TextFormatting.values()) {
            item = new JMenuItem(format.getFriendlyName());
            item.addActionListener(e -> insertFormat(format));
            item.setFont(item.getFont().deriveFont(4));
            if (format.isColor()) {
                item.setBackground(new Color(DataUtils.formatColour(format)));
            }
            else {
                switch (format) {
                    case BOLD:
                        item.setText("<html><b>" + format.getFriendlyName() + "</html>");
                        break;
                    case STRIKETHROUGH:
                        item.setText("<html><s>" + format.getFriendlyName() + "</html>");
                        break;
                    case UNDERLINE:
                        item.setText("<html><u>" + format.getFriendlyName() + "</u></html>");
                        break;
                    case ITALIC:
                        item.setText("<html><i>" + format.getFriendlyName() + "</html>");
                        break;
                }
            }

            formatMenu.add(item);
        }

        JMenu pFormatMenu = new JMenu("Paragraph Format");
        menuItems.add(pFormatMenu);

        pFormatMenu.add(item = new JMenuItem("Align Left"));
        item.addActionListener(e -> insertParagraphTag("\u00a7align:left"));
        pFormatMenu.add(item = new JMenuItem("Align Center"));
        item.addActionListener(e -> insertParagraphTag("\u00a7align:center"));
        pFormatMenu.add(item = new JMenuItem("Align Right"));
        item.addActionListener(e -> insertParagraphTag("\u00a7align:right"));
        pFormatMenu.add(item = new JMenuItem("Shadow"));
        item.addActionListener(e -> insertParagraphTag("\u00a7shadow"));
        pFormatMenu.add(item = new JMenuItem("Colour"));

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JColorChooser chooser = new JColorChooser(Color.WHITE);

                JLabel previewLabel = new JLabel("Colour Preview", JLabel.CENTER) {
                    @Override
                    public void paint(Graphics g) {
                        super.paint(g);
                        g.setColor(new Color(getForeground().getRGB()));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                previewLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 48));
                previewLabel.setSize(previewLabel.getPreferredSize());
                previewLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
                chooser.setPreviewPanel(previewLabel);

                JColorChooser.createDialog(PIEditor.this, "Select Colour", true, chooser, e1 -> insertParagraphTag("\u00a7colour[0x" + Integer.toHexString(chooser.getColor().getRGB()).substring(2) + "]"), null).setVisible(true);
            }
        });

        if (markdownWindow.getText().replaceAll(" ", "").replaceAll("\n", "").isEmpty()) {
            menuItems.add(new JPopupMenu.Separator());
            JMenu menu = new JMenu("Load Template");

            File templatesDir = new File(DocumentationManager.getDocDirectory().getParent(), "PageTemplates");
            if (templatesDir.exists()) {
                File[] files = templatesDir.listFiles((dir, name) -> name.endsWith(".template"));
                if (files != null) {
                    for (File file : files) {
                        JMenuItem tempItem = new JMenuItem(file.getName().replace(".template", ""));
                        tempItem.addActionListener(e -> {
                            try {
                                markdownWindow.setText(FileUtils.readFileToString(file, Charsets.UTF_8));
                                mdTextChange(null);
                            }
                            catch (IOException e1) {
                                JOptionPane.showMessageDialog(this, "An error occurred!\n\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        menu.add(tempItem);
                    }
                }
            }

            menuItems.add(menu);
        }

        return menuItems;
    }

    private void insertParagraphTag(String tag) {
        int start = markdownWindow.getSelectionStart();
        String text = markdownWindow.getText();

        if ((start <= 0 || text.charAt(start - 1) == '\n') && (start + 1 >= text.length() || text.charAt(start) == '\n')) {
            markdownWindow.insert("\n" + tag, start);
        }
        else {
            markdownWindow.insert("\n" + tag + "\n", start);
        }
        mdTextChange(null);
    }

    private void editMenuSelect(MenuEvent evt) {
        jMenu2.removeAll();
        generateMDContext().forEach(jComponent -> jMenu2.add(jComponent));
    }

    //endregion

    private String getHelpInfo(String topic) {
        switch (topic) {
            //Mod
            case "modName":
                return "This is the display name for the mod. e.g. Draconic Evolution or Thermal Expansion. This name supports translation.";
            case "modID":
                return "This is the mod id as specified by the mod itself.";
            case "versionHelp":
                return "Ok so read carefully this can be confusing. This sets the minimum version of the mod to which this documentation applies.\n" + "So if you want this to apply to all versions ever set this to 0.0.0 and if this is the only documentation version written for\n" + "this mod then this will apply to all versions of the mod.\n\n" + "Now say the mod releases a new version that changes a bunch of things. Do NOT change the existing documentation!\n" + "This would make it incompatible with the previous version of the mod. Instead you must create a copy of the documentation with\n" + "the minimum mod version set to match the new mod version. You can then make the required changes to the copy.\n\n" + "So what this does is creates two completely separate sets of documentation. One with its min version set to 0.0.0 and the other\n" + "set to say, 1.0.0 for example. Now if the installed mod version is less then 1.0.0 it will use the documentation targeting 0.0.0\n" + "otherwise it will use the documentation for 1.0.0. Do not be afraid to create a new doc version when something changes! All files are indexed\n" + "by their hashes on the server so it does not matter if you copy the entire documentation for a mod only to change one page. The server\n" + "will only need to store that one extra page.";
            case "aliases":
                return "Aliases are to be used in the event that a mod changes its mod id or has multiple id's such a mod like Project Red that is split into modules.\n" + "Though in the case of a mod like Project red it should be sufficient to just use the ID for the core main mod that all of the other modules depend on.\n" + "Alternatively it may make more sense to have separate documentation for each module.\n\n" + "But back to the point there are times where mods have to or just decide to change their name such as when forge started enforcing lowercase id's.\n" + "This forced DraconicEvolution to change to draconicevolution. In the case of DE if doc had already been created for the uppercase ID then the lowercase\n" + "ID would be added as an alias.";

            //Page Properties
            case "name":
                return "This is the name of the page. This name supports localization.\n" + "For help with adding translations see \"Lang Help\"";
            case "id":
                return "This is the unique id for this page. This should be unique within the context of the parent page. Meaning a page can not have two sub pages with the same id.\n" + "The id should be lowercase camel_case, Any uppercase characters will be automatically converted to lowercase and spaces will be replaced with underscores _.\n\n" + "The id should be indicative of the page content so a page about Draconium Ore should use the id draconium_ore.";
            case "weight":
                return "Page weight is used to determine the order in which pages are displayed in the page list.\n" + "Pages with a higher weight will sink down and be displayed bellow pages with a lower weight.\n" + "Pages with the same weight should be displayed in the order they were added but this is not guaranteed.";
            case "revision":
                return "This is the page revision. Whenever a page is updated the revision should be incremented. This is can be used to link translated pages to the default language.\n" + "E.g. say you write a page in english and it has a revision number of 1. That page is then translated to chinese. The translated page would be set to match\n" + "revision 1 of the english translation. Now if something on in the english translation changes its revision will increment and as a result the chinese translation\n" + "will be automatically marked as potentially outdated because its still targeting revision 1 of the english translation.";
            case "matchRev":
                return "As mentioned in the help info for the revision field (which you should read first) this can be used to match a page translation to the default translation revision\n" + "(usually english). That way when the targeted page updates this page will automatically be marked as potentially outdated.";
            case "icons":
                return "This is where you can add icons to be displayed to the left of the page name in the page list. An icon can be ether an item stack, entity or correctly sized image.\n" + "By default if you add more than one icon the first will be displayed and the others will be used as fallback icons. This means if your documentation spans multiple\n" + "mod versions and the id for an item changes you can add all of its id's and it will use whatever one is avalible for the icon.\n" + "You also have the option to check the \"Cycle Icons\" box. When option is enabled and you have multiple icons it will cycle between the listed icons.";
            case "relations":
                return "Relations are not yet implemented but when they are they will allow you to link pages directly to in game contend. For example the page for Draconium Ore could be\n" + "linked to the Draconium Ore item. Then there would be an option in game perhaps via a key bind while you have your mouse over draconium ore in your inventory to go\n" + "directly to the documentation page linked to that item. This may also be applied to mobs and blocks in world.";
        }

        return "[Error]: This help button has no bound help info!\n" + topic;
    }

    private void helpAction(ActionEvent evt) {
        String helpText = getHelpInfo(evt.getActionCommand());
        JOptionPane.showMessageDialog(this, helpText, "Help Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mdScroll(MouseWheelEvent evt) {
//        BoundedRangeModel model = mdScrollPane.getVerticalScrollBar().getModel();
//        double scrollPos = (double) model.getValue() / (double) (model.getMaximum() - model.getMinimum());
//        ProcessHandlerClient.syncTask(() -> TabManager.getActiveTab().updateScroll(scrollPos));
    }

    //region Misc

    private void openMDRef(ActionEvent evt) {
        MDReference frame = new MDReference();
        frame.setVisible(true);
        frame.setAlwaysOnTop(alwaysOnTop.isSelected());
        frame.jCheckBox1.setSelected(alwaysOnTop.isSelected());
        PIGuiHelper.centerWindowOn(frame, this);
    }

    public DocumentationPage getSelected() {
        return DocumentationManager.getPage(selectedPageURI);
    }

    private void aotAction(ActionEvent evt) {
        PIConfig.editorAlwaysOnTop = alwaysOnTop.isSelected();
        PIConfig.save();
        setAlwaysOnTop(PIConfig.editorAlwaysOnTop);
    }

    public static Stream<Integer> streamTree(JTree tree) {
        return StreamSupport.stream(new TreeIterator(tree).spliterator(), false);
    }

    public static Stream<Integer> streamTree(JTree tree, boolean backwards) {
        return StreamSupport.stream(new TreeIterator(tree, backwards).spliterator(), false);
    }

    public static class Singleton<E> {
        private E e;

        public Singleton(E e) {
            this.e = e;
        }

        public E get() {
            return e;
        }

        public void set(E e) {
            this.e = e;
        }
    }

    public static class TreeIterator implements Iterator<Integer>, Iterable<Integer> {

        private int row;
        private boolean backwards;
        private JTree tree;

        public TreeIterator(JTree tree) {
            row = 0;
            this.tree = tree;
            backwards = false;
        }

        public TreeIterator(JTree tree, boolean backwards) {
            row = tree.getRowCount() - 1;
            this.backwards = backwards;
            this.tree = tree;
        }

        @Override
        public Iterator<Integer> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return backwards ? row > 0 : row < tree.getRowCount();
        }

        @Override
        public Integer next() {
            return backwards ? row-- : row++;
        }
    }

    public static class TreePageContainer {

        private String pageURI;
        public boolean isStructPage;
        public boolean isPackDoc;

        public TreePageContainer(DocumentationPage page) {
            this.pageURI = page.getPageURI();
            this.isStructPage = page instanceof ModStructurePage;
            this.isPackDoc = page.isPackDoc();
        }

        private String getSelectedVersionRange() {
            DocumentationPage page = getPage();
            if (page == null) {
                return "[Invalid-Entry]";
            }
            if (isPackDoc) {
                return "Local pack doc";
            }

            LinkedList<String> versions = DocumentationManager.sortedModVersionMap.get(page.getModid());
            if (versions == null) {
                return "[Version-Error]";
            }
            int index = versions.indexOf(page.getModVersion());
            if (index == -1 || index + 1 >= versions.size()) {
                return page.getModVersion() + "+";
            }

            return page.getModVersion() + " ->" + versions.get(index + 1);
        }

        @Override
        public String toString() {
            DocumentationPage page = getPage();
            if (page == null) {
                return "[Invalid-Entry]";
            }
            String name = pageURI;
            if (LanguageManager.isPageLocalized(pageURI, LanguageManager.getUserLanguage())) {
                name = page.toString();
            }
            if (isStructPage) {
                name += " (" + getSelectedVersionRange() + ")";
            }
            return name;
        }

        public DocumentationPage getPage() {
            return DocumentationManager.getPage(pageURI);
        }
    }

    //endregion

    //region Generated fields
    private JList<String> aliasList;
    private JCheckBoxMenuItem alwaysOnTop;
    private JMenuItem changeLangButton;
    private JCheckBox copyDocFromSelected;
    private JButton createFromExistingModButton;
    private JCheckBox cycleIcons;
    private JSpinner enRevField;
    private JList<ContentInfo> iconList;
    private JLabel iconsLabel;
    private JTextField idField;
    private JLabel idLabel;
    private JButton jButton1;
    private JButton jButton10;
    private JButton jButton11;
    private JButton jButton12;
    private JButton jButton13;
    private JButton jButton2;
    private JButton jButton3;
    private JButton jButton4;
    private JButton jButton5;
    private JButton jButton7;
    private JButton jButton8;
    private JLabel jLabel1;
    private JMenu jMenu1;
    private JMenu jMenu2;
    private JMenu jMenu3;
    private JMenuBar jMenuBar1;
    private JPanel jPanel3;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane4;
    private JScrollPane jScrollPane5;
    private JScrollPane jScrollPane6;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JSeparator jSeparator3;
    private JSeparator jSeparator4;
    private JSplitPane jSplitPane1;
    private JToolBar jToolBar1;
    private JTextArea markdownWindow;
    private JComboBox<String> matchLangBox;
    private JPopupMenu mdContextMenu;
    private JScrollPane mdScrollPane;
    private JTextField modIdField;
    private JTextField modNameField;
    private JPanel modPanel;
    private JComboBox<String> modVersionSelect;
    private JTextField nameField;
    private JLabel nameLabel;
    private JLabel nameLabel1;
    private JLabel nameLabel2;
    private JLabel nameLabel3;
    private JButton newModButton;
    private JButton newPageButton;
    private JPanel pagePanel;
    private JTree pageTree;
    private JList<ContentRelation> relationList;
    private JLabel relationsLabel;
    private JLabel relationsLabel1;
    private JSpinner revisionField;
    private JLabel revisionLabel;
    private JTabbedPane tabbedPain;
    private JLabel targetEnRev;
    private JLabel targetEnRev1;
    private JCheckBox toggleHidden;
    private JPopupMenu treeContextMenu;
    private JButton updateEnRev;
    private JSpinner weightField;
    private JLabel weightLabel;
    private JButton jButton6;
    //endregion
}
