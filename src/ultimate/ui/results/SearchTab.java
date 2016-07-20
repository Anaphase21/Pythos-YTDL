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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JViewport;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Anaphase21
 */
public class SearchTab extends JScrollPane implements PropertyChangeListener, ChangeListener, ActionListener, KeyListener{
    
    SearchResultsPanel panel;
    JTextField searchField;
    public JViewport viewPort;
    private Point point;
    SearchTab.CustomJLayeredPane layeredPane;
    JPanel searchBar;
    SearchProgressPanel searchProgress;
    CustomJButton searchButton, newTabButton;
    SearchResultsPanel.Processor processor;
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public SearchTab(){
        super();
        setComponents();
    }
    
    public void setComponents(){
        setPreferredSize(new Dimension(700, 500));
        panel = new SearchResultsPanel(this);
        panel.addPropertyChangeListener(this);
        layeredPane = new SearchTab.CustomJLayeredPane();
        layeredPane.addPropertyChangeListener(layeredPane);
        layeredPane.setPreferredSize(new Dimension(700, 502));
        panel.addPropertyChangeListener(layeredPane);
        searchBar = new JPanel();
        java.awt.FlowLayout layout = new java.awt.FlowLayout(FlowLayout.LEADING, 2, 0);
        searchBar.setLayout(layout);
        searchField = new JTextField();
        searchField.addKeyListener(this);
        searchField.setPreferredSize(new Dimension(380, 28));
        searchBar.setPreferredSize(new Dimension(450, 29));
        searchBar.setOpaque(false);
        searchBar.setBounds(0, 0, searchBar.getPreferredSize().width, searchBar.getPreferredSize().height);
        searchField.setBackground(Color.CYAN);
        searchBar.add(searchField);
        searchBar.add(Box.createHorizontalGlue());
        searchButton = new CustomJButton("Search", "searchButton.png");
        searchButton.addActionListener(this);
        newTabButton = new CustomJButton("New search tab.", "newTabButton.png");
        newTabButton.addActionListener(this);
        searchBar.add(searchButton);
        searchBar.add(newTabButton);
        panel.setPreferredSize(new Dimension(700, 500));
        panel.setBounds(0, 0, panel.getPreferredSize().width, panel.getPreferredSize().height);
        panel.setOpaque(true);
        searchProgress = new SearchProgressPanel();
        searchProgress.setOpaque(true);
        searchProgress.setBounds(100, layeredPane.getPreferredSize().height/2-20, layeredPane.getPreferredSize().width-200, 60);
        searchProgress.pcss.addPropertyChangeListener(layeredPane);
        layeredPane.add(panel, new Integer(1));
        layeredPane.add(searchProgress, new Integer(2));
        layeredPane.add(searchBar, new Integer(2));
        setViewportView(layeredPane);
        getVerticalScrollBar().setUnitIncrement(14);
        viewPort = getViewport();
        viewPort.addChangeListener(this);
    }
    
    public void startSearch(){
        processor = panel.new Processor();
        processor.execute();
    }
    
class CustomJLayeredPane extends JLayeredPane implements PropertyChangeListener{
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        pcs.addPropertyChangeListener(listener);
    }
        
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String propertyName = event.getPropertyName();
        if("point".equals(propertyName)){
            Component component = null;
            int k = getComponentCount();
            Point point = (Point)event.getNewValue();
            for(int i = 0; i < k-1; i++){
                if((component = getComponent(i)) == searchBar){
                    if(component != null)
                    component.setBounds(0, point.y+2, component.getPreferredSize().width, component.getPreferredSize().height);
                }
                if(component == searchProgress){
                    component.setBounds(100, (viewport.getViewPosition().y+200), viewport.getWidth()-200, 60);
                }
            }
            }else if("remove_search_bar".equals(propertyName)){
                if((Boolean)event.getNewValue()){
                    Component component = null;
                    int k = getComponentCount();
                    for(int i = 0; i < k-1; ++i){
                        if((component = getComponent(i)) == searchBar){
                        remove(component);
                    }
                }
            }else{
                    add(searchBar, new Integer(2));
                }
            }else if("removeProgressPanel".equals(propertyName)){
                if((!searchProgress.error) || (searchProgress.cancelled)){
                    remove(searchProgress);
                    searchButton.setEnabled(true);
                    revalidate();
                    updateUI();
                    repaint();
                    searchProgress.cancelled = false;
                    searchProgress.error = true;
                }
            }
        }
    }
        
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if("ytstate".equals(property)){
        }else if("sizeChanged".equals(property)){
            layeredPane.setPreferredSize(new Dimension(700, (Integer)event.getNewValue()));
            panel.setBounds(0, 0, 700, (Integer)event.getNewValue());
            System.out.println(layeredPane.getPreferredSize().height);
        }else if("tab_title_change".equals(property)){
            pcs.firePropertyChange("title_changed", null, this);
        }else if("generateURLs".equals(property) || ("next".equals(property))){
            searchProgress = new SearchProgressPanel();
            if(property.equals("generateURLs")){
                searchProgress.message = (String)event.getNewValue();
            }
            searchProgress.setOpaque(true);
            searchProgress.setBounds(100, viewport.getViewPosition().y+200, layeredPane.getPreferredSize().width-200, 60);
            layeredPane.add(searchProgress, new Integer(2));
            searchProgress.pcss.addPropertyChangeListener(layeredPane);
        }
    }
        
    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() == searchButton){
            if((processor != null) && (!processor.isDone())){
                processor.cancel(true);
            }
            panel.searchQuery = searchField.getText();
            panel.executeTask(true);
            searchProgress = new SearchProgressPanel();
            searchProgress.setOpaque(true);
            searchProgress.setBounds(100, (viewport.getViewPosition().y+viewport.getHeight())/2-20, layeredPane.getPreferredSize().width-200, 60);
            layeredPane.add(searchProgress, new Integer(2));
            searchProgress.pcss.addPropertyChangeListener(layeredPane);
            pcs.firePropertyChange("title_changed", null, this);
        }else if(event.getSource() == newTabButton){
//            pcs.firePropertyChange("add_new_tab", null, searchField.getText());
            pcs.firePropertyChange("add_new_tab", false, true);
        }
    }

    @Override
    public void stateChanged(ChangeEvent event){
        if(event.getSource() instanceof JViewport){
            Point old = point;
            point = viewPort.getViewPosition();
            pcs.firePropertyChange("point", old, point);
        }
    }

class CustomJButton extends JButton{
        String imageName;
        Image image;
        CustomJButton(String text, String imageName){
            super();
            this.imageName = imageName;
            setBorderPainted(false);
            setUI(new BasicButtonUI());
            setPreferredSize(new Dimension(28, 28));
            setContentAreaFilled(false);
            setRolloverEnabled(true);
            setFocusable(false);
            setToolTipText(text);
            Border etchedBorder = BorderFactory.createEtchedBorder();
            setBorder(etchedBorder);
            image = loadImage(imageName);
            setDisabledIcon(new javax.swing.ImageIcon(loadImage(imageName)));
            addMouseListener(mouseListener);
        }
                
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D)g.create();
            if(getModel().isPressed()){
                g2d.translate(1, 1);
            }
            g2d.setColor(new Color(55, 55, 55));
            if(getModel().isRollover()){
                g2d.setColor(Color.MAGENTA);
            }
            if(image != null){
                g2d.drawImage(image, 0, 0, null);
            }
            g2d.dispose();
        }
        
        private final MouseListener mouseListener = new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent event){
                setBorderPainted(true);
            }
            
            @Override
            public void mouseExited(MouseEvent event){
                setBorderPainted(false);
            }
        };
    }
public class SearchProgressPanel extends JPanel implements ActionListener, Runnable, PropertyChangeListener{
    CustomJButton cancelButton;
    int arcColor = 0x010089;
    int red = 0;
    Color lColor = new Color(0x01ee08);
    Color rColor = new Color(0xff0201);
    int green = 255;
    int height;
    int width;
    int y_pos;
    int x_pos;
    boolean error = false;
    boolean cancelled = false;
    String message = "Searching Youtube. Please wait...";
    String possibleCauseMessage;
    String suggestionMessage;
    final PropertyChangeSupport pcss = new PropertyChangeSupport(this);
    public SearchProgressPanel(){
        setLayout(null);
        setPreferredSize(new Dimension(SearchTab.this.layeredPane.getPreferredSize().width-200, 60));
        height = getPreferredSize().height;
        width = getPreferredSize().width;
        y_pos = 9;
        x_pos = 26;
        panel.addPropertyChangeListener(this);
        Thread th = new Thread(this);
        th.start();
        cancelButton = new CustomJButton("Cancel", "cancel.png");
        cancelButton.setBounds(width-cancelButton.getPreferredSize().width-2, 2, cancelButton.getPreferredSize().width, cancelButton.getPreferredSize().height);
        cancelButton.addActionListener(this);
        add(cancelButton);
        searchButton.setEnabled(false);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.clearRect(0, 0, width, height);
        g2d.setColor(new Color(0x67019a));
        g2d.fillRoundRect(0, 0, width, height, 10, 10);
        if(!error){
            g2d.setColor(new Color(0xffffff));
            g2d.drawRoundRect(25, 8, width-50, 16, 5, 5);
            g2d.setColor(new Color(0x121212));
            g2d.fillRoundRect(26, 9, width-52, 15, 5, 5);
            g2d.setColor(new Color(arcColor));
            g2d.fillArc(x_pos, y_pos, 12, 12, 0, 360);
            g2d.setColor(rColor);
            g2d.drawString(message, width/2-getStringWidth(message, g2d)/2, height-20);
        }else{
            g2d.setColor(new Color(0xffffff));
            g2d.drawRoundRect((width/2)-getStringWidth(message, g2d)/2-getStringWidth("ERROR: ", g2d)-3, 1, getStringWidth("ERROR: ", g2d)+getStringWidth(message, g2d)+3, g2d.getFontMetrics().getHeight()+1, 5, 5);
            g2d.setColor(lColor);
            g2d.drawString("ERROR: ", (width/2)-getStringWidth(message, g2d)/2-getStringWidth("ERROR: ", g2d), 14);
            g2d.setColor(rColor);
            g2d.drawString(message, (width/2)-getStringWidth(message, g2d)/2, 14);
            g2d.setColor(lColor);
            g2d.drawString("POSSIBLE CAUSE: ", 5, g2d.getFontMetrics().getHeight()+14);
            g2d.setColor(rColor);
            g2d.drawString(possibleCauseMessage, getStringWidth("POSSIBLE CAUSE: ", g2d)+5, g2d.getFontMetrics().getHeight()+14);
            g2d.setColor(lColor);
            g2d.drawString("SUGGESTION: ", 5, g2d.getFontMetrics().getHeight()*2+14);
            g2d.setColor(rColor);
            g2d.drawString(suggestionMessage, getStringWidth("SUGGESTION: ", g2d)+5, g2d.getFontMetrics().getHeight()*2+14);
        }
        g2d.dispose();
    }
    
    @Override
    public void actionPerformed(ActionEvent event){
        if(event.getSource() == cancelButton){
            if((processor != null) && (!processor.isDone())){
                processor.cancel(true);
            }
            if((panel.runner != null) && (!panel.runner.isDone())){
                panel.runner.cancel(true);
            }
            cancelled = true;
            pcss.firePropertyChange("removeProgressPanel", null, this);
        }
    }
    
    @Override
    public void run(){
        int k = width-50;
        boolean reverse = false;
        while(true){
            if((cancelled) || (error)){
                cancelled = false;
                try{
                    throw new InterruptedException();
                }catch(InterruptedException ie){
                    return;
                }
            }
            if(red >= 255){
                red = 0;
            }
            red = ((arcColor>>>16) & 0xff);
            red += 3;
            arcColor = (red<<16)|89;
            try{
                if(reverse){
                    x_pos -= 5;
                    Thread.sleep(60);
                    if(!(reverse = (26 < x_pos))){
                    x_pos = 26;
                    continue;
                }
                repaint();
                continue;
            }
            x_pos += 5;
            Thread.sleep(60);
            if(reverse = (k < x_pos)){
                x_pos = k+12;
            }
            repaint();
            }catch(InterruptedException ie){}
        }
    }
    
    int getStringWidth(String str, Graphics2D g){
        if(str == null){
            return 0;
        }
        return g.getFontMetrics().stringWidth(str);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if(property.equals("ytstate")){//yt object of SearchResultsPanel is now guaranteed to be non-null, hence we can safely call yt.addPropertyChangeListener
            panel.yt.addPropertyChangeListener(this);
        }else if(property.equals("error")){
            error = true;
            message = (String)event.getNewValue();
            if(message.startsWith("java.net.UnknownHostException")){
                possibleCauseMessage = "No internet connectivity.";
                suggestionMessage = "Please check your internet connection and try again.";
            }else if(message.startsWith("java.net.SocketTimeoutException")){
                possibleCauseMessage = "Connection was timed out while trying to connect or read content.";
                suggestionMessage = "Try again.";
            }else if(message.startsWith("No links")){
                possibleCauseMessage = message;
                suggestionMessage = "Please try again.";
            }else{
                possibleCauseMessage = "Unknown";
                suggestionMessage = "Please try again or restart the application.";
            }
        }
    }
}
    @Override
    public void keyPressed(KeyEvent event){
        if(event.getKeyCode() == KeyEvent.VK_ENTER){
            if((processor != null) && (!processor.isDone())){
                processor.cancel(true);
            }
            panel.searchQuery = searchField.getText();
            panel.executeTask(true);
            searchProgress = new SearchProgressPanel();
            searchProgress.setOpaque(true);
            searchProgress.setBounds(100, (viewport.getViewPosition().y+viewport.getHeight())/2-20, layeredPane.getPreferredSize().width-200, 60);
            layeredPane.add(searchProgress, new Integer(2));
            searchProgress.pcss.addPropertyChangeListener(layeredPane);
            pcs.firePropertyChange("title_changed", null, this);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent event){
        
    }
    
    @Override
    public void keyTyped(KeyEvent event){
        
    }
    
    public Image loadImage(String imageName){
        Image icon = null;
        try{
           icon = ImageIO.read(new java.io.DataInputStream(getClass().getResourceAsStream("/res/thumbs/"+imageName)));
        }catch(java.io.IOException ioe){
                
        }
            return icon;
    }
}
