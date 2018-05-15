# Neurobell App

Things to fix/change and new features to add

## Modifications

1. Add white color to ListView in *res/layout/stetoscope_fragment*

   - [ ] Done.

2. Use parcelable instead of serializable when passing attributes between fragments.

   ```java
    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
               @Override
               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
   
                   float seizure_limits [] = new float[2*num_seizures_detected];
                   for(int i = 0; i<num_seizures_detected;i++) {
                       seizure_limits[i*2] = array4seizures[i][0];
                       seizure_limits[(i*2)+1] = array4seizures[i][1];
                   }
                   args.putSerializable("input",new DataHelper(input_buffer));
                   args.putFloatArray("limits",seizure_limits);
                   args.putInt("Seizures detected",num_seizures_detected);
                   args.putInt("Center", position);
                   args.putSerializable("vocoder", new 			DataHelper(vocoder.getVocoderSamples()));
                   
                   dialog.setArguments(args);
                   dialog.show(getFragmentManager(),"MyDialogFragmentTag");
               }
           });
   ```

   - [ ] Done.

3. Add more range in the probabilistic trace chart

   - [ ] Done.

4. Add progress bar when user click the annotated event to avoid double clicks.

   - [ ] Done.

5. Change shift only works in ascending order. Changes this set of conditions:

   ```java
   if(shift_value == 1 && counter == 32){
         position_flag = position - 256;
       mTimer.run();
       counter = 0;
   }
   if(shift_value == 4 && counter == 128){
       position_flag = position -256;
       mTimer.run();
       counter = 0;
   }
   if(shift_value == 8 && counter == 256){
       position_flag = position - 256;
       mTimer.run();
       counter =0;
   } else counter ++;
   
   position ++;
   ```

   - [ ] Done.

6. Unify *buffer* button with *start* button

   - [ ] Done.

7. In `private void fillMeter(double prob)`, try to change *centerY* of gradient instead of adding *th_value*. 

   - [ ] Done.

8. Changes axes of every chart to seconds.

   - [ ] Done.

9. Refine how seizures are displayed in review mode.

   - [ ] Done.

## Things to fix

- **Sonification integration.**
  - Must be listened without freezing the app. 
  - As it's an offline feature, adapt the velocity to x10.
  - How many samples to sonify? (Andriy).
- Add exception to avoid changing network in the middle of acquisition. Temporally, as it has to be an option in the future to change between layers.

## Improvements and new features

The improvements listed bellow, are the result of the usability study carried out by Montserrat Anglés.

1. We should add a tool for measuring the amplitude from peak to peak and the time that it lasts. You should be able to touch to points on the screen and a pop up message will show you the Vpp and the duration.

![](C:\Users\SalgadoE\Desktop\app rat.png)

   - [ ] Done.

2. The graphs should be always vertically centred. We have to be able to scroll forward and backwards but not up and down.

   - [ ] Done.

3. We should add the impedance of the signal (the quality of the signal).

   - [ ] Done.

4. Change the colours of the probability graph. Maybe a darker background? Try different options to choose one.

   - [x] Done.

5. Add a button to change the time scale from 20s to 10s and back.

   - [ ] Done.

6. Use the standardized filter and let the clinician make some small changes. CNN! = visualization (don't need to apply it to the probabilistic output trace).

   - [ ] Done.

7. Add a battery indicator for the acquisition device (sensors).

   - [ ] Done.

8. For each test, we should save the patient ID, the date of birth and the date and time when the test starts and finishes. ***Layout under development***

   - [ ] Done.

9. Add dialog to collect patient data prior to the acquisition.

   - [ ] Done.

10. Option to change network in real time. Consider new memory allocation.

    - [ ] Done.

11. Add a little chart to see the signal in aEEG:

  
    $$
    unknown formula --> ask andriy
    $$

    - [ ] Done.



## FUTURE TESTS

- Sonification must be tested in order to finish the sonificaiton block.

- Test to analyse the battery consumption between the networks.

  - Infinite loop of EEG with the lowest and the highest configuration (in terms of power)

  - Results will be posted in the table above:

    | Num. Layers | Shift 1 | Shift 4 | Shift 8 |
    | :---------- | :-----: | :-----: | :-----: |
    | 6 layers    |    -    |    -    |    -    |
    | 11 layers   |    -    |    -    |    -    |
