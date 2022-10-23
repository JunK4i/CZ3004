# CZ3004
Android App (Java) for CZ3004

Built and tested on: Samsung tab s8+

App Design Features:
  - MutableLiveData was used throughout the app to ensure consistent game state throughout different fragments
  - Classes and Helper Classes are well defined and organised. Will be easy to understand and extend functionality if needed 
  - Only need to be concerned with manipulating data in the MutableLivedata. The GridRecyclerAdapter will update the views accordingly.
 
App Highlight Features:
  - Fully fufill mdp checklist requirements
  
  - F1 themed design with sprite animation
  
  - D-Pad for robot movement
  
  - Interactive placement of obstacles:
    1. Drag and drop to place obstacle
    2. Drag out of map to remove
    3. Drag existing obstacle to reposition. 
    4. Click to change direction
    5. Implemented precise placement of osbtacle with scroll wheel. This is meant to minimise errors during the setup on test day
  
  - Practical features to save time:
    1. Save, Load functions for obstacle setup
    2. Reset Map
    3. Reset Bluetooth
    4. Pre-run test button to help the team ensure that 
    5. Debug tool: Write to recieved messages to test App's response to various strings manually
    6. Switch for d-pad manual control to ensure no accidental touches during actual run
    
Areas for improvement:
  1. Bluetooth has only been tested with a particular tablet, and did not work with the school provided tablet. May need to adapt it slightly to work
  2. The grid is updated at a less than ideal speed. notifyDataSetChanged() in the GridRecyclerAdapter is called in every change in game state. 
  3. The orientation is fixed to landscape in the manifest file, it is not a orientation dynamic app
  4. Some of the app's layout is hardcoded, meaning it will look weird with different screen sizes.
  5. The game timer stops when changing to a different fragment, as the counter is based on the chronometer element which lives on the fragment.
  6. Lack of animation to clearly demonstrate a 90 degree turn. 
  7. The robot "teleports" based on instructions given by algorithm. Algo handled the rounding of exact degrees and position before passing it to us. 
    
    
    
   
  
