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
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.concurrent.ExecutionException;
import javax.swing.border.Border;
import tube.api.SimpleYouTubeParser;
import tube.api.YouTubeInfoFile;
import ultimate.ui.PanelFactory;
import ultimate.ui.CustomRadioButton;
import java.io.IOException;
/**
 *
 * @author Anaphase21
 */
public class SearchResultsPanel extends JPanel implements PropertyChangeListener, ActionListener{
    boolean flag;
    public class Processor extends SwingWorker<Void, Void>{
        @Override
        public Void doInBackground(){
            yt = new YouTubeInfoFile();
            yt.addPropertyChangeListener(SearchResultsPanel.this);
            setYtState(yt);
            String searchQuery1 = replace(searchQuery, " ", "+");
            flag = parser.process("https://youtube.com/results?lclk=video&search_query="+searchQuery1+"&filters=video&page="+(isNewSearch?String.valueOf(1):String.valueOf(parser.pageNumber)), yt);
            return null;
        }
        @Override
        public void done(){
            try{
                get();
            }catch(ExecutionException ee){
            }catch(InterruptedException ie){
            }catch(java.util.concurrent.CancellationException ce){
                System.out.println("Cancelled");
                return;
            }
            setDone(true);//Triggers a property change event to be fired, indicating that the processing is complete
            if(!flag){
                return;
            }
            if(parser.video.size() > 0){
                setComponents(parser);
            }
        }
    }
    
    public boolean done = false;
    public int cachedPageCount = 0;
    private int sizeChange;
    public boolean newTask = false;
    private boolean hasNextPage = false;
    private boolean hasPreviousPage = false;
    boolean isNewSearch = false;
    private volatile String id;
    protected String searchQuery;
    public String url;
    public String urlKey;
    JButton nextButton;
    JButton previousButton;
    JButton back;
    JButton download;
    JPanel backPanel;
    JPanel navPanel;
    JPanel backDownloadPanel;
    JPanel searchResults;
    ArrayList<JPanel> results;
    JTextArea tb;
    SimpleYouTubeParser parser;
    public volatile YouTubeInfoFile yt;
    CustomRadioButton[] resolutions;
    java.util.Timer timer;
    Point backFromDwlPanel;
    HashMap<JPanel, Point> cachedPages;
    SearchTab searchTab;
    Point viewportPosition;
    InfoFileRunner runner;
    Processor processor;
    CustomRadioButton butt;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public SearchResultsPanel(SearchTab searchTab){
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        results = new ArrayList<JPanel>(20);
        parser = new SimpleYouTubeParser();
        addPropertyChangeListener(this);
        tb = new JTextArea();
        tb.setLineWrap(true);
        backFromDwlPanel = new Point();
        this.searchTab = searchTab;
        cachedPages = new HashMap<JPanel, Point>(100);
        viewportPosition = new Point();
    }
        
    public void setComponents(SimpleYouTubeParser syt){
        searchResults = new JPanel();
        if(searchResults.getComponentCount() == 0){
            searchResults.setName(String.valueOf(parser.pageNumber));
//            setFrameTitle("Search results: Page "+parser.pageNumber);
        }
        searchResults.setLayout(new BoxLayout(searchResults, BoxLayout.PAGE_AXIS));
        searchResults.setPreferredSize(new Dimension(700, 500));
        hasNextPage = syt.hasNextPage();
        hasPreviousPage = syt.hasPreviousPage();
        Set keys = syt.video.keySet();
        Iterator elements = keys.iterator();
        VideoPropertiesPanel pane = null;
        sizeChange = 0;
        while(elements.hasNext()){
            String s = (String)elements.next();
            ImageIcon icon = syt.video.get(s);
            pane = new VideoPropertiesPanel(s, icon);
            pane.addPropertyChangeListener(this);
            pane.setComponents();
            searchResults.add(pane);
            searchTab.viewPort.setViewPosition(new Point(0, 0));
            sizeChange += pane.getPreferredSize().height;
            searchResults.add(Box.createRigidArea(new Dimension(0, 10)));
            sizeChange += 10;
        }
        createNavPanel();
        searchResults.add(navPanel);
        sizeChange += navPanel.getPreferredSize().height;
        results.add(searchResults);
        removeAll();
        revalidate();
        updateUI();
        add(searchResults);
        pcs.firePropertyChange("sizeChanged", 0, sizeChange);
    }
    
    public void createNavPanel(){
        navPanel = new JPanel();
        nextButton = new JButton("Next  "+'\u00BB');
        nextButton.addActionListener(this);
        previousButton = new JButton('\u00AB'+"  Previous");
        previousButton.addActionListener(this);
        nextButton.setEnabled(hasNextPage);
        previousButton.setEnabled(hasPreviousPage);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.LINE_AXIS));
        Border line = BorderFactory.createLineBorder(new Color(200, 200, 200));
        navPanel.setBorder(line);
        navPanel.add(previousButton);
        navPanel.add(Box.createHorizontalGlue());
        navPanel.add(nextButton);
    }
    
    private JPanel createBackDownloadPanel(){
        backPanel = new JPanel();
        backPanel.setLayout(new BoxLayout(backPanel, BoxLayout.LINE_AXIS));
        back = new JButton("Back");
        back.addActionListener(this);
        download = new JButton("Download");
        download.addActionListener(this);
        download.setOpaque(true);
        backPanel.add(download);
        download.setEnabled(false);
        backPanel.add(Box.createHorizontalGlue());
        backPanel.add(back);
        return backPanel;
    }
    
    public void setDone(boolean flag){
        boolean old = done;
        pcs.firePropertyChange("done", old, flag);
    }
        
    public boolean getDone(){
        return done;
    }
    
    void addNotice(){
        System.out.println("This file notified");
        pcs.firePropertyChange("addNotice", null, SearchResultsPanel.this);
    }
    
    private void setId(String id){
        this.id = id;
        pcs.firePropertyChange("generateURLs", " ", "Generating URLs...");
    }
    
    public String getId(){
        return id;
    }
    
    //Notifies the listener(SearchResultsWindow object) to change the frame's title
    void setFrameTitle(String title){
        pcs.firePropertyChange("title", " ", title);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.pcs.addPropertyChangeListener(listener);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if("done".equals(property)){
            if(results.size() > 0){
                cachedPages.put(searchResults, searchTab.viewPort.getViewPosition());
            }
            if(isNewSearch){
                isNewSearch = false;
                parser.pageNumber = 1;
                results.removeAll(results);
            }
            removeAll();
            revalidate();
            updateUI();
            repaint();
            pcs.firePropertyChange("removeProgressPanel", " ", "null");
            pcs.firePropertyChange("tab_title_change", "", searchQuery);
        }else if("videoClicked".equals(property)){
            id = (String)event.getNewValue();
            viewportPosition = searchTab.viewPort.getViewPosition();
            if((runner != null) && (!runner.isDone())){
                runner.cancel(true);
                pcs.firePropertyChange("removeProgressPanel", " ", "null");
            }
            setId(id);
            updateUI();
        }else if("generateURLs".equals(property)){
            processTask();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent event){
        String label = "";
        JComponent jcomponent = (JComponent)event.getSource();
        if(jcomponent instanceof JButton){
            label = ((JButton)jcomponent).getText();
        }
        if(label.equals("Next  "+'\u00BB')){
            if(parser.pageNumber < results.size()){
                removeAll();
                revalidate();
                updateUI();
                JPanel currentPanel = results.get(parser.pageNumber);
                cachedPages.put(searchResults, searchTab.viewPort.getViewPosition());
//                setFrameTitle("Search results: Page "+currentPanel.getName());
                add(currentPanel);
                searchTab.viewPort.setViewPosition(cachedPages.get(currentPanel));
                updateUI();
                repaint();
                parser.pageNumber++;
                return;
            }
            parser.pageNumber++;
            pcs.firePropertyChange("next", " ", "null");
            executeTask(false);
        }else if(label.equals('\u00AB'+"  Previous")){
            removeAll();
            revalidate();
            updateUI();
            JPanel currentPanel = results.get(parser.pageNumber-2);
  //          setFrameTitle("Search results: Page "+currentPanel.getName());
            add(currentPanel);
            cachedPages.put(searchResults, searchTab.viewPort.getViewPosition());
            searchTab.viewPort.setViewPosition(cachedPages.get(currentPanel));
            parser.pageNumber--;
        }else if(event.getSource() == back){
            removeAll();
            revalidate();
            updateUI();
            JPanel currentPanel = results.get(parser.pageNumber-1);
            setFrameTitle("Search results: Page "+currentPanel.getName());
            pcs.firePropertyChange("sizeChanged", 0, sizeChange);
            pcs.firePropertyChange("remove_search_bar", true, false);
            searchTab.viewPort.setViewPosition(cachedPages.get(currentPanel));
            add(currentPanel);
            searchTab.viewPort.setViewPosition(cachedPages.get(currentPanel));
        }else if(jcomponent instanceof CustomRadioButton){
            urlKey = (butt = (CustomRadioButton)jcomponent).getText();
            url = yt.links.get(urlKey);
            download.setEnabled(true);
            tb.setText(yt.links.get(urlKey));
            revalidate();
            repaint();
        }else if(event.getSource() == download){//if download button is clicked.
            butt.getParent().revalidate();
            butt.getParent().repaint();
            revalidate();
            addNotice();//Notify the listener(SearchResultsWindow->SearchWindowUI->MainWindow) to initiate a download.
        }
    }
    
    protected void executeTask(boolean isNewSearch){
        if(isNewSearch){
            this.isNewSearch = isNewSearch;
        }
        if((runner != null) && (!runner.isDone())){
            runner.cancel(true);
        }
        if((processor != null) && (!processor.isDone())){
            processor.cancel(true);
        }
        if((searchTab.processor != null) && (!searchTab.processor.isDone())){
            searchTab.processor.cancel(true);
        }
        processor = new Processor();
        processor.execute();
    }
    
private String replace(String source, String from, String to){//a method to replace strings or characters in a string
StringBuilder sb = new StringBuilder();
int searchPos = source.indexOf(from);
int startPos = 0;
int searchStrLength = from.length();
while(searchPos != -1) {
sb.append(source.substring(startPos, searchPos)).append(to);
startPos = searchPos + searchStrLength;
searchPos = source.indexOf(from, startPos);
}
sb.append(source.substring(startPos, source.length()));
return sb.toString();
}

class InfoFileRunner extends SwingWorker<CustomRadioButton[], Void>{
    InfoFileRunner(){
        
    }
    
    @Override
    public CustomRadioButton[] doInBackground(){
        yt = new YouTubeInfoFile();
        CustomRadioButton[] resols = null;
        try{
            String g = yt.getPageSource("https://youtube.com/watch?v="+getId());
            if(g == null){
                return null;
            }
            boolean flag = yt.parseInfoFile(g);
            if((yt.resolutionsArray.length == 0) || (flag)){
                pcs.firePropertyChange("error", " ", "No links(URLs) found for this video.");
                JOptionPane.showMessageDialog(null, "No links found for this video.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }else{
                int length = yt.resolutionsArray.length;
                resols = new CustomRadioButton[length];
                for(int i = 0; i < length; ++i){
                    resols[i] = new CustomRadioButton(yt.resolutionsArray[i]);
                }
            }
        }catch(IOException ioe){
            JOptionPane.showMessageDialog(null, ioe.toString()+":\nOops, there was a read/write error, please try again", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return resols;
    }
    
    @Override
    public void done(){
        try{
            resolutions = get();
        }catch(InterruptedException ie){
            
        }catch(ExecutionException ee){
            
        }catch(java.util.concurrent.CancellationException ce){
            return;
        }
        if(resolutions == null){
            return;
        }
        cachedPages.put(searchResults, searchTab.viewPort.getViewPosition());
        pcs.firePropertyChange("sizeChanged", 0, 500);
        pcs.firePropertyChange("remove_search_bar", false, true);
        pcs.firePropertyChange("removeProgressPanel", " ", "null");
        pcs.firePropertyChange("getViewportLocationForBack", false, true);
        setResolutionsPanel();
        add(createBackDownloadPanel());
        tb.setText("");
        add(tb);
        searchTab.viewPort.setViewPosition(new Point(0, 0));
    }
}

private void setResolutionsPanel(){
    if(resolutions == null){
        JOptionPane.showMessageDialog(null, "Couldn't complete task, please try again.\nYOU MAY ALSO CHECK YOUR INTERNET CONNECTIONS.\nYou can restart the application and try again", "Notice", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    removeAll();
    revalidate();
    repaint();
    add(PanelFactory.createResolsPanel(resolutions, this));
    setFrameTitle("Download options");
 }

    private void processTask(){
        if((runner != null) && (!runner.isDone())){
            runner.cancel(true);
        }
        runner = new InfoFileRunner();
        runner.execute();
    }

    public void setYtState(YouTubeInfoFile yt){
        pcs.firePropertyChange("ytstate", null, yt);
    }
}
