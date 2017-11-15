package com.intellij.uiDesigner.core;

import javax.swing.*;
import java.awt.*;


public class Spacer extends JComponent {
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    public final Dimension getPreferredSize() {
        return getMinimumSize();
    }
}