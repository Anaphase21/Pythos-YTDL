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
package ultimate.ui.downloadmanager;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import properties.AppProperties;

/**
 *
 * @author Anaphase21
 */
public class DownloadTaskPane extends JPanel implements PropertyChangeListener, ActionListener{
    JProgressBar downloadProgressBar;
    long downloaded;
    long fileSize;
    int STATE;
    int progress = 0;
    JButton dwlButtonState;
    final int PAUSED = 0;
    final int ACTIVE = 1;
    final int COMPLETED = 2;
    protected boolean hasBeenExecuted = false;//
    JLabel downloadedLabel;
    JLabel sizeLabel;
    JLabel dateLabel;
    Downloader task;
    String sizeString = "<HTML><p color='#a54501'>File Size: ";
    String downloadedString = "<HTML><p color='#a54501'>Downloaded: ";
    TitledBorder titledBorder;
    Border btitle;
    long dateTime = 0;
    int tries = 0;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    protected DownloadTaskPane(Downloader task, long dateTime){
        this.task = task;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        titledBorder = BorderFactory.createTitledBorder(this.task.title);
        btitle = BorderFactory.createTitledBorder(titledBorder, "<HTML><p color='#0403e9'>"+this.task.title+"</p></HTML>", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(Font.SERIF, Font.ITALIC, 12));
        setBorder(btitle);
        setOpaque(true);
        setBackground(new Color(180, 180, 180, 200));
        downloaded = this.task.downloaded;
        fileSize = this.task.fileSize;
        this.STATE = this.task.STATE;
        downloadProgressBar = new JProgressBar(0, 100);
        downloadProgressBar.setValue(0);
        downloadProgressBar.setStringPainted(true);
        dwlButtonState = new JButton();
        dwlButtonState.addActionListener(this);
        sizeLabel = new JLabel(sizeString+AppProperties.bytesToMegaGiga(fileSize)+"</p></HTML>");
        sizeLabel.setFont(new Font("Serif", Font.PLAIN, 11));
        downloadedLabel = new JLabel(downloadedString+AppProperties.bytesToMegaGiga(downloaded)+"</p></HTML>");
        downloadedLabel.setFont(new Font("Serif", Font.PLAIN, 11));
        java.util.Date date = new java.util.Date(dateTime);
        java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("EEE, dd MMMM yyyy (HH:mm:ss)");
        dateLabel = new JLabel("<HTML><p color='#a54501'>Date: "+"<var color='#019d00'>"+dateFormat.format(date)+"</var>"+"</p></HTML>");
        dateLabel.setFont(new Font("Serif", Font.PLAIN, 11));
        setButtonState();
        add(createRemovePanel());
        add(sizeLabel);
        add(downloadedLabel);
        add(dateLabel);
        add(dwlButtonState);    
        this.task.addPropertyChangeListener(this);
    }
    
    void updateProgressBar(int progress){
        downloadProgressBar.setValue(progress);
        repaint();
    }
    
    void updateDownloadProgress(long downloading){
        downloadedLabel.setText(downloadedString+AppProperties.bytesToMegaGiga(downloaded)+"</p></HTML>");
  //        downloadedLabel.setText(downloadedString+String.valueOf(downloaded)+"</p></HTML>");
        repaint();
    }
    
    void updateFileDownloadSize(long size){
        sizeLabel.setText(sizeString+AppProperties.bytesToMegaGiga(fileSize)+"</p></HTML>");
                //downloadedLabel.setText(sizeString+String.valueOf(fileSize)+"</p></HTML>");
        repaint();
    }
    
    JPanel createRemovePanel(){
        RemovePaneButton removeButton = new RemovePaneButton();
        JPanel button = new JPanel();
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
        button.setMaximumSize(new Dimension(26, 26));
        button.setLayout(new BorderLayout());
        button.add(removeButton, BorderLayout.PAGE_START);
        pane.add(downloadProgressBar);
        pane.add(Box.createRigidArea(new Dimension(5, 0)));
        pane.add(button);
        return pane;
    }
    
    void setButtonState(){
        switch(STATE){
        case PAUSED:
            dwlButtonState.setText("Resume");
            repaint();
            break;
        case ACTIVE:
            dwlButtonState.setText("Pause");
            repaint();
            break;
        case COMPLETED:
            remove(dwlButtonState);
            JLabel label = new JLabel("<HTML><p color='#fe11e3'>Download Complete</p></HTML>");
            label.setFont(new Font("Serif", Font.PLAIN, 11));
            add(label);
            repaint();
            break;
        }
        updateUI();
    }
    
    void setRemove(){
        pcs.firePropertyChange("remove", false, true);
    }
    
    private void showErrorDialog(String msg){
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if("progress".equals(property)){
            progress = (Integer)event.getNewValue();
            updateProgressBar(progress);
        }else if("D_STATE".equals(property)){
            STATE = (Integer)event.getNewValue();
            setButtonState();
        }else if("fileSize".equals(property)){
            this.fileSize = (Long)event.getNewValue();
            updateFileDownloadSize(this.fileSize);
        }else if("downloaded".equals(property)){
            this.downloaded = ((Long)event.getNewValue());
            updateDownloadProgress(this.downloaded);
        }else if("Exception".equals(property)){
            String s = (String)event.getNewValue();
            if((fileSize == 0) && (downloaded == 0)){
                setRemove();
            }
            showErrorDialog(s);
        }else if("title".equals(property)){
            titledBorder.setTitle((String)event.getNewValue());
            btitle = btitle = BorderFactory.createTitledBorder(titledBorder, "<HTML><p color='#0403e9'>"+this.task.title+"</p></HTML>", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(Font.SERIF, Font.ITALIC, 12));
            setBorder(btitle);
            updateUI();
            repaint();
            System.out.println(titledBorder.getTitle());
        }else if("retry".equals(property)){
            if(!task.isCancelled()){
                task.cancel(true);
            }
            if(tries != 4){
                retry();
                tries++;
            }else{
                tries = 0;
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent event){
       if(STATE == PAUSED){
           if(!hasBeenExecuted){
               STATE = ACTIVE;
               task.STATE = task.active;
               task.isPausedBefore = true;
               if(task.closed){
                   task = new Downloader(task.url, task.title, task.path, task.id, task.resolution, task.time, fileSize, downloaded);
                   task.addPropertyChangeListener(this);
                   task.isPausedBefore = true;
                   task.execute();
               }
           }else{
               task.execute();
               hasBeenExecuted = false;
               task.isPausedBefore = true;
           }
           setButtonState();
           //task.doNotify();
       }else if(STATE == ACTIVE){
           task.STATE = PAUSED;
           STATE = PAUSED;
           setButtonState();
       }
    }
    
    class RemovePaneButton extends JButton implements ActionListener{
        
        RemovePaneButton(){
            setBorderPainted(false);
            setUI(new BasicButtonUI());
            setPreferredSize(new Dimension(20, 20));
            setContentAreaFilled(false);
            setRolloverEnabled(true);
            setFocusable(false);
            setToolTipText("Delete download task.");
            Border etchedBorder = BorderFactory.createEtchedBorder();
            setBorder(etchedBorder);
            addMouseListener(listener);
            addActionListener(this);
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
            g2d.drawLine(6, 6, getWidth()-6, getHeight()-6);
            g2d.drawLine(getWidth()-6, 6, 6, getHeight()-6);
            g2d.dispose();
        }
        
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
        
        @Override
        public void actionPerformed(ActionEvent event){
            JComponent component = (JComponent)event.getSource();
            if(component instanceof RemovePaneButton){
                DownloadRecords.deleteRecord(DownloadTaskPane.this.task.title, DownloadTaskPane.this.task.path);
                DownloadTaskPane.this.setRemove();
            }
        }
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        pcs.addPropertyChangeListener(listener);
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
    }
    
    private void retry(){
        try{
            tube.api.YouTubeInfoFile yt = new tube.api.YouTubeInfoFile();
            String source = yt.getPageSource("https://youtube.com/watch?v="+task.id);
            yt.parseInfoFile(source);
            String s = yt.links.get(task.resolution);
            task.url = yt.links.get(s);
            System.out.println(s);
            task = new Downloader(s, task.title, task.path, task.id, task.resolution, task.time, task.fileSize, task.downloaded);
            task.isPausedBefore = true;
            task.addPropertyChangeListener(this);
            task.execute();
        }catch(java.io.IOException ioe){}
    }
}
