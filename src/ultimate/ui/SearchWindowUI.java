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
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.beans.*;
import ultimate.ui.results.*;
import properties.AppProperties;
import ultimate.ui.downloadmanager.DownloadRecords;

/**
 *
 * @author Anaphase21
 */
public class SearchWindowUI extends JPanel implements ActionListener, PropertyChangeListener, KeyListener{
    JButton searchButton;
    JButton directoryButton;
    JTextField textField;
    JTextField currentDirectoryField;
    public static String searchQuery;
    JFileChooser fileChooser;
    public String currentDirectory;
    SearchResultsWindow window;
    public MainWindow mainWin;
    final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public SearchWindowUI(MainWindow mainWin){
        this.mainWin = mainWin;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        Border margin = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        if(AppProperties.curDirectory == null){
            fileChooser = new JFileChooser();
        }else{
            fileChooser = new JFileChooser(new File(AppProperties.curDirectory));
        }
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setBorder(margin);
    }
    public void setUIComponents(){
        add(createTextFieldFileChooserPanel("Search"));
        add(Box.createRigidArea(new Dimension(0, 10)));
        searchButton = new JButton("<html><a href=\" \">Search</a></html");
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setMaximumSize(new Dimension(75, 25));
        add(searchButton);
        searchButton.addActionListener(this);
    }
    
    public String getText(){
        return textField.getText();
    }
    
    public void setDirectory(String newValue){
        String oldValue = currentDirectory;
        currentDirectory = newValue;
        pcs.firePropertyChange("directory1", oldValue, newValue);
    }
    
    private JPanel createTextFieldFileChooserPanel(String label){
        JPanel textFieldPanel = new JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.LINE_AXIS));
        textField = new JTextField(20){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setColor(new Color(90, 12, 180, 80));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        textField.addKeyListener(this);
        textFieldPanel.setOpaque(true);
        textFieldPanel.setBackground(new Color(255, 255, 255, 98));
        textField.setMaximumSize(new Dimension(580, 25));
        textFieldPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel jlabel = new JLabel(label){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        jlabel.setOpaque(false);    
        textFieldPanel.add(jlabel);
        textFieldPanel.add(Box.createRigidArea(new Dimension(27, 0)));
        textFieldPanel.add(textField);
        JPanel textFieldFileChooserPanel = new JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        textFieldFileChooserPanel.setOpaque(true);
        textFieldFileChooserPanel.setBackground(new Color(255, 255, 255, 98));
        textFieldFileChooserPanel.setLayout(new BoxLayout(textFieldFileChooserPanel, BoxLayout.PAGE_AXIS));
        textFieldFileChooserPanel.add(textFieldPanel);
        textFieldFileChooserPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textFieldFileChooserPanel.add(createFileChooserPanel());
        return textFieldFileChooserPanel;
    }
    
    private JPanel createFileChooserPanel(){
        DownloadRecords.openDirectory();
        currentDirectoryField = new JTextField(20){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setColor(new Color(8, 25, 180, 80));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        currentDirectoryField.setEditable(false);
        if(AppProperties.curDirectory != null){
            currentDirectoryField.setText(AppProperties.curDirectory);
        }
        currentDirectoryField.setMaximumSize(new Dimension(580, 25));
        directoryButton = new JButton("Set Path");
        JPanel fileChooserPanel = new JPanel();
        fileChooserPanel.setOpaque(true);
        fileChooserPanel.setBackground(new Color(100, 100, 100, 98));
        directoryButton.addActionListener(this);
        fileChooserPanel.setLayout(new BoxLayout(fileChooserPanel, BoxLayout.LINE_AXIS));
        fileChooserPanel.add(directoryButton);
        fileChooserPanel.add(currentDirectoryField);
        return fileChooserPanel;
    }
    
    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() == searchButton){
           searchQuery = getText();
           window = new SearchResultsWindow(this);
           window.addPropertyChangeListener(this);
           window.setComponents();
        }else if(event.getSource() == directoryButton){
            int returnValue = fileChooser.showSaveDialog(this);
            if(returnValue == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                String dir = file.getPath()+File.separator;
                currentDirectoryField.setText(dir);
                setDirectory(dir);
                AppProperties.curDirectory = dir;
                fileChooser = new JFileChooser(new File(dir));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                DownloadRecords.saveDirectory(dir);
            }
        }
        updateUI();
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.pcs.addPropertyChangeListener(listener);
    }
    
    @Override
    public void paintComponent(Graphics g){
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D)g.create();
    g2d.drawImage(AppProperties.backgroundImg, 0, 0, null);
    g2d.dispose();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if("addNotice".equals(property)){
            addNotice((SearchResultsPanel)event.getNewValue());
        }
    }
    
    void addNotice(SearchResultsPanel sp){
        pcs.firePropertyChange("addNotice", null, sp);
    }
    
    @Override
    public void keyPressed(KeyEvent event){
        if(event.getKeyCode() == KeyEvent.VK_ENTER){
            searchQuery = getText();
            window = new SearchResultsWindow(this);
            window.addPropertyChangeListener(this);
            window.setComponents();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent event){
        
    }
    
    @Override
    public void keyTyped(KeyEvent event){
        
    }
}
