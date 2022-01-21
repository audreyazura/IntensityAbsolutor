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

import albanlafuente.physicstools.math.ContinuousFunction;
import albanlafuente.physicstools.physics.PhysicsVariables;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 *
 * @author Alban Lafuente
 */
public class Spectra extends ContinuousFunction
{
    static public Spectra spectraFromWinSpec(File p_winSpecFile) throws FileNotFoundException, DataFormatException, ArrayIndexOutOfBoundsException, IOException
    {
        return new Spectra(p_winSpecFile, PhysicsVariables.UnitsPrefix.NANO.getMultiplier(), PhysicsVariables.UnitsPrefix.UNITY.getMultiplier(), "txt", " ", 3, new int[] {0,2});
    }
    static public Spectra callibrationAbsoluteIntensitySpectra(File p_passedFile) throws FileNotFoundException, DataFormatException, ArrayIndexOutOfBoundsException, IOException
    {
        return new Spectra(p_passedFile, PhysicsVariables.UnitsPrefix.NANO.getMultiplier(), PhysicsVariables.UnitsPrefix.UNITY.getMultiplier().divide(PhysicsVariables.UnitsPrefix.NANO.getMultiplier()), "intensity", "\t", 2, new int[] {0,1});
    }
    
    public Spectra()
    {
        super();
    }
    
    public Spectra(Spectra p_passedSpectra)
    {
        super((ContinuousFunction) p_passedSpectra);
    }
    
    public Spectra(ContinuousFunction p_passedFunction)
    {
        super(p_passedFunction);
    }
    
    public Spectra(HashMap p_function)
    {
        super(p_function);
    }
    
    private Spectra (File p_inputFile, BigDecimal p_abscissaUnitMultiplier, BigDecimal p_valuesUnitMultiplier, String p_expectedExtension, String p_separator, int p_ncolumn, int[] p_columnToExtract) throws FileNotFoundException, DataFormatException, ArrayIndexOutOfBoundsException, IOException
    {
        super(p_inputFile, p_abscissaUnitMultiplier, p_valuesUnitMultiplier, p_expectedExtension, p_separator, p_ncolumn, p_columnToExtract);
    }
    
    @Override
    public Spectra divide(BigDecimal p_dividend)
    {
        return new Spectra(super.divide(p_dividend));
    }
    
    @Override
    public Spectra avoidZeros()
    {
        return new Spectra(super.avoidZeros());
    }
    
    public Spectra substract(Spectra p_passedSpectra)
    {
        return new Spectra(super.subtract(p_passedSpectra));
    }
    
    public Spectra multiply(Spectra p_passedSpectra)
    {
        return new Spectra(super.multiply(p_passedSpectra));
    }
    
    public Spectra divide(Spectra p_passedSpectra)
    {
        return new Spectra(super.divide(p_passedSpectra));
    }
    
    public void logToFile(File outputFile, BigDecimal valueMultiplier) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        List<BigDecimal> abscissaList = new ArrayList(m_abscissa);
        
        //removing the first and last elements as they can contains some calculation errors
        abscissaList.remove(0);
        abscissaList.remove(abscissaList.size()-1);
        
        writer.write("Wavelength (nm)\tAbsolute intensity (μW/nm)");
        for(BigDecimal currentAbscissa: abscissaList)
        {
            //new scale: number.scale() - number.precision() gives the number of digits after the point in scientific notation. Setting the scale to this + 11 gives us at least 10 digits after the points, which is enough
            BigDecimal currentValue = m_values.get(currentAbscissa);
            BigDecimal convertedIntensity = (currentValue.multiply(valueMultiplier)).setScale(currentValue.scale() - currentValue.precision() + 11, RoundingMode.HALF_UP).stripTrailingZeros();
            
            writer.newLine();
            writer.write(currentAbscissa.divide(PhysicsVariables.UnitsPrefix.NANO.getMultiplier()) + "\t" + convertedIntensity);
        }
        writer.flush();
        writer.close();
    }
    
    public void selectWindow(BigDecimal p_minAbscissa, BigDecimal p_maxAbscissa)
    {
        List<BigDecimal> abscissaList = new ArrayList(m_abscissa);
        int abscissaLastIndex = abscissaList.size() - 1;
        BigDecimal previous, next;
        
        if (abscissaList.get(1).compareTo(p_minAbscissa) < 0 || abscissaList.get(1).compareTo(p_maxAbscissa) > 0)
        {
            m_values.remove(abscissaList.get(0));
            m_abscissa.remove(abscissaList.get(0));
        }
        for(int i = 1 ; i < abscissaLastIndex ; i += 1)
        {
            previous = abscissaList.get(i-1);
            next = abscissaList.get(i+1);
            
            if((previous.compareTo(p_minAbscissa) < 0 || previous.compareTo(p_maxAbscissa) > 0)     //previous not in range
                    && (next.compareTo(p_minAbscissa) < 0 || next.compareTo(p_maxAbscissa) > 0))    //next not in range
            {
                m_values.remove(abscissaList.get(i));
                m_abscissa.remove(abscissaList.get(i));
            }
        }
        if (abscissaList.get(abscissaLastIndex-1).compareTo(p_minAbscissa) < 0 || abscissaList.get(abscissaLastIndex-1).compareTo(p_maxAbscissa) > 0)
        {
            m_values.remove(abscissaList.get(abscissaLastIndex));
            m_abscissa.remove(abscissaList.get(abscissaLastIndex));
        }
        
        //if the first abscissa is still below the minimal abscissa, we replace it explicitly
        if (m_abscissa.first().compareTo(p_minAbscissa) < 0)
        {
            m_values.put(p_minAbscissa, getValueAtPosition(p_minAbscissa));
            m_values.remove(m_abscissa.first());
            m_abscissa.remove(m_abscissa.first());
            m_abscissa.add(p_minAbscissa);
        }
        
        //if the last abscissa is still above the max abscissa, we replace it explicitly
        if(m_abscissa.last().compareTo(p_maxAbscissa) > 0)
        {
            m_values.put(p_maxAbscissa, getValueAtPosition(p_maxAbscissa));
            m_values.remove(m_abscissa.last());
            m_abscissa.remove(m_abscissa.last());
            m_abscissa.add(p_maxAbscissa);
        }
    }
}
