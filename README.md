# IntensityAbsolutor
A software to calculate the absolute intensity of spectra taken in the integration sphere experiment at the Akiyama laboratory at the University of Tokyo.

# Using the software
 - Chose the reference light source in the callibration light dropdown list
 - Sample spectra: enter here the files of the luminescence of your sample and the background for these experiments. Write how long was the integration on the right of the fields (only one integration time can written for the spectra files)
 - Callibration spectra: enter here the spectra taken of the callibration light selected from the dropdown menu, and the background for this experiment. Write how long was the integration on the right of the fields.
 - While light spectra with sample: enter here the spectra of the white light taken with the sample in, and the background for this experiment. Write how long was the integration on the right of the fields.
 - White light with light source: enter here the spectra taken of the white light taken with the callibration light in, and the background for this experiment. Write how long was the integration on the right of the fields.

# Adding new callibration light
New callibration lights can be added by adding a file in the `ressources/ReferenceIntensities` folder.
This must be a text file with the `.intensity` extension.
The data must be formated as two numbers seperated by a single tabulation. The first column represent the wavelength of the emission in nm, and the second colum represent the intensity of the emission at that wavelength in W/nm.
Any line of text will be ignore.
Files not following this format will not be read properly and may lead to bugs.
