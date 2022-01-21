/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intensityabsolutor.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 *
 * @author audreyazura
 */
public class PopupController
{
    @FXML Label message;
    
    @FXML private void close()
    {
        ((Stage) message.getScene().getWindow()).close();
    }
    
    public void initialize(String p_message)
    {
        message.setText(p_message);
    }
}
