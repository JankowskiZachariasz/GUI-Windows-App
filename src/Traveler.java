import edu.cmu.ri.createlab.terk.robot.finch.Finch;

public class Traveler {

	private int turnTime=2000;
	private int turnAroundTime=2100;
	private int forIterations=30;//to how many small steps each movement is divided 
	public Finch myf;
	
	public Traveler() {
		myf=new Finch();
		
	}
	
	public void twoSectionsForward(int velocity, int timeSection,int i,int z) {
		/*/
		 * Integer "i" is a current for loop iteration
		 * Integer "z" is the planned number of iterations
		 */
		myf.setLED(0,255,0);//setting the LED to a green light
		if(!Core.abort)//this is necessary for instant abort
		myf.buzz(1000, timeSection+forIterations);//playing tone 1
		//for loop makes it possible to track finch's position
		if(!Core.abort)//this is necessary for instant abort
		for(int x=0;x<forIterations;x++) {
			if(Core.abort)break;//this is necessary for instant abort
			myf.setWheelVelocities(velocity, velocity, timeSection/forIterations);
			//sending progress to GUI
			GUI.showProgres((int)Math.floor(((float)(i+0.5f*x/forIterations)*100f)/(float)z),true);
		}
		myf.setLED(0,0,0);//turn off the LED
		if(!Core.abort)//this is necessary for instant abort
		myf.setWheelVelocities(60, -60, turnTime); //turn 90° right
		myf.setLED(255, 0, 0);//setting the LED to a red light
		if(!Core.abort)//this is necessary for instant abort
		myf.buzz(2000, timeSection+forIterations);
		//for loop makes it possible to track finch's position
		if(!Core.abort)//this is necessary for instant abort
		for(int x=0;x<forIterations;x++) {
			if(Core.abort)break;//this is necessary for instant abort
			myf.setWheelVelocities(velocity, velocity, timeSection/forIterations);
			//sending progress to GUI
			GUI.showProgres((int)Math.floor(((float)(i+0.5f+0.5f*x/forIterations)*100f)/(float)z),true);
		}
		myf.setLED(0,0,0);//turn off the LED
		if(!Core.abort)//this is necessary for instant abort
		myf.setWheelVelocities(-60, 60, turnTime); //turn 90° left
	}
	public void twoSectionsBackwards(int velocity, int timeSection, boolean last, int i, int z) {
		myf.setLED(255,0,0);
		if(!Core.abort)
		myf.buzz(2000, timeSection+forIterations);
		//for loop makes it possible to track finch's position
		if(!Core.abort)
		for(int x=0;x<forIterations;x++) {
			if(Core.abort)break;
			myf.setWheelVelocities(velocity, velocity, timeSection/forIterations);
			GUI.showProgres((int)Math.floor(((float)(z-i-0.5f*x/forIterations)*100f)/(float)z),false);
		}
		myf.setLED(0,0,0);
		if(!Core.abort)
		myf.setWheelVelocities(-60, 60, turnTime); //turn 90° left
		myf.setLED(0, 255, 0);
		if(!Core.abort)
		myf.buzz(1000, timeSection+forIterations);
		//for loop makes it possible to track finch's position
		if(!Core.abort)
		for(int x=0;x<forIterations;x++) {
			if(Core.abort)break;
			myf.setWheelVelocities(velocity, velocity, timeSection/forIterations);
			GUI.showProgres((int)Math.floor(((float)(z-i-0.5f-0.5f*x/forIterations)*100f)/(float)z),false);
		}
		myf.setLED(0,0,0);
		if(!last&&!Core.abort)
		myf.setWheelVelocities(60, -60, turnTime); //turn 90° right
	}
	public void turnAround() {
		myf.setWheelVelocities(-60,60,turnAroundTime);
	}
}
