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
package intensityabsolutor.calculator;

import albanlafuente.physicstools.physics.PhysicsVariables;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

/**
 *
 * @author Alban Lafuente
 */
public class IntensityAbsolutorThread extends Thread
{
    private final Map<String, File> m_fileMap;
    private final Map<String, BigDecimal> m_exposureMap;
    private final CalculationManager m_manager;
    private boolean m_properlyEnded = false;
    
    public IntensityAbsolutorThread(HashMap<String, File> p_fileMap, HashMap<String, BigDecimal> p_exposureMap, CalculationManager p_manager)
    {
        m_fileMap = p_fileMap;
        m_exposureMap = p_exposureMap;
        m_manager = p_manager;
    }
    
    @Override
    public void run()
    {
        Map<String, Spectra> spectraMap = new HashMap();
        
        try
        {
            spectraMap.put("sample", Spectra.spectraFromWinSpec(m_fileMap.get("experiment")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the experiment file: " + m_fileMap.get("experiment"));
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
        
        try
        {
            spectraMap.put("sampleBG", Spectra.spectraFromWinSpec(m_fileMap.get("bgexperiment")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the background of the experiment.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
            
        try
        {
            spectraMap.put("callibrationLight", Spectra.spectraFromWinSpec(m_fileMap.get("callibration")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the spectra under standard light.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
            
        try
        {
            spectraMap.put("callibrationLightBG", Spectra.spectraFromWinSpec(m_fileMap.get("bgcallibration")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the background of the  spectra under standard light.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
            
        try
        {
            spectraMap.put("whiteLightNoSample", Spectra.spectraFromWinSpec(m_fileMap.get("whitelightnosample")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the file for the white light with no sample.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
                
        try
        {
            spectraMap.put("whiteLightNoSampleBG", Spectra.spectraFromWinSpec(m_fileMap.get("bgwlnosample")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the file for the background of the white light with no sample.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
                
        try
        {
            spectraMap.put("whiteLightSample", Spectra.spectraFromWinSpec(m_fileMap.get("whitelightwithsample")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the file for the white light with sample.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
                
        try
        {
            spectraMap.put("whiteLightSampleBG", Spectra.spectraFromWinSpec(m_fileMap.get("bgwlsample")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with the file for the background of the white light with sample.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
                
        try
        {
            spectraMap.put("lightCallibration", Spectra.callibrationAbsoluteIntensitySpectra(m_fileMap.get("lightintensity")));
        } 
        catch (DataFormatException | ArrayIndexOutOfBoundsException | IOException ex)
        {
            Exception exToSent = new Exception("Problem with file of the absolute intensity of the standard light.");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
        
        //we start by making all the Spectra defined on the same interval
        
        BigDecimal maxFirstAbscissa = spectraMap.get("sample").getAbscissa().first();
        BigDecimal minLastAbscissa = spectraMap.get("sample").getAbscissa().last();
        
        for(String key: spectraMap.keySet())
        {
            maxFirstAbscissa = maxFirstAbscissa.max(spectraMap.get(key).start());
            minLastAbscissa = minLastAbscissa.min(spectraMap.get(key).end());
        }
        
        for(String key: spectraMap.keySet())
        {
            spectraMap.get(key).selectWindow(maxFirstAbscissa, minLastAbscissa);
        }
        
        //once done, we start the calculation of the absolute intensity
        
        Spectra correctedSampleSpectra = (spectraMap.get("sample").divide(m_exposureMap.get("expintegration"))).substract(spectraMap.get("sampleBG").divide(m_exposureMap.get("expbgintegration")));
        Spectra correctedCallibrationSpectra = ((spectraMap.get("callibrationLight").divide(m_exposureMap.get("callintegration"))).substract(spectraMap.get("callibrationLightBG").divide(m_exposureMap.get("callbgintegration")))).avoidZeros();
        Spectra correctedWLNoSample = (spectraMap.get("whiteLightNoSample").divide(m_exposureMap.get("wlnosampleintegration"))).substract(spectraMap.get("whiteLightNoSampleBG").divide(m_exposureMap.get("wlnosamplebgintegration")));
        Spectra correctedWLSample = ((spectraMap.get("whiteLightSample").divide(m_exposureMap.get("wlsampleintegration"))).substract(spectraMap.get("whiteLightSampleBG").divide(m_exposureMap.get("wlsamplebgintegration")))).avoidZeros();
        
        Spectra sampleRelativeIntensity = new Spectra();
        Spectra whiteLightDivision = new Spectra();

        try
        {
            sampleRelativeIntensity = correctedSampleSpectra.divide(correctedCallibrationSpectra);
        }
        catch (ArithmeticException ex)
        {
            ArithmeticException exToSent = new ArithmeticException("Null value in the intensities of the callibration light");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }

        try
        {
            whiteLightDivision = correctedWLNoSample.divide(correctedWLSample);
        }
        catch (ArithmeticException ex)
        {
            ArithmeticException exToSent = new ArithmeticException("Null value in the intensities of the white light file with sample");
            exToSent.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSent);
            return;
        }
        
        try
        {
            (sampleRelativeIntensity.multiply(whiteLightDivision).multiply(spectraMap.get("lightCallibration"))).logToFile(m_fileMap.get("output"), PhysicsVariables.UnitsPrefix.NANO.getMultiplier().divide(PhysicsVariables.UnitsPrefix.MICRO.getMultiplier()));
            m_properlyEnded = true;
        } 
        catch (IOException ex)
        {
            IOException exToSend = new IOException("Problem when calculating the last step for the absolute intensity.");
            exToSend.setStackTrace(ex.getStackTrace());
            m_manager.sendException(exToSend);
        }
    }
    
    public boolean hasProperlyFinished()
    {
        return m_properlyEnded;
    }
    
    String getSampleFileName()
    {
        return m_fileMap.get("experiment").getName();
    }
}
