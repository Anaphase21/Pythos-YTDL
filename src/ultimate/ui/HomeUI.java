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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import java.beans.*;
import java.io.*;
import javax.swing.border.*;
import javax.swing.text.DefaultEditorKit;
import tube.api.YouTubeInfoFile;
import properties.AppProperties;
import ultimate.ui.downloadmanager.DownloadRecords;

/**
 *
 * @author Anaphase21
 */
public class HomeUI extends JPanel implements ActionListener, PropertyChangeListener, KeyListener, PopupMenuListener{
    private class GetInfoFileProgress extends SwingWorker<CustomRadioButton[], Void>{
        @Override
       public CustomRadioButton[] doInBackground(){
            YouTubeInfoFile yt = new YouTubeInfoFile();
            yt.addPropertyChangeListener(HomeUI.this);
            CustomRadioButton[] resols = null;
            try{
                HomeUI.this.g = yt.getPageSource("https://youtube.com/watch?v="+getId());
                if(g.endsWith("error")){
                    JOptionPane.showMessageDialog(null, "There was an error encountered."+'\n'+"Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                if(g == null){
                    JOptionPane.showMessageDialog(null, "No internet connection.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                boolean flag = yt.parseInfoFile(HomeUI.this.g);
                HomeUI.this.yt = yt;
                if((yt.resolutionsArray.length == 0) || (flag)){
                    JOptionPane.showMessageDialog(mainWindow, "No links found for this video.\n Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    HomeUI.this.updateUI();
                }
                else{
                    int length = yt.resolutionsArray.length;
                    resols = new CustomRadioButton[length];
                    for(int i = 0; i < length; i++){
                        resols[i] = new CustomRadioButton(yt.resolutionsArray[i]);
                    }
                }
            }catch(IOException ioe){
            JOptionPane.showMessageDialog(frame, ioe.toString()+"\nOops, there was a read/write error, please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            HomeUI.this.updateUI();
            } 
            return resols;
        }
       
       @Override
       public void done(){
               updateUI();
               try{
                   HomeUI.this.resolutions = get();
               }catch(InterruptedException ie){
                   JOptionPane.showMessageDialog(mainWindow, ie.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                   HomeUI.this.updateUI();
                   return;
               }catch(ExecutionException ee){
                   //ee.printStackTrace();
                    canCancel = !canCancel;
                    cancelEnter = "Enter";
                    fetchFileButton.setText(cancelEnter);
                    fetchFileButton.setToolTipText("Get links for this video.");
                    remove(progress);
                    revalidate();
                   JOptionPane.showMessageDialog(mainWindow, ee.toString()+"\n"+error, "Error", JOptionPane.ERROR_MESSAGE);
                   error = "";
                   HomeUI.this.updateUI();
                   return;
               }catch(CancellationException ce){
                   return;
               }catch(NullPointerException npe){
                   return;
               }
               HomeUI.this.remove(HomeUI.progress);
               if(resolutions == null){
                    JOptionPane.showMessageDialog(mainWindow, "Couldn't complete task, please try again.\nYOU MAY ALSO CHECK YOUR INETERNET CONNECTIONS.\nYou can also restart the application and try again", "Notice", JOptionPane.INFORMATION_MESSAGE);
                    canCancel = !canCancel;
                    cancelEnter = "Enter";
                    fetchFileButton.setText(cancelEnter);
                    fetchFileButton.setToolTipText("Get links for this video");
                    remove(progress);
                    return;
               }
               resolPanel =  PanelFactory.createResolsPanel(HomeUI.this.resolutions, HomeUI.this);
               HomeUI.this.add(resolPanel);
               HomeUI.this.add(Box.createRigidArea(new Dimension(0, 40)));
               ta = new JTextArea();
               downloadResetPanel = PanelFactory.createDownloadAndResetButtonsPanel(downloadButton, resetButton);
               HomeUI.this.add(downloadResetPanel);
               ta.setLineWrap(true);
               add(ta);
               fetchFileButton.setEnabled(false);
               repaint();
               HomeUI.this.updateUI();
           }
    }
    
    JButton fetchFileButton;
    JButton downloadButton;
    JButton resetButton;
    JPanel resolPanel;
    JPanel downloadResetPanel;
    JPanel tfsPanel;
    volatile YouTubeInfoFile yt = null;
    CustomRadioButton[] resolutions;
    MainWindow frame;
    String g = " ";
    Border margin;
    SearchWindowUI searchWindow;
    JTabbedPane tabs;
    JMenu menu;
    JMenuBar menuBar;
    JPopupMenu popupMenu;
    JScrollPane scrollPane;
    JFileChooser fileChooser;
    JFrame mainWindow;
    Image im;
    static JPanel progress;
    JTextArea ta;
    JButton directoryButton;
    CJTextField textField;
    JTextField currentDirectoryField;
    public String currentDirectory = " ";
    private String id;
    String urlKey;
    private boolean canCancel = false;
    private String cancelEnter;
    HomeUI.GetInfoFileProgress task;
    volatile String error = "Error";
    
    {
        popupMenu = new JPopupMenu();
        popupMenu.addPopupMenuListener(this);//The listener will listen to popupMenu visibility and update the components
        tfsPanel = this.createTextFieldFileChooserPanel("Enter URL");
        Action[] actions = textField.getActions();
        int len = actions.length;
        Action action;
        for(int i = 0; i < len; ++i){
            action = actions[i];
            if(action.getValue(Action.NAME).equals(DefaultEditorKit.copyAction)){
                popupMenu.add(action);
            }else if(action.getValue(Action.NAME).equals(DefaultEditorKit.cutAction)){
                popupMenu.add(action);
            }else if(action.getValue(Action.NAME).equals(DefaultEditorKit.pasteAction)){
                popupMenu.add(action);
            }else if(action.getValue(Action.NAME).equals(DefaultEditorKit.selectAllAction))
                popupMenu.add(action);
        }
    }
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public HomeUI(MainWindow mainWindow){
        this.mainWindow = mainWindow;
        setPreferredSize(new Dimension(650, 480));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setOpaque(true);
        setBackground(new Color(100, 100, 100, 98));
        cancelEnter = canCancel ? "Cancel" : "Enter";
        fetchFileButton = new JButton(cancelEnter);
        fetchFileButton.setPreferredSize(new Dimension(60, 20));
        fetchFileButton.setToolTipText("Get links for this video.");
        fetchFileButton.addActionListener(this);
        downloadButton = new JButton("Download");
        downloadButton.setToolTipText("Click to start downloading");
        downloadButton.addActionListener(this);
        resetButton = new JButton("Reset UI");
        resetButton.setToolTipText("Reset this window");
        resetButton.addActionListener(this);
        margin = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        if(AppProperties.curDirectory == null){
            fileChooser = new JFileChooser();
        }else{
            fileChooser = new JFileChooser(new File(AppProperties.curDirectory));
        }
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setBorder(margin);
    }
    
    public void setUIComponents(){
        tfsPanel.setOpaque(true);
        add(tfsPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        fetchFileButton.setOpaque(true);
        add(fetchFileButton);
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
        textField = new CJTextField(20){
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
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRect(0, 0, getWidth(), getHeight());
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
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRect(0, 0, getWidth(), getHeight());
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
    
    public String getText(){
        return textField.getText();
    }
    
    public void setDirectory(String newValue){
        String oldValue = currentDirectory;
        currentDirectory = newValue;
        pcs.firePropertyChange("directory", oldValue, newValue);
    }
    
    public void addNotice(){
        pcs.firePropertyChange("Start", false, true);
    }
    
    @Override
    public void actionPerformed(ActionEvent event){
        JComponent component = (JComponent)event.getSource();
        if(event.getSource() == fetchFileButton){
            if(textField.getText().length() == 0){
                return;
            }
            int j = setId();
            if(j == -1){
                return;
            }
            if(canCancel){
                if(task != null){
                    task.cancel(true);
                    remove(progress);
                    revalidate();
                    repaint();
                }
                canCancel = !canCancel;
                cancelEnter = "Enter";
                fetchFileButton.setText(cancelEnter);
                fetchFileButton.setToolTipText("Get links for this video.");
                return;
            }
            canCancel = !canCancel;
            cancelEnter = "Cancel";
            fetchFileButton.setText(cancelEnter);
            fetchFileButton.setToolTipText("Cancel request.");
            progress = PanelFactory.createProgressPanel("Please wait...", true);
            add(progress);
            revalidate();
            repaint();
            updateUI();
            createAndExecuteTask();
        }
        else if(event.getSource() == downloadButton){
            addNotice();//Notifies its listener object(MainWindow object) to add a download task
        }
        else if(event.getSource() == resetButton){
            fetchFileButton.setEnabled(true);
            canCancel = !canCancel;
            cancelEnter = "Enter";
            fetchFileButton.setText(cancelEnter);
            fetchFileButton.setToolTipText("Get links for this video.");
            for(int i = getComponentCount()-1; i > -1; i--){
                remove(i);
            }
            setUIComponents();
            revalidate();
            updateUI();
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
            }else if(component instanceof CustomRadioButton){
                urlKey = ((CustomRadioButton)component).getText();
                if(ta == null){
                    ta = new JTextArea();
                    ta.setLineWrap(true);
                }
                ta.setText(yt.links.get(urlKey));
                pcs.firePropertyChange("update", false, true);
//                updateUI();
//                repaint();
        }
    }
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.pcs.addPropertyChangeListener(listener);
    } 
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
        String property = event.getPropertyName();
        if("error".equals(property)){
            String s = (String)event.getNewValue();
            if(s.startsWith("java.net.UnknownHostException")){
                error = "No internet connection. Please make sure you're connected to the internet.";
            }else if(s.startsWith("java.net.SocketTimeoutException")){
                error = "Server was timed out. Please try again.";
            }
        }
    }
    
    private void createAndExecuteTask(){
        task = new HomeUI.GetInfoFileProgress();
        task.execute();
    }
    
    private int setId(){
        String s = textField.getText()+"&";
        int start = s.indexOf("v=");
        int end = s.indexOf("&", start);
        if((start > -1)&&(end > -1)){
        id = s.substring(start+2, end);
        }else{
          JOptionPane.showMessageDialog(mainWindow, "Invalid url. Please enter a valid one", "Error", JOptionPane.ERROR_MESSAGE);
          return -1;
        }
        return 0;
    }
    
    protected String getId(){
        return id;
    }
    
    @Override
    public void paintComponent(Graphics g){
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D)g.create();
    g2d.drawImage(AppProperties.backgroundImg, 0, 0, null);
    g2d.dispose();
    }
    
    @Override
    public void keyPressed(KeyEvent event){
        if(event.getKeyCode() == KeyEvent.VK_ENTER){
            if((task != null) && (!task.isDone())){
                task.cancel(true);
                remove(progress);
                revalidate();
                repaint();
            }
            if(textField.getText().length() == 0){
                return;
            }
            int j = setId();
            if(j == -1){
                return;
            }
            canCancel = true;
            cancelEnter = canCancel ? "Cancel" : "Enter";
            fetchFileButton.setText(cancelEnter);
            fetchFileButton.setText(cancelEnter);
            progress = PanelFactory.createProgressPanel("Please wait...", true);
            add(progress);
            revalidate();
            repaint();
            updateUI();
            createAndExecuteTask();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent event){
        
    }
    
    @Override
    public void keyTyped(KeyEvent event){
        
    }
    
    class CJTextField extends JTextField implements MouseListener{
        CJTextField(int columns){
            super(columns);
//            popupMenu.addMouseListener(this);
            addMouseListener(this);
        }
//        final MouseListener mouseListener = new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent event){
                HomeUI.this.showPopup(event);
            }

            @Override
            public void mouseClicked(MouseEvent event){
                HomeUI.this.showPopup(event);
            }
            @Override
            public void mouseReleased(MouseEvent event){
                HomeUI.this.showPopup(event);
            }
           @Override
           public void mouseExited(MouseEvent event){
               
           }
           @Override
           public void mouseEntered(MouseEvent event){
               
           }
  //      };
    }
    
    private void showPopup(MouseEvent event){
        if(event.isPopupTrigger()){
            popupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }
    
    @Override
    public void popupMenuCanceled(PopupMenuEvent event){
        pcs.firePropertyChange("update", false, true);
    }
    
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent event){
        pcs.firePropertyChange("update", false, true);
    }
    
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent event){
        
    }
}