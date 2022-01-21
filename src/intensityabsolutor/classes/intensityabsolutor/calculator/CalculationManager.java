/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package intensityabsolutor.calculator;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author audreyazura
 */
public class CalculationManager implements Runnable
{
    private final List<HashMap<String, File>> m_experimentsList;
    private final Map<String, BigDecimal> m_exposureMap;
    private final GUIInterface m_mainApp;
    private List<String> m_errorFiles = new ArrayList<>();
    
    public CalculationManager(List<HashMap<String, File>> p_experiments, Map<String, BigDecimal> p_exposures, GUIInterface p_app)
    {
        m_experimentsList = p_experiments;
        m_exposureMap = p_exposures;
        m_mainApp = p_app;
    }
    
    @Override
    public void run()
    {
        int nWorker = Integer.min(Runtime.getRuntime().availableProcessors(), m_experimentsList.size());
        int nSupplementaryExperiment = m_experimentsList.size() - nWorker;
        Iterator<HashMap<String, File>> experimentListIterator = m_experimentsList.iterator();
        IntensityAbsolutorThread[] experimentArray = new IntensityAbsolutorThread[m_experimentsList.size()];

        for (int workerCounter = 0 ; workerCounter < nWorker ; workerCounter +=1)
        {
            //starting as many calculation as there are available worker
            IntensityAbsolutorThread currentAbsolutor = new IntensityAbsolutorThread(experimentListIterator.next(), (HashMap<String, BigDecimal>) m_exposureMap, this);
            currentAbsolutor.start();
            experimentArray[workerCounter] = currentAbsolutor;
        }

        //waiting for the threads to finish
        for (int threadWalker = 0 ; threadWalker < m_experimentsList.size() ; threadWalker += 1)
        {
            try
            {
                experimentArray[threadWalker].join();
            }
            catch (InterruptedException ex)
            {
                InterruptedException exToSend = new InterruptedException("Thread-" + threadWalker + " execution interrupted.");
                exToSend.setStackTrace(ex.getStackTrace());
                sendException(exToSend);
                System.exit(0);
            }
            
            if (!experimentArray[threadWalker].hasProperlyFinished())
            {
                m_errorFiles.add(experimentArray[threadWalker].getSampleFileName());
            }
            
            //if there are still experiments to do, now that a new core is available, we start one of them
            if (nSupplementaryExperiment > 0)
            {
                IntensityAbsolutorThread newAbsolutor = new IntensityAbsolutorThread(experimentListIterator.next(), (HashMap<String, BigDecimal>) m_exposureMap, this);
                newAbsolutor.start();
                experimentArray[nWorker + threadWalker] = newAbsolutor;

                nSupplementaryExperiment -= 1;
            }
        }
    }
    
    void sendException(Exception p_exception)
    {
        m_mainApp.sendException(p_exception);
    }
    
    public ArrayList<String> getErrorFiles()
    {
        return new ArrayList(m_errorFiles);
    }
}
