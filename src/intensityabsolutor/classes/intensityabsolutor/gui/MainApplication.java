/*
 * Copyright (C) 2020 Alban Lafuente
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
package intensityabsolutor.gui;

import intensityabsolutor.calculator.GUIInterface;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Alban Lafuente
 */
public class MainApplication extends Application implements GUIInterface
{
    private Stage m_mainStage;
    
    public static void main(String[] args)
    {
        launch(args);
    }
    
    @Override
    public void start(Stage stage)
    {
        //find the reference intensities given
        HashMap<String, File> referenceIntensityFiles = new HashMap<>();
        String referenceDirectoryPath = "ressources/ReferenceIntensities/";
        File referenceDirectory = new File(referenceDirectoryPath);
        for (String fileName: referenceDirectory.list())
        {
            String[] fileNameSplit = fileName.split("\\.");
            if (fileNameSplit.length > 0 && fileNameSplit[fileNameSplit.length-1].equals("intensity"))
            {
                referenceIntensityFiles.put(fileNameSplit[0], new File(referenceDirectoryPath + fileName));
            }
        }
        
        m_mainStage = stage;
        
        FXMLLoader parameterWindowLoader = new FXMLLoader(MainApplication.class.getResource("FXMLApplicationWindow.fxml"));
        
        try
        {
            Parent windowFxml = parameterWindowLoader.load();
            ((WindowController) parameterWindowLoader.getController()).initialize(this, referenceIntensityFiles);
            m_mainStage.setScene(new Scene(windowFxml));
            m_mainStage.sizeToScene();
	    m_mainStage.show();
        }
        catch (IOException ex)
        {
            Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendException(Exception p_exception)
    {
        Platform.runLater(() ->
        {
           Stage popupStage = new Stage();
        
            FXMLLoader popupLoader = new FXMLLoader(MainApplication.class.getResource("FXMLPopup.fxml"));

            try
            {
                Parent windowPopup = popupLoader.load();
                ((PopupController) popupLoader.getController()).initialize(p_exception.getMessage());
                popupStage.setScene(new Scene(windowPopup));
                popupStage.sizeToScene();
                popupStage.show();
            }
            catch (IOException ex)
            {
                Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
            } 
        });
        
        Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, p_exception.getMessage(), p_exception);
    }
    
    public Stage getMainStage()
    {
        return m_mainStage;
    }
}
