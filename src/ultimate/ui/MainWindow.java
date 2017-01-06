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
import javax.imageio.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.beans.*;
import properties.AppProperties;
import ultimate.ui.downloadmanager.*;
import ultimate.ui.results.SearchResultsPanel;

/**
 *
 * @author Anahase21
 */

public class MainWindow extends JFrame implements PropertyChangeListener, ActionListener{
    HomeUI homeUI;
    SearchWindowUI searchWindow;
    public static DownloadWindow downloadWindow;
    JTabbedPane tabs;
    JMenu menu;
    JMenuBar menuBar;
    JScrollPane scrollPane;
    JScrollPane dwnlWindowScrollPane;
    JMenuItem about;
    JMenuItem help;
    JFrame aboutHelpWindow;
    PropertyChangeSupport pcs;
    
    public MainWindow(){
        setLayout(new GridLayout());
    }
    
    public void init(){
    setTitle("Pythos YTDL v1.0.1");
    setIconImage((new ImageIcon(getClass().getResource("/res/thumbs/py.png"))).getImage());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setPreferredSize(new Dimension(650, 480));
    pcs = new PropertyChangeSupport(this);
    homeUI = new HomeUI(this);
    homeUI.downloadButton.addActionListener(this);
    searchWindow = new SearchWindowUI(this);
    downloadWindow = new DownloadWindow();
    addPropertyChangeListener(downloadWindow);
    //homeUI.setLayout(new BoxLayout(homeUI, BoxLayout.PAGE_AXIS));
    searchWindow.setUIComponents();
    searchWindow.addPropertyChangeListener(this);
    homeUI.setUIComponents();
    homeUI.addPropertyChangeListener(this);
    scrollPane = new JScrollPane(homeUI);
    scrollPane.setOpaque(true);
    scrollPane.setBackground(new Color(100, 100, 100, 98));
    scrollPane.getVerticalScrollBar().setUnitIncrement(18);//Increase the scroll speed which is always slow by default.
    scrollPane.getHorizontalScrollBar().setUnitIncrement(12);
    dwnlWindowScrollPane = new JScrollPane(downloadWindow);
    dwnlWindowScrollPane.getVerticalScrollBar().setUnitIncrement(20);//Increase the scroll speed which is always slow by default.
    tabs = new JTabbedPane();
    menu = new JMenu("Help/About");
    about = new JMenuItem("About", menuItemIcon("about"));
    about.addActionListener(this);
    help = new JMenuItem("Help", menuItemIcon("help"));
    help.addActionListener(this);
    menu.add(help);
    menu.add(about);
    menuBar = new JMenuBar();
    menuBar.add(menu);
    tabs.addTab("Home", scrollPane);
    tabs.addTab("Search", new JScrollPane(searchWindow));
    tabs.addTab("Downloads", dwnlWindowScrollPane);
    add(tabs);
    setJMenuBar(menuBar);
    //setResizable(false);
    pack();
    setLocationRelativeTo(null);
    }
    
    public void setFrame(){
        setVisible(true);
    }
    
    public void setFileExistence(){
        pcs.firePropertyChange("fileExistence", false, true);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if("directory".equals(property)){
            searchWindow.currentDirectory = (String)event.getNewValue();
            searchWindow.currentDirectoryField.setText(searchWindow.currentDirectory);
            revalidate();
            repaint();
        }else if("update".equals(property)){
            revalidate();
            repaint();
        }else if("directory1".equals(property)){
            homeUI.currentDirectory = (String)event.getNewValue();
            homeUI.currentDirectoryField.setText(homeUI.currentDirectory);
        }else if("Start".equals(property)){
            if(AppProperties.curDirectory == null){
                JOptionPane.showMessageDialog(this, "Download path not set.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File dir = new File(AppProperties.curDirectory);
            if(!dir.exists()){
                JOptionPane.showMessageDialog(this, "The currently set download path is invalid. Please set a valid path", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            setFileExistence();
            downloadWindow.addDownloadPanel(AppProperties.title, homeUI.yt.links.get(homeUI.urlKey), AppProperties.curDirectory, AppProperties.getExtension(homeUI.urlKey), homeUI.getId(), homeUI.urlKey);
            downloadWindow.updateUI();
        }else if("addNotice".equals(property)){
            if(AppProperties.curDirectory == null){
                JOptionPane.showMessageDialog(this, "Download path not set.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            File dir = new File(AppProperties.curDirectory);
            if(!dir.exists()){
                JOptionPane.showMessageDialog(this, "The currently set download path is invalid. Please set a valid path", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            setFileExistence();
            SearchResultsPanel panel = (SearchResultsPanel)event.getNewValue();
            downloadWindow.addDownloadPanel(AppProperties.title, panel.url, AppProperties.curDirectory, AppProperties.getExtension(panel.urlKey), panel.getId(), panel.urlKey);
            downloadWindow.updateUI();
        }
    }
    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() == about){
            setHelpAboutWindow(true, getAboutHelpText("about.j"));
        }else if(event.getSource() == help){
            setHelpAboutWindow(false, getAboutHelpText("help.j"));
        }
    }
    
    private String getAboutHelpText(String file){
        BufferedReader reader = null;
        String str = null;
        StringBuilder buff = new StringBuilder();
        InputStream in = null;
        try{
            in = getClass().getResourceAsStream("/res/helpabout/"+file);
            reader = new BufferedReader(new InputStreamReader(in));
            while((str = reader.readLine()) != null){
                buff.append(str).append("\n");
            }
        }catch(IOException ioe){
            return "Error while loading file...";
        }
        return buff.toString();
    }
    
    private void setHelpAboutWindow(boolean about, final String text){
        final String str = about?"About Pythos":"Help";
        aboutHelpWindow = new JFrame(str);
        JPanel aboutHelpPanel = new JPanel();
        JTextArea textArea = new JTextArea(text);
        textArea.setLineWrap(true);
        textArea.setEnabled(false);
        JScrollPane scroller = new JScrollPane(textArea);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        scroller.getVerticalScrollBar().setUnitIncrement(18);
        aboutHelpPanel.add(scroller);
        aboutHelpPanel.setLayout(new BoxLayout(aboutHelpPanel, BoxLayout.PAGE_AXIS));
        aboutHelpWindow.setPreferredSize(new Dimension(400, 340));
        aboutHelpWindow.add(aboutHelpPanel);
        aboutHelpWindow.setResizable(false);
        aboutHelpWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        aboutHelpWindow.pack();
        aboutHelpWindow.setLocationRelativeTo(null);
        aboutHelpWindow.setVisible(true);
    }
    
    public Icon menuItemIcon(String name){
        Icon menuItemIcon = null;
        try{
            Image i = ImageIO.read(new java.io.DataInputStream(getClass().getResourceAsStream("/res/thumbs/"+name+".png")));
            ImageIcon ic = new ImageIcon(i);
            menuItemIcon = ic;
        }catch(IOException ioe){
            
        }
        return menuItemIcon;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        pcs.addPropertyChangeListener(listener);
    }
}
