package com.brandon3055.projectintelligence.client.gui.swing;

import com.brandon3055.projectintelligence.client.PIGuiHelper;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Supplier;

/**
 * @author brandon3055
 */
public class MDTagDialog extends JDialog {

    private TagType type;

    public MDTagDialog(Frame parent, TagType type) {
        super(parent, true);
        this.type = type;
        initComponents();
        setTitle("Create " + type.name().toLowerCase() + " Tag");

        switch (type) {
            case RECIPE:
                setColour1("border_colour:");
                colourField1.setText("-1");
                setColour2("border_colour_hover:");
                colourField2.setText("-1");
                setText1("Stack String:");
                setSpinner1("padding:");
                spinner1.setToolTipText("Sets uniform padding around the recipe");
                setSpinner2("top_pad:");
                setSpinner3("left_pad:");
                setSpinner4("spacing:");
                spinner4.setValue(4);
                setSpinner5("bottom_pad:");
                setSpinner6("right_pad:");

                break;
            case LINK:

                setColour1("colour:");
//                colourField1.setText("0x4444FF");
                setColour2("colour_hover:");
//                colourField2.setText("0xFF69B4");
                setColour3("border_colour:");
                colourField3.setToolTipText("Used only for solid button style link");
                setColour4("border_colour_hover:");
                colourField4.setToolTipText("Used only for solid button style link");

                setText1("Link Target");
                textField1.setToolTipText("<html>This target can ether be a web link e.g. http://google.com or a link to another page e.g. draconicevolution:fusionCrafting<br>You can get the address of a page by right clicking it in the page tree.</html>");
                setText2("alt_text");
                textField2.setToolTipText("If specified this is the text that will be displayed in place of the link text");
                setSpinner1("padding:");
                setSpinner2("top_pad:");
                setSpinner3("left_pad:");
                setSpinner5("bottom_pad:");
                setSpinner6("right_pad:");
                setStyle("render:");
                break;
            case RULE:
                setColour1("colour:");
                setText1("width:");
                textField1.setText("100%");
                textField1.setToolTipText("This field accepts ether a fixed width or a percentage, thats a percentage of the screen width");
                setSpinner1("height:");
                spinner1.setValue(5);
                setSpinner2("top_padding:");
                spinner2.setToolTipText("Adds blank space above the rule");
                setSpinner3("bottom_padding:");
                spinner2.setToolTipText("Adds blank space bellow the rule");
                break;
            case TABLE:

                setText1("width:");
                textField1.setText("100%");
                textField1.setToolTipText("This field accepts ether a fixed width or a percentage, thats a percentage of the screen width");
                //setAlign("align:");
                alignSelector.setToolTipText("Sets the alignment of the table itself. Check the table documentation for info on aligning specific columns in the table");
                setVertAlign("vert_align:");
                vertAlignSelector.setToolTipText("Sets the vertical alignment of content in cells that are taller than required for the content.");
                setColour1("border_colour:");
                colourField1.setToolTipText("Sets the colour of the table cell renderer.");
                setColour2("heading_colour");
                colourField2.setToolTipText("Sets the colour of the heading cell (if a heading cell exists)");
                setRender("render_cells");
                renderCheck.setToolTipText("Can be used to completely disable the table cell renderer. In this mode the table can be used as a layout/position tool to layout a group of elements.");

                setSpinner1("rows");
                spinner1.setValue(3);
                setSpinner2("columns");
                spinner2.setValue(3);

                break;
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                previewField.setText("");
            }
        });

        pack();
        updateOutput();
        PIGuiHelper.centerWindowOn(this, parent);
    }

    public void setStack(String stack) {
        textField1.setText(stack);
        updateOutput();
    }

    private void insert(ActionEvent evt) {
        dispose();
    }

    private void cancel(ActionEvent evt) {
        previewField.setText("");
        dispose();
    }

    private void initComponents() {

        jPanel1 = new JPanel();
        jTextArea1 = new JTextArea();
        spinnerLabel1 = new JLabel();
        spinner1 = new JSpinner();
        spinnerLabel2 = new JLabel();
        spinner2 = new JSpinner();
        spinnerLabel3 = new JLabel();
        spinner3 = new JSpinner();
        spinnerLabel5 = new JLabel();
        spinner5 = new JSpinner();
        spinnerLabel6 = new JLabel();
        spinner6 = new JSpinner();
        colourLabel2 = new JLabel();
        colourField2 = new JTextField();
        colourField1 = new JTextField();
        colourLabel1 = new JLabel();
        colourField4 = new JTextField();
        colourLabel4 = new JLabel();
        colourField3 = new JTextField();
        colourLabel3 = new JLabel();
        jSeparator1 = new JSeparator();
        jLabel10 = new JLabel();
        jSeparator2 = new JSeparator();
        textLabel1 = new JLabel();
        textField1 = new JTextField();
        textField2 = new JTextField();
        textLabel2 = new JLabel();
        styleSelector = new JComboBox<>();
        styleLabel = new JLabel();
        spinnerLabel4 = new JLabel();
        spinner4 = new JSpinner();
        jSeparator3 = new JSeparator();
        jButton1 = new JButton();
        jButton2 = new JButton();
        alignSelector = new JComboBox<>();
        alignLabel = new JLabel();
        vertAlignSelector = new JComboBox<>();
        vertAlignLabel = new JLabel();
        renderCheck = new JCheckBox();
        jScrollPane1 = new JScrollPane();
        previewField = new JTextArea();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(470, 0));
        setModal(true);
        setResizable(false);

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new Color(60, 63, 65));
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(2);
        jTextArea1.setText("This dialog allows you to prefigure a markdown tag before it is added to the page.\nYou can manually edit any of these values later once the tag has been created.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setBorder(null);
        jTextArea1.setFocusable(false);
        jTextArea1.setOpaque(false);

        spinnerLabel1.setText("padding:");
        spinnerLabel1.setVisible(false);

        spinner1.setVisible(false);
        spinner1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        spinnerLabel2.setText("top_pad:");
        spinnerLabel2.setVisible(false);

        spinner2.setVisible(false);
        spinner2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        spinnerLabel3.setText("left_pad:");
        spinnerLabel3.setVisible(false);

        spinner3.setVisible(false);
        spinner3.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        spinnerLabel5.setText("bottom_pad:");
        spinnerLabel5.setVisible(false);

        spinner5.setVisible(false);
        spinner5.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        spinnerLabel6.setText("right_pad:");
        spinnerLabel6.setVisible(false);

        spinner6.setVisible(false);
        spinner6.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        colourLabel2.setText("colour2");
        colourLabel2.setVisible(false);

        colourField2.setVisible(false);
        colourField2.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                fieldChange(evt);
            }
        });

        colourField1.setVisible(false);
        colourField1.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                fieldChange(evt);
            }
        });

        colourLabel1.setText("colour1");
        colourLabel1.setVisible(false);

        colourField4.setVisible(false);
        colourField4.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                fieldChange(evt);
            }
        });

        colourLabel4.setText("colour3");
        colourLabel4.setVisible(false);

        colourField3.setVisible(false);
        colourField3.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                fieldChange(evt);
            }
        });

        colourLabel3.setText("colour3");
        colourLabel3.setVisible(false);

        jLabel10.setText("Tag Preview:");

        textLabel1.setText("Alt Text:");
        textLabel1.setVisible(false);

        textField1.setVisible(false);
        textField1.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                fieldChange(evt);
            }
        });

        textField2.setVisible(false);
        textField2.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                fieldChange(evt);
            }
        });

        textLabel2.setText("Hover Text:");
        textLabel2.setVisible(false);

        styleSelector.setVisible(false);
        styleSelector.setModel(new DefaultComboBoxModel<>(new String[]{"text", "vanilla", "solid"}));
        styleSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                itemChange(evt);
            }
        });

        styleLabel.setText("Style:");
        styleLabel.setVisible(false);

        spinnerLabel4.setText("spacing:");
        spinnerLabel4.setVisible(false);

        spinner4.setVisible(false);
        spinner4.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        jButton1.setText("Cancel");
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancel(evt);
            }
        });

        jButton2.setText("Insert");
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insert(evt);
            }
        });

        alignSelector.setVisible(false);
        alignSelector.setModel(new DefaultComboBoxModel<>(new String[]{"left", "center", "right"}));
        alignSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                itemChange(evt);
            }
        });

        alignLabel.setText("Align:");
        alignLabel.setVisible(false);

        vertAlignSelector.setVisible(false);
        vertAlignSelector.setModel(new DefaultComboBoxModel<>(new String[]{"top", "middle", "bottom"}));
        vertAlignSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                itemChange(evt);
            }
        });

        vertAlignLabel.setText("Vert Align:");
        vertAlignLabel.setVisible(false);

        renderCheck.setSelected(true);
        renderCheck.setText("render");
        renderCheck.setVisible(false);

        previewField.setColumns(20);
        previewField.setRows(5);
        previewField.setText("test");
        jScrollPane1.setViewportView(previewField);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jTextArea1).addComponent(jSeparator1, GroupLayout.Alignment.TRAILING).addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(spinnerLabel1).addComponent(spinnerLabel4)).addGap(22, 22, 22).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(spinner4, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE).addComponent(spinner1, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(spinnerLabel2).addComponent(spinnerLabel5)).addGap(22, 22, 22).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(spinner2, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE).addComponent(spinner5, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)).addGap(30, 30, 30).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(spinnerLabel6).addComponent(spinnerLabel3)).addGap(23, 23, 23).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(spinner6, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE).addComponent(spinner3, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE))).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(colourLabel1).addComponent(colourLabel3)).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(colourField1, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE).addComponent(colourField3, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(colourLabel4).addComponent(colourLabel2)).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(colourField2, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE).addComponent(colourField4, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)).addGap(8, 8, 8)).addComponent(jSeparator2).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(textLabel2).addComponent(textLabel1)).addGap(10, 10, 10).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(textField1).addComponent(textField2))).addComponent(jSeparator3).addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jButton2).addGap(18, 18, 18).addComponent(jButton1)).addGroup(jPanel1Layout.createSequentialGroup().addComponent(styleLabel).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(styleSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(alignLabel).addGap(5, 5, 5).addComponent(alignSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(vertAlignLabel).addGap(5, 5, 5).addComponent(vertAlignSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(renderCheck).addGap(0, 0, Short.MAX_VALUE)).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel10).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jScrollPane1))).addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jTextArea1, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(colourLabel1).addComponent(colourField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(colourLabel3).addComponent(colourField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(colourLabel2).addComponent(colourField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(colourLabel4).addComponent(colourField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 4, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(spinnerLabel2).addComponent(spinner2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(spinnerLabel3).addComponent(spinner3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(spinnerLabel1).addComponent(spinner1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(spinnerLabel5).addComponent(spinner5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(spinnerLabel6).addComponent(spinner6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(spinnerLabel4).addComponent(spinner4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(textLabel1).addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(textLabel2).addComponent(textField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(vertAlignSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(vertAlignLabel).addComponent(renderCheck)).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(alignSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(alignLabel)).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(styleSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(styleLabel))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jSeparator3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel10).addGap(0, 0, Short.MAX_VALUE))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(jButton1).addComponent(jButton2)).addContainerGap()));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));

        setBounds(0, 0, 486, 484);
    }

    private void updateOutput() {
        StringBuilder tag = new StringBuilder();
        String value = "";
        String options = "";
        String colour1 = colourField1.getText();
        String colour2 = colourField2.getText();
        String colour3 = colourField3.getText();
        String colour4 = colourField4.getText();
        int p1 = (int) spinner1.getValue();
        int p2 = (int) spinner2.getValue();
        int p3 = (int) spinner3.getValue();
        int p4 = (int) spinner4.getValue();
        int p5 = (int) spinner5.getValue();
        int p6 = (int) spinner6.getValue();

        switch (type) {
            case RECIPE:
                tag = new StringBuilder("\u00a7recipe");
                value = textField1.getText();
                options = addIf(options, "border_colour:" + colour1, () -> !(colour1.equals("-1") || colour1.isEmpty()));
                options = addIf(options, "border_colour_hover:" + colour2, () -> !(colour2.equals("-1") || colour2.isEmpty()));
                options = addIf(options, "padding:" + p1, () -> p1 > 0);
                options = addIf(options, "top_pad:" + p2, () -> p2 > 0);
                options = addIf(options, "left_pad:" + p3, () -> p3 > 0);
                options = addIf(options, "spacing:" + p4, () -> p4 > 0);
                options = addIf(options, "bottom_pad:" + p5, () -> p5 > 0);
                options = addIf(options, "right_pad:" + p6, () -> p6 > 0);
                break;
            case LINK:
                tag = new StringBuilder("\u00a7link");
                value = textField1.getText();
                options = addIf(options, "alt_text:\"" + textField2.getText() + "\"", () -> !textField2.getText().isEmpty());
                options = addIf(options, "border_colour:" + colour3, () -> !(colour3.equals("-1") || colour3.isEmpty()));
                options = addIf(options, "border_colour_hover:" + colour4, () -> !(colour4.equals("-1") || colour4.isEmpty()));
                options = addIf(options, "colour:" + colour1, () -> !(colour1.equals("-1") || colour1.isEmpty()));
                options = addIf(options, "colour_hover:" + colour2, () -> !(colour2.equals("-1") || colour2.isEmpty()));
                options = addIf(options, "padding:" + p1, () -> p1 > 0);
                options = addIf(options, "top_pad:" + p2, () -> p2 > 0);
                options = addIf(options, "left_pad:" + p3, () -> p3 > 0);
                options = addIf(options, "bottom_pad:" + p5, () -> p5 > 0);
                options = addIf(options, "right_pad:" + p6, () -> p6 > 0);
                options = addIf(options, "link_style:" + styleSelector.getSelectedItem(), () -> !styleSelector.getSelectedItem().equals("text"));
                break;
            case RULE:
                tag = new StringBuilder("\u00a7rule");
                options = addIf(options, "colour:" + colour1, () -> !(colour1.equals("-1") || colour1.isEmpty()));
                options = addIf(options, "height:" + p1, () -> p1 > 0);
                options = addIf(options, "top_pad:" + p2, () -> p2 > 0);
                options = addIf(options, "bottom_pad:" + p3, () -> p3 > 0);
                options = addIf(options, "width:" + textField1.getText(), () -> true);
                options = addIf(options, "align:" + alignSelector.getSelectedItem(), () -> !alignSelector.getSelectedItem().equals("left"));
                previewField.setText(tag + "{" + options + "}");
                return;
            case TABLE:
                options = addIf(options, "border_colour:" + colour1, () -> !(colour1.equals("-1") || colour1.isEmpty()));
                options = addIf(options, "heading_colour:" + colour2, () -> !(colour2.equals("-1") || colour2.isEmpty()));
                options = addIf(options, "width:" + textField1.getText(), () -> true);
                //options = addIf(options, "align:" + alignSelector.getSelectedItem(), () -> !alignSelector.getSelectedItem().equals("left"));
                options = addIf(options, "vert_align:" + vertAlignSelector.getSelectedItem(), () -> !vertAlignSelector.getSelectedItem().equals("top"));
                tag = new StringBuilder("\u00a7table[" + options + "]");
                int rows = Math.max(1, (int) spinner1.getValue());
                int columns = Math.max(1, (int) spinner2.getValue());

                for (int i = 0; i < rows + 1; i++) {
                    StringBuilder r = new StringBuilder("|");
                    for (int c = 0; c < columns; c++) {
                        if (i == 0) {
                            r.append(" Heading ").append(c).append(" |");
                        }
                        else if (i == 1) {
                            r.append(" :-------------------- |");
                        }
                        else {
                            r.append(" <Row ").append(i).append(", Column ").append(c).append("> |");
                        }
                    }
                    tag.append("\n").append(r);
                }

                previewField.setText(tag.toString());
                return;
        }

        previewField.setText(tag + "[" + (value.isEmpty() ? "" : value) + "]" + (options.isEmpty() ? "" : "{" + options + "}"));
    }

    private void fieldChange(KeyEvent evt) {
        updateOutput();
    }

    private void spinnerChange(ChangeEvent evt) {
        updateOutput();
    }

    private void itemChange(ItemEvent evt) {
        if (type == TagType.LINK && evt.getSource() == styleSelector && styleSelector.getSelectedItem() != null) {
            if (styleSelector.getSelectedItem().equals("vanilla")) {
                colourField1.setText("0xE0E0E0");
                colourField2.setText("0xFFFFA0");
                colourField3.setText("-1");
                colourField4.setText("-1");
            }
            else if (styleSelector.getSelectedItem().equals("text")) {
                colourField1.setText("0x4444FF");
                colourField2.setText("0xFF69B4");
                colourField3.setText("-1");
                colourField4.setText("-1");
            }
            else {
                colourField1.setText("0x303030");
                colourField2.setText("0x303030");
                colourField3.setText("0xFFFFFF");
                colourField4.setText("0x00FF00");
            }

        }
        updateOutput();
    }

    public String getTag() {
        return previewField.getText();
    }

    //region Field Configurators

    private void setColour1(@Nullable String name) {
        colourField1.setVisible(name != null);
        colourLabel1.setVisible(name != null);
        colourLabel1.setText(String.valueOf(name));
    }

    private void setColour2(@Nullable String name) {
        colourField2.setVisible(name != null);
        colourLabel2.setVisible(name != null);
        colourLabel2.setText(String.valueOf(name));
    }

    private void setColour3(@Nullable String name) {
        colourField3.setVisible(name != null);
        colourLabel3.setVisible(name != null);
        colourLabel3.setText(String.valueOf(name));
    }

    private void setColour4(@Nullable String name) {
        colourField4.setVisible(name != null);
        colourLabel4.setVisible(name != null);
        colourLabel4.setText(String.valueOf(name));
    }

    private void setSpinner1(@Nullable String name) {
        spinner1.setVisible(name != null);
        spinnerLabel1.setVisible(name != null);
        spinnerLabel1.setText(String.valueOf(name));
    }

    private void setSpinner2(@Nullable String name) {
        spinner2.setVisible(name != null);
        spinnerLabel2.setVisible(name != null);
        spinnerLabel2.setText(String.valueOf(name));
    }

    private void setSpinner3(@Nullable String name) {
        spinner3.setVisible(name != null);
        spinnerLabel3.setVisible(name != null);
        spinnerLabel3.setText(String.valueOf(name));
    }

    private void setSpinner4(@Nullable String name) {
        spinner4.setVisible(name != null);
        spinnerLabel4.setVisible(name != null);
        spinnerLabel4.setText(String.valueOf(name));
    }

    private void setSpinner5(@Nullable String name) {
        spinner5.setVisible(name != null);
        spinnerLabel5.setVisible(name != null);
        spinnerLabel5.setText(String.valueOf(name));
    }

    private void setSpinner6(@Nullable String name) {
        spinner6.setVisible(name != null);
        spinnerLabel6.setVisible(name != null);
        spinnerLabel6.setText(String.valueOf(name));
    }

    private void setText1(@Nullable String name) {
        textField1.setVisible(name != null);
        textLabel1.setVisible(name != null);
        textLabel1.setText(String.valueOf(name));
    }

    private void setText2(@Nullable String name) {
        textField2.setVisible(name != null);
        textLabel2.setVisible(name != null);
        textLabel2.setText(String.valueOf(name));
    }

    private void setStyle(@Nullable String name) {
        styleSelector.setVisible(name != null);
        styleLabel.setVisible(name != null);
        styleLabel.setText(String.valueOf(name));
    }

    private void setAlign(@Nullable String name) {
        alignSelector.setVisible(name != null);
        alignLabel.setVisible(name != null);
        alignLabel.setText(String.valueOf(name));
    }

    private void setVertAlign(@Nullable String name) {
        vertAlignSelector.setVisible(name != null);
        vertAlignLabel.setVisible(name != null);
        vertAlignLabel.setText(String.valueOf(name));
    }

    private void setRender(@Nullable String name) {
        renderCheck.setVisible(name != null);
    }

    private String addIf(String ops, Object add, Supplier<Boolean> check) {
        return ops + (check.get() ? (ops.isEmpty() ? "" : ",") + add : "");
    }

    //endregion

    public enum TagType {
        RECIPE,
        LINK,
        RULE,
        TABLE
    }

    // Variables declaration - do not modify                     
    private JLabel alignLabel;
    private JComboBox<String> alignSelector;
    private JTextField colourField1;
    private JTextField colourField2;
    private JTextField colourField3;
    private JTextField colourField4;
    private JLabel colourLabel1;
    private JLabel colourLabel2;
    private JLabel colourLabel3;
    private JLabel colourLabel4;
    private JButton jButton1;
    private JButton jButton2;
    private JLabel jLabel10;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JSeparator jSeparator3;
    private JTextArea jTextArea1;
    private JTextArea previewField;
    private JCheckBox renderCheck;
    private JSpinner spinner1;
    private JSpinner spinner2;
    private JSpinner spinner3;
    private JSpinner spinner4;
    private JSpinner spinner5;
    private JSpinner spinner6;
    private JLabel spinnerLabel1;
    private JLabel spinnerLabel2;
    private JLabel spinnerLabel3;
    private JLabel spinnerLabel4;
    private JLabel spinnerLabel5;
    private JLabel spinnerLabel6;
    private JLabel styleLabel;
    private JComboBox<String> styleSelector;
    private JTextField textField1;
    private JTextField textField2;
    private JLabel textLabel1;
    private JLabel textLabel2;
    private JLabel vertAlignLabel;
    private JComboBox<String> vertAlignSelector;
    // End of variables declaration                   
}
