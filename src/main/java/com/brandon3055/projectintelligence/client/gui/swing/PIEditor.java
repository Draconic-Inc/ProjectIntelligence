/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brandon3055.projectintelligence.client.gui.swing;

import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.projectintelligence.ModHelper;
import com.brandon3055.projectintelligence.PIHelpers;
import com.brandon3055.projectintelligence.client.gui.GuiProjectIntelligence;
import com.brandon3055.projectintelligence.client.gui.PIConfig;
import com.brandon3055.projectintelligence.docdata.DocumentationManager;
import com.brandon3055.projectintelligence.docdata.DocumentationPage;
import com.brandon3055.projectintelligence.docdata.LanguageManager;
import com.brandon3055.projectintelligence.docdata.ModStructurePage;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author brand
 */
public class PIEditor extends javax.swing.JFrame {

    private static Pattern versionValidator = Pattern.compile("^[\\d\\.]+$");
    private String selectedPageURI = "";
    private DefaultTreeModel treeModel;
    private DefaultListModel<Icon> iconListModel = new DefaultListModel<>();
    private DefaultListModel<String> relationListModel = new DefaultListModel<>();
    private DefaultListModel<String> aliasListModel = new DefaultListModel<>();
    private boolean reloading = false;

    /**
     * Creates new form PIEditor
     */
    public PIEditor() {
        initComponents();
        iconList.setModel(iconListModel);
        relationList.setModel(relationListModel);
        aliasList.setModel(aliasListModel);
        DefaultTreeSelectionModel selectModel = new DefaultTreeSelectionModel();
        selectModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        pageTree.setSelectionModel(selectModel);
        reload();
    }

    //region Generated Code
    @SuppressWarnings("unchecked")
    private void initComponents() {

        treePopup = new JPopupMenu();
        jMenuItem1 = new JMenuItem();
        jPanel3 = new JPanel();
        jToolBar1 = new JToolBar();
        jLabel1 = new JLabel();
        jButton1 = new JButton();
        jButton2 = new JButton();
        jButton3 = new JButton();
        jButton4 = new JButton();
        jButton5 = new JButton();
        jButton6 = new JButton();
        newModButton = new JButton();
        jSplitPane1 = new JSplitPane();
        jScrollPane2 = new JScrollPane();
        markdownWindow = new JTextArea();
        jScrollPane6 = new JScrollPane();
        pageTree = new JTree();
        newPageButton = new JButton();
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
        jButton9 = new JButton();
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
        JButton idHelp1 = new JButton();
        jMenuBar1 = new JMenuBar();
        jMenu1 = new JMenu();
        jMenu2 = new JMenu();
        jMenu3 = new JMenu();
        alwaysOnTop = new JCheckBoxMenuItem();
        changeLangButton = new JMenuItem();

        jMenuItem1.setText("jMenuItem1");
        treePopup.add(jMenuItem1);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Project Intelligence Editor");

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jLabel1.setText("Insert:");
        jToolBar1.add(jLabel1);

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
        jToolBar1.add(jButton1);

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
        jToolBar1.add(jButton2);

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
        jToolBar1.add(jButton3);

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
        jToolBar1.add(jButton4);

        jButton5.setText("Paragraph Formatting");
        jButton5.setActionCommand("paragraph");
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });
        jToolBar1.add(jButton5);

        jButton6.setText("Line Formatting");
        jButton6.setActionCommand("line");
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertAction(evt);
            }
        });
        jToolBar1.add(jButton6);

        newModButton.setText("New Mod");
        newModButton.setToolTipText("Add documentation for a new mod.");
        newModButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newModAction(evt);
            }
        });

        jSplitPane1.setDividerLocation(200);

        markdownWindow.setColumns(20);
        markdownWindow.setRows(5);
        markdownWindow.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                mdTextChange(evt);
            }
        });
        jScrollPane2.setViewportView(markdownWindow);

        jSplitPane1.setRightComponent(jScrollPane2);

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

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addComponent(newModButton, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(newPageButton, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jToolBar1, GroupLayout.PREFERRED_SIZE, 514, GroupLayout.PREFERRED_SIZE)).addComponent(jSplitPane1)).addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(newModButton).addComponent(newPageButton)).addComponent(jToolBar1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE).addContainerGap()));

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

        jButton9.setText("Create documentation for new mod version");
        jButton9.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newVersionAction(evt);
            }
        });

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
        modPanelLayout.setHorizontalGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addContainerGap().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(nameLabel1).addComponent(nameLabel2)).addGap(44, 44, 44).addComponent(modNameField, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(nameHelp1)).addGroup(modPanelLayout.createSequentialGroup().addGap(113, 113, 113).addComponent(modIdField, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(nameHelp2)).addGroup(modPanelLayout.createSequentialGroup().addContainerGap().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(jButton9, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(copyDocFromSelected).addGroup(modPanelLayout.createSequentialGroup().addComponent(nameLabel3).addGap(18, 18, 18).addComponent(modVersionSelect, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)).addComponent(jSeparator1)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(nameHelp3))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(modPanelLayout.createSequentialGroup().addComponent(addRelation1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(editRelation1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(removeRelation1)).addGroup(GroupLayout.Alignment.LEADING, modPanelLayout.createSequentialGroup().addComponent(relationsLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(relationsHelp1)).addComponent(jScrollPane5, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 296, Short.MAX_VALUE).addComponent(jButton10).addContainerGap()));
        modPanelLayout.setVerticalGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addContainerGap().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, modPanelLayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jButton10)).addComponent(jSeparator3).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(relationsLabel1).addComponent(relationsHelp1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(removeRelation1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(editRelation1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(addRelation1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel1).addComponent(modNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(nameHelp1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel2).addComponent(modIdField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(nameHelp2, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(modPanelLayout.createSequentialGroup().addGroup(modPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel3).addComponent(modVersionSelect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(1, 1, 1).addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(4, 4, 4).addComponent(jButton9).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(copyDocFromSelected)).addComponent(nameHelp3, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)))).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));

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

        targetEnRev.setText("Target EN Revision:");

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

        updateEnRev.setText("Set Current Version");
        updateEnRev.setToolTipText("Sets the target English Revision to the current english revision of this page.");
        updateEnRev.setActionCommand("revision");
        updateEnRev.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        updateEnRev.setMargin(new Insets(0, 4, 0, 4));
        updateEnRev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateEnRev(evt);
            }
        });

        revisionField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                revisionChange(evt);
            }
        });

        enRevField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                enRevChange(evt);
            }
        });

        revHelp1.setText("?");
        revHelp1.setToolTipText("Click for more info about this field.");
        revHelp1.setActionCommand("enRev");
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

        idHelp1.setText("Save");
        idHelp1.setToolTipText("Save changes to the page id. Save buttion is required for this field because this chnage requires changes to the file system which can not be dynamically applied as you type like other fields can.");
        idHelp1.setActionCommand("");
        idHelp1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        idHelp1.setMargin(new Insets(0, 4, 0, 4));
        idHelp1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updatePageId(evt);
            }
        });

        GroupLayout pagePanelLayout = new GroupLayout(pagePanel);
        pagePanel.setLayout(pagePanelLayout);
        pagePanelLayout.setHorizontalGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addContainerGap().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(idLabel).addComponent(weightLabel).addComponent(revisionLabel).addComponent(targetEnRev).addComponent(nameLabel)).addGap(8, 8, 8).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false).addComponent(nameField).addComponent(weightField).addComponent(revisionField).addGroup(pagePanelLayout.createSequentialGroup().addComponent(idField, GroupLayout.PREFERRED_SIZE, 157, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(idHelp1, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(revHelp).addComponent(nameHelp).addComponent(idHelp).addComponent(weightHelp))).addGroup(pagePanelLayout.createSequentialGroup().addComponent(enRevField).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(updateEnRev).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(revHelp1))).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE).addGroup(pagePanelLayout.createSequentialGroup().addComponent(cycleIcons).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE).addComponent(addIcon).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(editIcon).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(removeIcon)).addGroup(pagePanelLayout.createSequentialGroup().addComponent(iconsLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(iconsHelp))).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(pagePanelLayout.createSequentialGroup().addComponent(addRelation).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(editRelation).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(removeRelation)).addGroup(GroupLayout.Alignment.LEADING, pagePanelLayout.createSequentialGroup().addComponent(relationsLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(relationsHelp)).addComponent(jScrollPane4, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(jButton8, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE).addComponent(toggleHidden).addComponent(jButton7, GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)).addContainerGap()));
        pagePanelLayout.setVerticalGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(pagePanelLayout.createSequentialGroup().addGap(4, 4, 4).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameLabel).addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(nameHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(iconsHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(iconsLabel).addComponent(relationsLabel).addComponent(relationsHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(pagePanelLayout.createSequentialGroup().addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(cycleIcons).addComponent(removeIcon, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(editIcon, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(addIcon, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))).addGroup(pagePanelLayout.createSequentialGroup().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(idField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(idHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(idHelp1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(weightLabel).addComponent(weightHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(weightField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(28, 28, 28).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(revisionLabel).addComponent(revHelp, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(revisionField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(updateEnRev, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(enRevField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(targetEnRev).addComponent(revHelp1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))).addGroup(pagePanelLayout.createSequentialGroup().addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(removeRelation, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(editRelation, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE).addComponent(addRelation, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))).addGap(0, 2, Short.MAX_VALUE)).addGroup(pagePanelLayout.createSequentialGroup().addContainerGap().addGroup(pagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSeparator2).addComponent(jSeparator4).addGroup(pagePanelLayout.createSequentialGroup().addComponent(toggleHidden).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jButton8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton7, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))))).addContainerGap()));

        tabbedPain.addTab("Page Properties", pagePanel);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
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

        pack();
    }
    //endregion

    public void reload() {
        reloading = true;
        alwaysOnTop.setSelected(PIConfig.editorAlwaysOnTop);
        setAlwaysOnTop(PIConfig.editorAlwaysOnTop);
        updateTree();
        reloading = false;
    }

    private void updateTree() {
        //Create a list of all currently expanded pages.
//        java.util.List<String> expanded = new ArrayList<>();
//        for (int i = 0; i < pageTree.getRowCount(); i++) {
//            Object comp = pageTree.getPathForRow(i).getLastPathComponent();
//            if (pageTree.isExpanded(pageTree.getPathForRow(i))) {
//                expanded.add(comp.toString());
//                LogHelper.dev("Add: " + comp);
//            }
//        }
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
//        for (String modid : DocumentationManager.getModStructureMap().keySet()) {
//            Map<String, ModDescriptorPage> modLangMap = DocumentationManager.getModStructureMap().get(modid);
//            if (modLangMap.containsKey(lang)) {
//                modsNode.add(loadModPages(modLangMap.get(lang)));
//            }
//        }

        Map<String, ModStructurePage> structureMap = DocumentationManager.getModStructureMap();
        for (String modid : structureMap.keySet()) {
            modsNode.add(loadModPages(structureMap.get(modid)));
        }

        treeModel.reload();

        //Reset the tree state to what it was before the reload
        streamTree(pageTree).forEach(pageTree::expandRow);
//        streamTree(pageTree, pageTree::expandRow);
//        int row = 0;
//        do {
//            pageTree.expandRow(row);
//        } while (row++ < pageTree.getRowCount());

//        for (int i = pageTree.getRowCount() - 1; i > 0; i--) {
//            Object component = pageTree.getPathForRow(i).getLastPathComponent();
//            if (selectedPageURI != null && component instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) component).getUserObject() == selectedPageURI) {
//                keepSelection = true;
//                pageTree.setSelectionRow(i);
//            }
//            String node = component.toString();
//            if (!expanded.contains(node) && !node.equals("Root Page")) {
//                pageTree.collapseRow(i);
//            }
//        }

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
            String node = pageTree.getPathForRow(row).getLastPathComponent().toString();
            if (!expanded.contains(node) && !node.equals("Root Page")) {
                pageTree.collapseRow(row);
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
        for (DocumentationPage subPage : page.getSubPages()) {
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
        DocumentationManager.saveModToDisk(modPage);
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
        DocumentationManager.saveModToDisk(modPage);
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
        DocumentationManager.saveModToDisk(modPage);
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

        DocumentationManager.deleteMod(page.getModid());
        DocumentationManager.checkAndReloadDocFiles();
    }

    //Page Fields

    private void weightChanged(ChangeEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null) {
            page.setSortingWeight((Integer) weightField.getValue());
        }
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

    private void enRevChange(ChangeEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null) {
            page.setTargetEnRev((Integer) enRevField.getValue());
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
        DocumentationPage page = getSelected();
        if (page == null) return;
        String id = JOptionPane.showInputDialog(this, "Please enter the id for the new page. Id should use \"snake_case\"\nformatting and should be based on the content this page is for.\ne.g. a page for Draconium Ore would use the id draconium_ore", "Choose page ID", JOptionPane.PLAIN_MESSAGE);
        if (id == null) return;

        id = id.toLowerCase().replaceAll(" ", "_");

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Page id can not be empty!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DocumentationManager.getPage(page.getPageURI() + "/" + id) != null) {
            JOptionPane.showMessageDialog(this, "The selected page already contains a sub page with this id!\nPlease choose a different id.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = JOptionPane.showInputDialog(this, "Please choose a display name for this page.", "Choose page Name", JOptionPane.PLAIN_MESSAGE);
        if (name == null) {
            return;
        }

        DocumentationPage newPage = new DocumentationPage(page, page.getModid(), page.getModVersion());
        page.getSubPages().add(newPage);
        newPage.setPageId(id);
        newPage.generatePageURIs(page.getPageURI(), new HashMap<>());
        LanguageManager.setPageName(page.getModid(), newPage.getPageURI(), name, LanguageManager.getUserLanguage());
        DocumentationManager.checkAndReloadDocFiles();
    }

    private void newModAction(ActionEvent evt) {
        UINewMod ui = new UINewMod(this, Maps.filterEntries(ModHelper.getModNameMap(), input -> input != null && !PIHelpers.getSupportedMods().contains(input.getKey())));
        PIHelpers.centerWindowOnMC(ui);
        ui.setVisible(true);

        if (!ui.isCanceled()) {
            try {
                DocumentationManager.addMod(ui.getModID(), ui.getModName(), ui.getModVersion());
            }
            catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage(), "An Error Occurred!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeLangAction(ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void updatePageId(ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void importContentFromLang(ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "This feature is not yet implemented!", "NYI", JOptionPane.ERROR_MESSAGE);
    }

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

        String message = "This dialog allows you to add an icon to this page.\n" +
                "Avalible icon types are Item Stacks, Entities, Player's and images.\n" +
                "Images must be hosted online. Only http addresses are currently supported.\n";

        Map<String, java.util.List<String>> map = new HashMap<>();
        map.put("image", new ArrayList<>());
        SelectIcon selector = createSelectDialog(map, message, "stack", "");
        selector.setVisible(true);
        if (selector.canceled || selector.getIconString().isEmpty()) {
            return;
        }

        Icon icon = new Icon(Icon.IconType.get(selector.getIconType()), selector.getIconString());
        page.getIcons().add(icon.save());
        String selected = selectedPageURI;
        page.saveToDisk();
        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(selected));
    }

    private void editIcon(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        Icon selected = iconList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an icon to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = "This dialog allows you to edit an icon on this page.\n" +
                "Avalible icon types are Item Stacks, Entities, Player's and images.\n" +
                "Images must be hosted online. Only http addresses are currently supported.\n";


        Map<String, java.util.List<String>> map = new HashMap<>();
        map.put("image", new ArrayList<>());
        SelectIcon selector = createSelectDialog(map, message, selected.type.name().toLowerCase(), selected.iconString);
        selector.setVisible(true);
        if (selector.canceled || selector.getIconString().isEmpty()) {
            return;
        }

        page.getIcons().remove(selected.save());

        String isp = selector.getIconType().equals("player") ? "player:" : "";
        Icon icon = new Icon(Icon.IconType.get(selector.getIconType()), isp + selector.getIconString());
        page.getIcons().add(icon.save());
        String sp = selectedPageURI;
        page.saveToDisk();
        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(sp));
    }

    private void removeIcon(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            return;
        }

        Icon selected = iconList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an icon to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        page.getIcons().remove(selected.save());
        String sp = selectedPageURI;
        page.saveToDisk();
        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(sp));
    }

    //endregion

    //region Relations

    private void addRelation(ActionEvent evt) {
        DocumentationPage page = getSelected();
        JOptionPane.showMessageDialog(this, "Relations are not yet implemented!", "NYI", JOptionPane.ERROR_MESSAGE);
        if (page == null || true) {
            return;
        }

        String message = "This dialog allows you to add a relation for this page.\n" +
                "Relations are stacks or entities that are related to this page in some way.\n" +
                "This is used to link eo a page by pressing the info button while hovering over an item in inventory.\n" +
                "Required format is\n" +
                "stack|minecraft:stone\n" +
                "stack|minecraft:stone,<count>,<meta>,{<nbt>}\n" +
                "entity|minecraft:zombie";

        Map<String, java.util.List<String>> map = new HashMap<>();
        SelectIcon selector = createSelectDialog(map, message, "stack", "");
        selector.setVisible(true);
        if (selector.canceled || selector.getIconString().isEmpty()) {
            return;
        }

        page.getLinked().add(selector.getIconType()+":"+selector.getIconString());
        String selected = selectedPageURI;
        page.saveToDisk();
        DocumentationManager.checkAndReloadDocFiles();
        setSelectedPage(DocumentationManager.getPage(selected));
    }

    private void editRelation(ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void removeRelation(ActionEvent evt) {
        // TODO add your handling code here:
    }

    //endregion

    private SelectIcon createSelectDialog(Map<String, java.util.List<String>> map, String message, String startType, String startContent) {
        if (!map.containsKey("stack")) map.put("stack", PIHelpers.getPlayerInventory());
        if (!map.containsKey("entity")) map.put("entity", PIHelpers.getEntitySelectionList());
        map.put("player", new ArrayList<>());
        return new SelectIcon(this, message, map, startType, startContent);
    }

    //endregion

    //region General/Selection

    private void pageSelected(TreeSelectionEvent evt) {
        Object component = pageTree.getLastSelectedPathComponent();
        if (component instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) component).getUserObject() instanceof TreePageContainer) {
            setSelectedPage(((TreePageContainer) ((DefaultMutableTreeNode) component).getUserObject()).getPage());
        }
        else { setSelectedPage(null); }
    }

    private void setSelectedPage(@Nullable DocumentationPage page) {
        reloading = true;
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
            modNameField.setText(page.getModPageName());
            modIdField.setText(modid);
            modVersionSelect.removeAllItems();
            modVersionSelect.addItem("[Default-Best-Match]");
            if (versions != null) versions.forEach(version -> modVersionSelect.addItem(version));
            modVersionSelect.setSelectedItem(page.getModVersion());

            //Page Fields
            nameField.setText(isModPage ? "" : page.getDisplayName());
            idField.setText(isModPage ? "" : page.getPageId());
            weightField.setValue(page.getSortingWeight());
            revisionField.setValue(page.getRevision());
            cycleIcons.setSelected(page.cycle_icons());
            toggleHidden.setSelected(page.isHidden());

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

        reloadLists(page instanceof ModStructurePage);
        reloading = false;
    }

    private void readMDFile(DocumentationPage page) {
        File mdFile = page.getMarkdownFile();
        boolean set = false;

        try {
            markdownWindow.setText(new String(Files.readAllBytes(mdFile.toPath())));
            set = true;
        }
        catch (IOException ignored) {}

        if (!set) {
            markdownWindow.setText("");
        }
    }

    private void reloadLists(boolean modPage) {
        DocumentationPage page = getSelected();
        iconListModel.clear();
        relationListModel.clear();
        aliasListModel.clear();

        if (page == null) {
            return;
        }

        page.getIcons().forEach(jsonObject -> iconListModel.addElement(Icon.load(jsonObject)));

        ModStructurePage sp = DocumentationManager.getModPage(page.getModid());
        if (sp != null) {
            sp.getModAliases().forEach(alias -> aliasListModel.addElement(alias));
        }

        if (!modPage) {
            page.getLinked().forEach(s -> relationListModel.addElement(s));
        }
    }

    private void deletePage(ActionEvent evt) {
        DocumentationPage page = getSelected();
        if (page != null && !(page instanceof ModStructurePage) && JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this page and its sub pages? \"" + page.getDisplayName() + "\" This can not be undone!", "Confirm Delete", JOptionPane.YES_NO_OPTION) == 0) {
            page.deletePage();
            reload();
            GuiProjectIntelligence.requiresReload = true;
        }
    }

    //endregion

    //<editor-fold defaultstate="collapsed" desc=" MD Editing ">

    private void mdTextChange(KeyEvent evt) {
        DocumentationPage page = getSelected();
        if (page == null) {
            markdownWindow.setText("");
        }
        else {
            try {
                page.setRawMarkdown(markdownWindow.getText());
            }
            catch (DocumentationPage.MDException e) {
                JOptionPane.showMessageDialog(this, "An error occurred!\n\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void insertAction(ActionEvent evt) {
        // TODO add your handling code here:
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Help Info ">

    private void helpAction(ActionEvent evt) {
        System.out.println(evt.getActionCommand());
        reload();
    }

    //</editor-fold>

    //region Misc


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

    public static class Icon {

        public IconType type = IconType.STACK;
        public String iconString = "";

        public Icon() {}

        public Icon(IconType type, String iconString) {
            this.type = type;
            this.iconString = iconString;
        }

        @Override
        public String toString() {
            return type + " | " + iconString;
        }

        public static Icon load(JsonObject obj) {
            Icon icon = new Icon();
            if (JsonUtils.isString(obj, "type")) {
                icon.type = IconType.get(JsonUtils.getString(obj, "type"));
            }
            if (JsonUtils.isString(obj, "icon_string")) {
                icon.iconString = JsonUtils.getString(obj, "icon_string");
            }
            return icon;
        }

        public JsonObject save() {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", type.name().toLowerCase());
            obj.addProperty("icon_string", iconString);
            return obj;
        }

        public enum IconType {
            STACK,
            ENTITY,
            PLAYER,
            IMAGE;

            public static IconType get(String name) {
                if (name.toLowerCase().equals("entity")) {
                    return ENTITY;
                }
                else if (name.toLowerCase().equals("player")) {
                    return ENTITY;
                }
                else if (name.toLowerCase().equals("image")) {
                    return IMAGE;
                }
                return STACK;
            }
        }
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
        public boolean isRootPage;

        public TreePageContainer(DocumentationPage page) {
            this.pageURI = page.getPageURI();
            this.isRootPage = page instanceof ModStructurePage;
        }

        private String getSelectedVersionRange() {
            DocumentationPage page = getPage();
            if (page == null) {
                return "[Invalid-Entry]";
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
            if (isRootPage) {
                return page.toString() + " (" + getSelectedVersionRange() + ")";
            }
            return page.toString();
        }

        public DocumentationPage getPage() {
            return DocumentationManager.getPage(pageURI);
        }
    }

    //endregion

    // <editor-fold defaultstate="collapsed" desc="=== Generated Variables ===">
    private JList<String> aliasList;
    private JCheckBoxMenuItem alwaysOnTop;
    private JMenuItem changeLangButton;
    private JCheckBox copyDocFromSelected;
    private JCheckBox cycleIcons;
    private JSpinner enRevField;
    private JList<Icon> iconList;
    private JLabel iconsLabel;
    private JTextField idField;
    private JLabel idLabel;
    private JButton jButton1;
    private JButton jButton10;
    private JButton jButton2;
    private JButton jButton3;
    private JButton jButton4;
    private JButton jButton5;
    private JButton jButton6;
    private JButton jButton7;
    private JButton jButton8;
    private JButton jButton9;
    private JLabel jLabel1;
    private JMenu jMenu1;
    private JMenu jMenu2;
    private JMenu jMenu3;
    private JMenuBar jMenuBar1;
    private JMenuItem jMenuItem1;
    private JPanel jPanel3;
    private JScrollPane jScrollPane2;
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
    private JList<String> relationList;
    private JLabel relationsLabel;
    private JLabel relationsLabel1;
    private JSpinner revisionField;
    private JLabel revisionLabel;
    private JTabbedPane tabbedPain;
    private JLabel targetEnRev;
    private JCheckBox toggleHidden;
    private JPopupMenu treePopup;
    private JButton updateEnRev;
    private JSpinner weightField;
    private JLabel weightLabel;
    //</editor-fold>
}
