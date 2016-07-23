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

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.ArrayList;
import java.beans.*;
import properties.AppProperties;

/**
 *
 * @author Anaphase21
 */
public final class DownloadWindow extends JPanel implements PropertyChangeListener{
    JButton pauseResumeButton;
    boolean fileExistence = false;
    public DownloadWindow(){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        DownloadRecords.openDownloadRecords();
        if(!DownloadRecords.downloadRecords.isEmpty()){
            loadDownloadPanels();
        }
    }
    
    public void addDownloadPanel(String title, String url, String path, String fileExtension, String id, String resolution){
        long dateTime = System.currentTimeMillis();
        Downloader downloader = new Downloader(url, title+fileExtension, path, id, resolution, dateTime, 0, 0);
        downloader.fileAlreadyExists = getFileExistence();
        downloader.execute();
        DownloadTaskPane taskPane = new DownloadTaskPane(downloader, dateTime);
        taskPane.addPropertyChangeListener(this);
        add(taskPane, 0);
    }
    
    public boolean getFileExistence(){
        return fileExistence;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        if(event.getPropertyName().equals("fileExistence")){
            fileExistence = (Boolean)event.getNewValue();
            return;
        }
        JComponent component = (JComponent)event.getSource();
        if(component instanceof DownloadTaskPane){
            remove(component);
            revalidate();
            repaint();
        }
    }
    
    public void loadDownloadPanels(){
        if(DownloadRecords.downloadRecords.isEmpty()){
            return;
        }
        Downloader downloader = null;
        DownloadTaskPane taskPane = null;
        String titleKey = null;
        String title = null;
        String urlthis = null;
        String path = null;
        String id = null;
        String resolution = null;
        long time = 0;
        long downloaded = 0;
        long fileSize = 0;
        ArrayList<String> list = null;
        java.util.Collection<String> set = (java.util.Collection<String>)DownloadRecords.downloadRecords.keySet();
        Iterator iterator = set.iterator();
        long[] times  = new long[set.size()];
        for(int i = 0; iterator.hasNext(); ++i){
            titleKey = (String)iterator.next();
            times[i] = Long.parseLong(titleKey);
        }
        times = AppProperties.sort(times);
        int l = times.length;
        for(int k = 0; k < l; ++k){
            list = DownloadRecords.downloadRecords.get(String.valueOf(times[k]));
            title = list.get(0);
            urlthis = list.get(1);
            path = list.get(2);
            time = Long.parseLong(list.get(3));
            fileSize = Long.parseLong(list.get(4));
            downloaded = Long.parseLong(list.get(5));
            id = list.get(6);
            resolution = list.get(7);
            downloader = new Downloader(urlthis, title, path, id, resolution, time, fileSize, downloaded);
            taskPane = new DownloadTaskPane(downloader, time);
            if(downloaded > 0){
                taskPane.downloadProgressBar.setValue((int)(downloaded*100/fileSize));
            }else{
                taskPane.downloadProgressBar.setValue(0);
            }
            if(((fileSize > 0)&&(downloaded > 0))&&(downloaded == fileSize)){
            taskPane.task.STATE = taskPane.task.complete;
            taskPane.STATE = taskPane.task.STATE;
            taskPane.setButtonState();
        }
            taskPane.hasBeenExecuted = true;
            taskPane.addPropertyChangeListener(this);
            add(taskPane, 0);
        }
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        revalidate();
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.drawImage(AppProperties.backgroundImg, 0, 0, null);
        g2d.dispose();
        repaint();
    }
}