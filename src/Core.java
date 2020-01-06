import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;  

public class Core {

	
	static GUI gui;//instantiation of GUI class - used to create a separate thread
	static Traveler traveler;//class used to move 'finch'
	static DateTimeFormatter dtf;
	static Thread s;
	static String input[];
	static String status1="";
	static String status2="";
	static float length;
	static int zigzagQuantity;
	static int velocity;
	static int timeSection;
	static String startTime;
	static String endTime;
	static int sectionPairs;
	static float pathLength;
	static double distance;
	
	static boolean connectedFinch=false;
	static boolean ready=false;
	static boolean abort=false;
	public static boolean changed=false;

	public static void main(String args[]) {
		
		initialize();
		
		do{
			ready=false;
			abort=false;
			//if the window is closed - terminate
			if(!s.isAlive())System.exit(0);
			
			//when GUI gets a valid user input and a user clicks the Start button
			//- it changes ready flag to true. Until that moment - current Thread waits
			waitForInput(ready,s);
			
			//calculations
			velocity = Math.round((float)((Math.random()*1000)%100+50));
			timeSection = Math.round((length*12000)/velocity);
			sectionPairs = zigzagQuantity/2;
			pathLength = length * zigzagQuantity;
			distance = pathLength / Math.sqrt(2);
			
			//traverse Zigzag
			traverseZigzag();

			//saving data to a text file
			writeToFile();
			
			GUI.showProgres(0, true);
			traveler.myf.buzzBlocking(0,0);//to avoid playing a tune after aborting the program
			
			/*/program will Terminate if a certain tick box is ticked,
			 * but it will not happen if the program was aborted
			 */
		} while (!GUI.finish||abort);
			
			System.exit(0);//terminate
		
	}
	private static void traverseZigzag() {
		
		startTime = dtf.format(LocalDateTime.now());//storing the start time
		//traversing Zigzag
		if(!abort)
		for(int i=0;i<sectionPairs;i++) {
			if(Core.abort)break;
			if(!s.isAlive())System.exit(0);//if the window is closed - terminate
			traveler.twoSectionsForward(velocity,timeSection,i, sectionPairs);}
	
		//turning around
		if(!s.isAlive())System.exit(0);//if the window is closed - terminate
		if(!abort)
		traveler.turnAround();
	
		//traversing Zigzag back
		if(!abort)
		for(int i=0;i<sectionPairs;i++) {
			if(Core.abort)break;
			if(!s.isAlive())System.exit(0);//if the window is closed - terminate
			traveler.twoSectionsBackwards(velocity,timeSection,(i==sectionPairs-1),i,sectionPairs);}
	
		endTime = dtf.format(LocalDateTime.now());//storing the end time
		
	}
	private static void initialize() {
		//initialization
				input=new String[2];
				input[0]="0";
				input[1]="0";
				gui= new GUI();
				s = new Thread(gui,"scan");
				s.start();
				dtf = DateTimeFormatter.ofPattern("HH:mm:ss"); 
				traveler = new Traveler();
				connectedFinch=true;
	}
	
	/*/
	 * This method creates an instantiation of Waiter class. It wasn't possible to
	 * use Thread.wait() method in a static context
	 */
	static void waitForInput(boolean untillTrue, Thread s) {
		Waiter waiter = new Waiter();
		while(!ready) {
			waiter.waiter();
			if(!s.isAlive()) {System.exit(0);}
		}
		
	}
	private static void writeToFile(){
		
		if(!abort)
		try {
			
			FileWriter writehandle = new FileWriter(System.getProperty("user.dir")+"\\data.txt");
			BufferedWriter bw = new BufferedWriter(writehandle);
			   
			
			   bw.write("The length of the traversed path:"+pathLength);
			   bw.newLine();
			   bw.write("The distance travelled:"+distance);
			   bw.newLine();
			   bw.write("The start time:"+startTime);
			   bw.newLine();
			   bw.write("The end time:"+endTime);
			   bw.newLine();
			   bw.write("***************************");
			   bw.newLine();
			   bw.close();
			   writehandle.close();
			
			
		}
		catch(Exception e) {
			GUI.finish=false;//if program can't save Data to a file - it shouldn't terminate
			GUI.lblLengthMustBe.setText("Couldn't write data to a text file.");
			
		}
		
		   
	}
	public static boolean validation(String[] input) {
		
		extractString(input);
		boolean output =true;
		
		try {validateLength(length);status1="";}
		catch(Error e) {
			output=false;
			status1=e.message;}
		
		try {validateQuantity(zigzagQuantity);status2="";}
		catch(Error e) {
			output=false;
			status2=e.message;}
		
		return output;
	}
	
	static boolean validateLength(float length) throws Error {
		
		Error f1 = new Error(false,"Error! Length in a wrong format!");
		Error f2 = new Error(false,"Error! Length must be in a range: 20-80!");
		
		if(length==-1)
			throw f1;
		
		else if(length<20||length>80)
			throw f2;
		
		return true;
	}
	
	static boolean validateQuantity(int zigzagQuantity) throws Error{
		
		Error i1 = new Error(true,"Error! Quantity in a wrong format!");
		Error i2 = new Error(true,"Error! Quantity must be in a range: 2-10!");
		Error i3 = new Error(true,"Error! Quantity must be an even number!");
		

		if(zigzagQuantity==-1)
			throw i1;
		
		else if(zigzagQuantity<2||zigzagQuantity>10)
			throw i2;
		
		else if(zigzagQuantity%2!=0)
			throw i3;
		
		return true;
	}
	
	//String---> integer array
	static void extractString(String[] input) {
		int[] conveyL=new int[input[0].length()];
		int[] conveyQ=new int[input[1].length()];
		
		for(int i =0;i<input[0].length();i++) {
			conveyL[i]=charToInteger(input[0].charAt(i));
		}
		length=intArrayToFloat(conveyL);
		
		for(int i =0;i<input[1].length();i++) {
			conveyQ[i]=charToInteger(input[1].charAt(i));
		}
		zigzagQuantity=intArrayToInt(conveyQ);
		
		
	}
	//char---> integer number
	static int charToInteger(char i) {
		int out=-1;
		if(i>=48&&i<=57) out=i-48;
		else if(i==46||i==44) {out=-2;}
		return out;
	}
	
	static int intArrayToInt(int[] input) {
		int sum=0;
		for(int i=0;i<input.length;i++) {
			if(input[i]==-1) {sum=-1;break;}
			else if(input[i]==-2) {sum=-1;break;}
			sum+=input[i]*Math.pow(10, input.length-1-i);
		}
		return sum;
	}
	static float intArrayToFloat(int[] input) {
		
		//integers
		int decPoint=decimalPointPos(input);
		float sum=0;
		for(int i=0;i<decPoint;i++) {
			if(input[i]==-1) {sum=-1;break;}
			sum+=input[i]*Math.pow(10, decPoint-1-i);
		}
		
		//fractions
		if(decPoint<input.length) {
			for(int i=decPoint+1;i<input.length;i++) {
				if(input[i]==-1) {sum=-1;break;}
				else if(input[i]==-2) {sum=-1;break;}
				sum+=input[i]*Math.pow(0.1f, i-decPoint);
			}
		}
		
		return sum;
	}
	static int decimalPointPos(int[] input) {
		
		int output=input.length;
		
		for(int i=0;i<input.length;i++) {
			if(input[i]==-2) {output=i;break;}
		}
		
		return output;
	}
	}



class Error extends Exception{
	private static final long serialVersionUID = 1L;
	String message;
	boolean whichOne;//determines whether an Error is linked to 'length text box'(false) or 'quantity text box'(true)
	public Error(boolean whichOne, String message) {
		this.whichOne=whichOne;
		this.message=message;
} 
	

}
class Waiter {
	public Waiter() {}
	public void waiter()  {
		try{Thread.currentThread().wait(10);}
			catch (Exception e) {
				
			}
		
	}
	
}

