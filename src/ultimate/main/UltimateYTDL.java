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

package ultimate.main;

import javax.swing.UIManager;
import ultimate.ui.*;
import properties.AppProperties;

/**
 *
 * @author Anaphase21
 */
public class UltimateYTDL {
//    public MainWindow mainWindow;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                try{
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }catch(Exception e){
                    
                }
                AppProperties.loadBackgroundImage();
                MainWindow mainWindow = new MainWindow();
                mainWindow.init();
                mainWindow.setFrame();
            }
        });
    }
}
