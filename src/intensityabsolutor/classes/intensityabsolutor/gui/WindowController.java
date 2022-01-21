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

import intensityabsolutor.calculator.CalculationManager;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Alban Lafuente
 */
public class WindowController
{
    @FXML private ChoiceBox callibrationlight;
    @FXML private Label lastlabel;
    @FXML private TextArea experiment;
    @FXML private TextField bgexperiment;
    @FXML private TextField expintegration;
    @FXML private TextField expbgintegration;
    @FXML private TextField callibration;
    @FXML private TextField bgcallibration;
    @FXML private TextField callintegration;
    @FXML private TextField callbgintegration;
    @FXML private TextField whitelightnosample;
    @FXML private TextField bgwlnosample;
    @FXML private TextField wlnosampleintegration;
    @FXML private TextField wlnosamplebgintegration;
    @FXML private TextField whitelightwithsample;
    @FXML private TextField bgwlsample;
    @FXML private TextField wlsampleintegration;
    @FXML private TextField wlsamplebgintegration;
    @FXML private TextField output;
    
    private HashMap<String, File> m_referenceIntensities;
    private MainApplication m_mainApp;
    private String m_defaultCallibrationLight = "-- Choose a light source";
    
    @FXML private void removedefault()
    {
        ObservableList<String> finalItems = callibrationlight.getItems();
        finalItems.remove(m_defaultCallibrationLight);
        callibrationlight.setItems(finalItems);
    }
    
    @FXML private void browseSample()
    {
        browse(experiment, "Chose the files with the sample spectra");
    }
    
    @FXML private void browseBGSample()
    {
        browse(bgexperiment, "Chose the file with the sample background");
    }
    
    @FXML private void browseCallibration()
    {
        browse(callibration, "Chose the file with the callibration spectra");
    }
    
    @FXML private void browseBGCallibration()
    {
        browse(bgcallibration, "Chose the file with the callibration background");
    }
    
    @FXML private void browseWhiteLightNoSample()
    {
        browse(whitelightnosample, "Chose the file with the white light spectra with no sample");
    }
    
    @FXML private void browseBGWLNoSample()
    {
        browse(bgwlnosample, "Chose the file with the white light background with no sample");
    }
    
    @FXML private void browseWhiteLightSample()
    {
        browse(whitelightwithsample, "Chose the file with the white light spectra with the sample");
    }
    
    @FXML private void browseBGWLSample()
    {
        browse(bgwlsample, "Chose the file with the white light background with the sample");
    }
    
    @FXML private void browseOutput()
    {
        DirectoryChooser browser = new DirectoryChooser();
        
        browser.setTitle("Chose the output directory");
        
	try
        {
            String fieldText = output.getText();
            browser.setInitialDirectory(new File((new File(fieldText)).getParent()));
        }
        catch (NullPointerException ex)
        {
            browser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        
        File outFolder = browser.showDialog(new Stage());
        
        if (outFolder != null)
        {
            output.setText(outFolder.getAbsolutePath());
        }
        
    }
    
    @FXML private void calculate()
    {
        List<HashMap<String, File>> experimentList  = new ArrayList<>();
        Map<String, BigDecimal> exposureMap = new HashMap<>();
        
        String[] experimentFileAddresses = experiment.getText().stripTrailing().split(System.lineSeparator());
        
        try
        {
            exposureMap.put(expintegration.getId(), new BigDecimal(expintegration.getText()));
            exposureMap.put(expbgintegration.getId(), new BigDecimal(expbgintegration.getText()));
            exposureMap.put(callintegration.getId(), new BigDecimal(callintegration.getText()));
            exposureMap.put(callbgintegration.getId(), new BigDecimal(callbgintegration.getText()));
            exposureMap.put(wlnosampleintegration.getId(), new BigDecimal(wlnosampleintegration.getText()));
            exposureMap.put(wlnosamplebgintegration.getId(), new BigDecimal(wlnosamplebgintegration.getText()));
            exposureMap.put(wlsampleintegration.getId(), new BigDecimal(wlsampleintegration.getText()));
            exposureMap.put(wlsamplebgintegration.getId(), new BigDecimal(wlsamplebgintegration.getText()));
        }
        catch (NumberFormatException ex)
        {
            NumberFormatException passedEx = new NumberFormatException("Error in at least one of exposure time.");
            passedEx.setStackTrace(ex.getStackTrace());
            m_mainApp.sendException(passedEx);
            return;
        }
        
        for (String experimentAddress: experimentFileAddresses)
        {
            Map<String, File> experimentFiles = new HashMap<>();
            
            String[] splittedAddress = experimentAddress.split("/"); 
            
            experimentFiles.put(experiment.getId(), new File(experimentAddress));
            experimentFiles.put(bgexperiment.getId(), new File(bgexperiment.getText()));
            experimentFiles.put(callibration.getId(), new File(callibration.getText()));
            experimentFiles.put(bgcallibration.getId(), new File(bgcallibration.getText()));
            experimentFiles.put(whitelightnosample.getId(), new File(whitelightnosample.getText()));
            experimentFiles.put(bgwlnosample.getId(), new File(bgwlnosample.getText()));
            experimentFiles.put(whitelightwithsample.getId(), new File(whitelightwithsample.getText()));
            experimentFiles.put(bgwlsample.getId(), new File(bgwlsample.getText()));

            if (!splittedAddress[splittedAddress.length-1].equals(""))
            {
                experimentFiles.put(output.getId(), new File(String.join("/", output.getText(), "AbsoluteIntensity_"+splittedAddress[splittedAddress.length-1])));
            }
            else
            {
                m_mainApp.sendException(new NullPointerException("Error in the input file addresses given."));
                return;
            }

            if(experimentFiles.containsValue(new File("")))
            {
                m_mainApp.sendException(new NullPointerException("Error in the file addresses given."));
                return;
            }

            experimentFiles.put("lightintensity", m_referenceIntensities.get((String) callibrationlight.getValue()));
            
            experimentList.add((HashMap) experimentFiles);
        }
        
        CalculationManager calculationModule = new CalculationManager(experimentList, exposureMap, m_mainApp);
        Thread calculationThread = new Thread(calculationModule);
        calculationThread.start();

        try
        {
            calculationThread.join();
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(WindowController.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
        
        ArrayList<String> errorFiles = calculationModule.getErrorFiles();
        String printMessage = "";
        
        if(errorFiles.size() == 0)
        {
            printMessage = "All absolute intensity files have been successfully created.";
        }
        else
        {
            printMessage = "Error with files:\n";
            for (String fileName: errorFiles)
            {
                printMessage += "\t" + fileName + "\n";
            }
        }
        
        lastlabel.setText(printMessage.stripTrailing());
        lastlabel.setManaged(true);
        lastlabel.setVisible(true);
        m_mainApp.getMainStage().sizeToScene();
    }
    
    private void browse(TextInputControl p_outputField, String p_title)
    {
        FileChooser browser = new FileChooser();
        
        browser.setTitle(p_title);
        browser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text (*.txt)", "*.txt"), new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));
	
	try
        {
            String fieldText = p_outputField.getText().split(System.lineSeparator())[0];
            browser.setInitialDirectory(new File((new File(fieldText)).getParent()));
        }
        catch (NullPointerException ex)
        {
            browser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        
        List<File> selectedFileList = new ArrayList();
        if (p_outputField.equals(experiment))
        {
            selectedFileList = browser.showOpenMultipleDialog(new Stage());
        }
        else
        {
            selectedFileList.add(browser.showOpenDialog(new Stage()));
        }
        
        String fileAddressList = p_outputField.getText();
        if (selectedFileList != null)
        {
            fileAddressList = "";
            for (File selectedFile: selectedFileList)
            {
                if (selectedFile != null)
                {
                    fileAddressList += selectedFile.getAbsolutePath() + System.lineSeparator();
                }
                else
                {
                    if (fileAddressList.equals("") && selectedFileList.size() == 1)
                    {
                        fileAddressList = p_outputField.getText();
                    }
                }
            }
        }
        
        fileAddressList = fileAddressList.strip();
        p_outputField.setText(fileAddressList);
    }
    
    public void initialize(MainApplication p_app, Map<String, File> p_referenceIntensities)
    {
        m_mainApp = p_app;
        lastlabel.setVisible(false);
        lastlabel.setManaged(false);
        m_mainApp.getMainStage().sizeToScene();
        
        m_referenceIntensities = new HashMap(p_referenceIntensities);
        TreeSet<String> referencesSet = new TreeSet(m_referenceIntensities.keySet());
        referencesSet.add(m_defaultCallibrationLight);
        ObservableList<String> references = FXCollections.observableArrayList(referencesSet);
        callibrationlight.setItems(references);
        callibrationlight.setValue(m_defaultCallibrationLight);
        
//        experiment.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absolute_samp_70mA_60s_25um.txt");
//        bgexperiment.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absolute_samp_00mA_60s_25um.txt");
//        expintegration.setText("60");
//        expbgintegration.setText("60");
//        callibration.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absolute_std_0.6s_25um.txt");
//        bgcallibration.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absolute_std_0.6s_25um_bg.txt");
//        callintegration.setText("0.6");
//        callbgintegration.setText("0.6");
//        whitelightnosample.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absorption_std_5s_25um.txt");
//        bgwlnosample.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absorption_std_5s_25um_bg.txt");
//        wlnosampleintegration.setText("5");
//        wlnosamplebgintegration.setText("5");
//        whitelightwithsample.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absorption_samp_5s_25um.txt");
//        bgwlsample.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/csv/absorption_samp_5s_25um_bg.txt");
//        wlsampleintegration.setText("5");
//        wlsamplebgintegration.setText("5");
//        output.setText("/home/audreyazura/Documents/Work/TestData/HiroseAbsoluteIntensity/SampleSet2/");
    }
}
