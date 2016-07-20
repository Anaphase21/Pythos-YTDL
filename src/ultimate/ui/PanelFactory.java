/*
 * Copyright (C) 2016 Anaphase21
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ultimate.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import ultimate.ui.results.SearchResultsWindow;

/**
 *
 * @author Anaphase21
 */
public class PanelFactory{
    static JTextField textField;
    //non-initializable constructor
    private PanelFactory(){
    }
    
    public static JPanel createResolsPanel(CustomRadioButton[] resolutions, ActionListener listener){
        JPanel resolutionsPanel = new JPanel(new GridLayout(0, 3));
        resolutionsPanel.setOpaque(true);
        resolutionsPanel.setBackground(new Color(100, 100, 100, 98));
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Select format");
        Border title = BorderFactory.createTitledBorder(titledBorder, "Select format", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(Font.SERIF, Font.ITALIC, 13));
        resolutionsPanel.setBorder(title);
        int length = resolutions.length;
        ButtonGroup res = new ButtonGroup();
        for(int i = 0; i < length; i++){
            resolutions[i].setOpaque(true);
            resolutions[i].setBackground(new Color(150, 150, 150, 40));
            resolutions[i].addActionListener(listener);
            res.add(resolutions[i]);
            resolutionsPanel.add(resolutions[i]);
        }
        return resolutionsPanel;
    }
    
    public static JPanel createFileChooserPanel(){
        JTextField fileChooser = new JTextField(20);
        fileChooser.setMaximumSize(new Dimension(580, 25));
        final JButton directoryButton = new JButton("Set Path");
        final JPanel fileChooserPanel = new JPanel();
        fileChooserPanel.setOpaque(true);
        fileChooserPanel.setBackground(new Color(100, 100, 100, 98));
        directoryButton.addActionListener(new SearchWindowUI());
        fileChooserPanel.setLayout(new BoxLayout(fileChooserPanel, BoxLayout.LINE_AXIS));
        fileChooserPanel.add(directoryButton);
        fileChooserPanel.add(fileChooser);
        return fileChooserPanel;
    }
    
    public static JPanel createTextFieldFileChooserPanel(String label){
        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.LINE_AXIS));
        PanelFactory.textField = new JTextField(20);
        textFieldPanel.setOpaque(true);
        textFieldPanel.setBackground(new Color(100, 100, 100, 98));
        PanelFactory.textField.setMaximumSize(new Dimension(580, 25));
        textFieldPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel jlabel = new JLabel(label);
        jlabel.setOpaque(false);    
        textFieldPanel.add(jlabel);
        textFieldPanel.add(Box.createRigidArea(new Dimension(27, 0)));
        textFieldPanel.add(PanelFactory.textField);
        JPanel textFieldFileChooserPanel = new JPanel();
        textFieldFileChooserPanel.setOpaque(true);
        textFieldFileChooserPanel.setBackground(new Color(100, 100, 100, 98));
        textFieldFileChooserPanel.setLayout(new BoxLayout(textFieldFileChooserPanel, BoxLayout.PAGE_AXIS));
        textFieldFileChooserPanel.add(textFieldPanel);
        textFieldFileChooserPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textFieldFileChooserPanel.add(PanelFactory.createFileChooserPanel());
        return textFieldFileChooserPanel;
    }
    public static JPanel createDownloadAndResetButtonsPanel(JButton downloadButton, JButton resetButton){
        JPanel downloadResetButtons = new JPanel();
        downloadResetButtons.setOpaque(true);
        downloadResetButtons.setBackground(new Color(100, 100, 100, 98));
        downloadResetButtons.setLayout(new BoxLayout(downloadResetButtons, BoxLayout.LINE_AXIS));
        downloadResetButtons.add(downloadButton);
        downloadResetButtons.add(Box.createRigidArea(new Dimension(10, 0)));
        downloadResetButtons.add(resetButton);
        return downloadResetButtons;
    }
    
    public static JPanel createProgressPanel(String label, boolean transparent){
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(200, 15));
        JPanel progress = new JPanel();
        if(transparent){
        progress.setOpaque(true);
        progress.setBackground(new Color(100, 100,100, 0));
        }
        progress.setLayout(new BoxLayout(progress, BoxLayout.PAGE_AXIS));
        progress.add(Box.createRigidArea(new Dimension(40, 0)));
        progress.add(new JLabel(label));
        progress.add(Box.createRigidArea(new Dimension(20, 0)));
        progress.add(Box.createRigidArea(new Dimension(0, 10)));
        progress.add(progressBar);
        progress.add(Box.createRigidArea(new Dimension(0, 20)));
        return progress;
    }
    
    public static JPanel createCloseButtonPanel(JTabbedPane pane, String title){
        JPanel closePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        final JTabbedPane tabs = pane;
        final String title1 = title;
        JButton closeButton = new JButton(){
            private final MouseListener listener = new MouseAdapter(){
                @Override
                public void mouseEntered(MouseEvent event){
                    setBorderPainted(true);
                }
                
                @Override
                public void mouseExited(MouseEvent event){
                    setBorderPainted(false);
                }
            };
            {
                setBorderPainted(false);
                setUI(new javax.swing.plaf.basic.BasicButtonUI());
                setPreferredSize(new Dimension(20, 20));
                setContentAreaFilled(false);
                setRolloverEnabled(true);
                setFocusable(false);
                setToolTipText("Close tab");
                Border etchedBorder = BorderFactory.createEtchedBorder();
                setBorder(etchedBorder);
                addMouseListener(listener);
            }
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g.create();
                if(getModel().isPressed()){
                    g2d.translate(1,1);
                }
                g2d.setColor(new Color(55, 55, 55));
                if(getModel().isRollover()){
                    g2d.setColor(Color.MAGENTA);
                }
                g2d.drawLine(6, 6, getWidth()-6, getHeight()-6);
                g2d.drawLine(getWidth()-6, 6, 6, getHeight()-6);
                g2d.dispose();
            }
        };
        closeButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event){
                int i = tabs.indexOfTab(title1);
                tabs.removeTabAt(i);
                if(i == 0){
                    SearchResultsWindow.newTabNum = 2;
                }else{
                    SearchResultsWindow.newTabNum--;
                }
            }
        });
        closePane.add(new JLabel(title));
        closePane.add(closeButton);
        return closePane;
    }
}
