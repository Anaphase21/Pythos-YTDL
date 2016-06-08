/*
 * Copyright (C) 2015 Anaphase21
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

import javax.swing.BoxLayout;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.*;
/**
 *
 * @author Anaphase21
 */
public class VideoPropertiesPanel extends JPanel implements MouseListener{
    private String videoProperties;
    private ImageIcon thumbnail;
    JLabel icon;
    JLabel properties;
    private static final String IDLE_START = "<html><font color=\"#000000\">";
    private static final String IDLE_END = "</font></html>";
    private static final String ENTERED_START = "<html><a href=\" \">";
    private static final String ENTERED_END = "</a></html>";
    private static final String CLICKED_START = "<html><a href=\" \" color=\"#ed5544\">";
    private static final String CLICKED_END = "</a></html>";
    private boolean clicked = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public VideoPropertiesPanel(String videoProperties, ImageIcon thumbnail){
        this.videoProperties = videoProperties;
        this.thumbnail = thumbnail;
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        Border line = BorderFactory.createLineBorder(new Color(200, 200, 200));
        setBorder(line);
    }
    
    public void setComponents(){
        properties = new JLabel(VideoPropertiesPanel.IDLE_START+videoProperties.substring(videoProperties.indexOf("]")+1)+VideoPropertiesPanel.IDLE_END);
        properties.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //properties.setMaximumSize(new Dimension(20, 40));
        icon = new JLabel(thumbnail);
        add(icon);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(properties);
        properties.addMouseListener(this);
    }
    
    private String getId(){
        String id = null;
        if(videoProperties != null){
            int start = videoProperties.indexOf("[");
            int end = videoProperties.indexOf("]", start);
            id = videoProperties.substring(start+1, end);
        }
        return id;
    }
    
    private void setId(String id){
        pcs.firePropertyChange("videoClicked", " ", id);
    }
    
    @Override
    public void mouseClicked(MouseEvent event){
        clicked = true;
        properties.setText(VideoPropertiesPanel.CLICKED_START+videoProperties.substring(videoProperties.indexOf("]")+1)+VideoPropertiesPanel.CLICKED_END);
        setId(getId());
    }
    
    @Override
    public void mouseEntered(MouseEvent event){
        if(!clicked){
        properties.setText(VideoPropertiesPanel.ENTERED_START+videoProperties.substring(videoProperties.indexOf("]")+1)+VideoPropertiesPanel.ENTERED_END);
        }
    }
    
    @Override
    public void mouseExited(MouseEvent event){
        if(!clicked){
        properties.setText(VideoPropertiesPanel.IDLE_START+videoProperties.substring(videoProperties.indexOf("]")+1)+VideoPropertiesPanel.IDLE_END);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent event){
    }
    
    @Override
    public void mouseReleased(MouseEvent event){
        
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        pcs.addPropertyChangeListener(listener);
    }
}
