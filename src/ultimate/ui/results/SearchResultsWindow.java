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
package ultimate.ui.results;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import ultimate.ui.PanelFactory;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Anaphase21
 */
public class SearchResultsWindow extends JFrame implements PropertyChangeListener, ChangeListener, WindowListener{
    
    JScrollPane scrollPane;
    SearchResultsPanel panel;
    JTextField searchField;
    JViewport viewport;
    JTabbedPane tabs;
    public static int newTabNum = 2;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public SearchResultsWindow(){
        addWindowListener(this);
        setTitle("Search results");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(755, 502));
        Point point = ultimate.main.UltimateYTDL.mainWindow.getLocationOnScreen();
        setLocation(point.x+50, point.y+20);
    }
    
    public void setComponents(){
        tabs = new JTabbedPane();
        SearchTab tab = new SearchTab();
        tabs.addChangeListener(this);
        tab.panel.searchQuery = ultimate.ui.SearchWindowUI.searchQuery;
        tab.searchField.setText(ultimate.ui.SearchWindowUI.searchQuery);
        tab.pcs.addPropertyChangeListener(this);
        tab.panel.addPropertyChangeListener(this);
        tab.startSearch();
        String text = tab.searchField.getText();
        tabs.addTab(text, null, tab, text);
        int i = tabs.indexOfTab(text);
        tabs.setTabComponentAt(i, PanelFactory.createCloseButtonPanel(tabs, text));
        add(tabs);
        pack();
        setVisible(true);
    }

    void addNotice(SearchResultsPanel sp){
        pcs.firePropertyChange("addNotice", null, sp);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if("done".equals(property)){
            revalidate();
            setVisible(true);
        }else if("addNotice".equals(property)){
            System.out.println("Download added");
            addNotice((SearchResultsPanel)event.getNewValue());
        }else if("title".equals(property)){//sets the frame's title when ever the SearchResultsPanel object fires the 'title' property change by its setFrameTitle method.
            setTitle((String)event.getNewValue());
        }else if("ytstate".equals(property)){
        }else if("sizeChanged".equals(property)){
            //layeredPane.setPreferredSize(new Dimension(700, (Integer)event.getNewValue()));
            //panel.setBounds(0, 0, 700, (Integer)event.getNewValue());
        }else if("add_new_tab".equals(property)){
            SearchTab tab = new SearchTab();
            tab.layeredPane.remove(tab.searchProgress);
            tab.pcs.addPropertyChangeListener(this);
            tab.panel.addPropertyChangeListener(this);
/*            String searchFieldText = (String)event.getNewValue();
            tab.searchField.setText(searchFieldText);
            tab.panel.searchQuery = searchFieldText;
            if(!searchFieldText.equals("")){
                tab.startSearch();
            }**/
            String tabName = "New search tab "+newTabNum++;
            tabs.addTab(tabName, null, tab, tabName);
            JPanel tabComponent = PanelFactory.createCloseButtonPanel(tabs, tabName);
            tabs.setTabComponentAt(tabs.getTabCount()-1, tabComponent);
//            tabs.addTab(searchFieldText, null, tab, searchFieldText);
//            tabs.setTabComponentAt(tabs.getTabCount()-1, PanelFactory.createCloseButtonPanel(tabs, searchFieldText));
        }else if("title_changed".equals(property)){
            SearchTab tab = (SearchTab)event.getNewValue();
            int i = tabs.indexOfComponent(tab);
            tabs.setTabComponentAt(i, PanelFactory.createCloseButtonPanel(tabs, tab.searchField.getText()));
        }else if("tabCount".equals(property)){
            newTabNum--;
        }
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        pcs.addPropertyChangeListener(listener);
    }
    
    @Override
    public void stateChanged(ChangeEvent event){
        if(event.getSource() == tabs){
            if(tabs.getTabCount() == 0){
                this.dispose();//dispose this Frame when the last tab is closed
            }
        }
    }
    
    @Override
    public void windowActivated(WindowEvent event){
        
    }
    
    @Override
    public void windowDeactivated(WindowEvent event){
        
    }
    
    @Override
    public void windowClosed(WindowEvent event){
        
    }
    
    @Override
    public void windowClosing(WindowEvent event){
        newTabNum = 2;
        dispose();
    }
    
    @Override
    public void windowIconified(WindowEvent event){
        
    }
    
    @Override
    public void windowDeiconified(WindowEvent event){
        
    }
    
    @Override
    public void windowOpened(WindowEvent event){
        
    }
}
