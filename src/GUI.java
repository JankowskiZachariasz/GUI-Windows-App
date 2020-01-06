import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

public class GUI implements Runnable {

	private static Canvas canvas;//object used to draw the preview
	private static GUI window;
	private static Display display;
	private static Shell shell;
	private static Text text;//label over length text box
	private static Text text_1;//label over quantity text box
	private static Button start;
	private static Label lblStatusConnected;//label showing the connection status
	public static Label lblLengthMustBe;//label showing errors if an invalid input has been given (it also show other errors)
	private static ProgressBar progressBar;
	public static Button btnCloseProgramOnce;//tick box 
	
	private static boolean lengthBoxValidity=false; 
	private static boolean quantityBoxValidity = false;
	private static boolean changed=true;//program knows, if there's a need to redraw canvas
	private static boolean goingBack=true;//indicates whether the finch is moving towards an end or back to a start
	public static boolean finish=false;//tick box result
	private static String w[] = new String[2]; 
	private static long counter=0;
	public static int progress;//this variable indicates how far the finch is, with traversing the zigzag (progress=100 --> finch is at the end) 
	
	//initialization - method created by an built-in SWT generator
	public static void main() {
		try {
			window = new GUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		
		while (!shell.isDisposed()) {
			
			if (!display.readAndDispatch()) {
				
				checkInputs();
				if(!Core.ready)//the display shouldn't sleep when the path is being traversed (canvas wouldn't refresh)
				display.sleep();
			}
		}
	}
	//widget declaration and the method used for drawing the zigzag on 'canvas'
	protected void createContents() {
		
		shell = new Shell();
		shell.setSize(566, 436);
		shell.setText("SWT Application");
		
		text = new Text(shell, SWT.BORDER);
		text.setBounds(50, 47, 99, 21);
		
		
		text_1 = new Text(shell, SWT.BORDER);
		text_1.setBounds(173, 47, 119, 21);
		
		start = new Button(shell, SWT.NONE);
		start.setBounds(352, 20, 148, 48);
		start.setText("Start");
		//what happens when the start Button is clicked
		start.addSelectionListener(new SelectionAdapter()
		{
			@Override 
			public void widgetSelected(SelectionEvent e) {
				if(Core.ready)
				{Core.abort=true;}//abort button function
				else
				Core.ready=true;//start button function 
		
				}
			});
		
		CLabel lblLength = new CLabel(shell, SWT.NONE);
		lblLength.setBounds(50, 20, 61, 21);
		lblLength.setText("length");
		
		CLabel lblNumberOfSections = new CLabel(shell, SWT.NONE);
		lblNumberOfSections.setBounds(173, 20, 119, 21);
		lblNumberOfSections.setText("number of sections");
		
		canvas = new Canvas(shell, SWT.NONE);
		canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		canvas.setBounds(50, 100, 450, 214);
		
		progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(50, 320, 450, 30);
		
		lblStatusConnected = new Label(shell, SWT.NONE);
		lblStatusConnected.setBounds(50, 356, 137, 15);
		lblStatusConnected.setText("");
		
		lblLengthMustBe = new Label(shell, SWT.NONE);
		lblLengthMustBe.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblLengthMustBe.setBounds(50, 79, 450, 15);
		lblLengthMustBe.setText("");
		
		btnCloseProgramOnce = new Button(shell, SWT.CHECK);
		btnCloseProgramOnce.setBounds(261, 356, 239, 16);
		btnCloseProgramOnce.setText("Close program after traversing the zigzag");
	    //redrawing canvas
		canvas.addPaintListener(new PaintListener() { 
	        public void paintControl(PaintEvent e) { 
	        	int sections=Core.zigzagQuantity;
	            int baseLength=0;
	            int currentY=0;
	            if(sections!=0)//to avoid division by zero
	            baseLength=400/sections;
	            
	            //drawing path consisting of given number of sections
	            for(int i=0;i<sections;i++) {
	            	int oldY,newY=0;
	            	
	            	if(i%2==0) {
	            		oldY=baseLength+(sections-2)*10;
	            		newY=(sections-2)*10;
	            	}
	            	else {
	            		oldY=(sections-2)*10;
	            		newY=baseLength+(sections-2)*10;	
	            	}
	            e.gc.drawLine(20+baseLength*i, oldY, 20+baseLength+baseLength*i, newY);
	            currentY=oldY;
	            }
	            
	            if(sections!=0) {
	            //checking if finch traverses green light section or a red light one
	            boolean direction=Math.round((float)Math.floor((float)progress/(100f/(float)sections)))%2==0;
	            int shift=Math.round(progress%(100f/sections)*sections);//how many per cents of current section has been traversed
	            int w = Math.round(baseLength*shift/100);//how much of a current section has been traversed
	            //calculating x-axis position
	            int currentX=20+Math.round((float)Math.floor((float)progress/(100f/(float)sections)))*baseLength+w;
	            if(direction) {//green light section
	            	if(goingBack)//finch is moving towards an end
		         drawFinch_green(e,currentX,currentY+baseLength-w);
	            	else//finch is going back to the starting point
	            		drawFinch_greenBack(e,currentX,currentY+baseLength-w);
	            }
	            else {//red light section
	            	if(goingBack)//finch is moving towards an end
		         drawFinch_red(e,currentX,currentY+w);
	            	else//finch is going back to the starting point
	            		drawFinch_redBack(e,currentX,currentY+w);
	            }
	            }
	        } 
	    });

	}
	
	//this method is invoked from a while loop (it serves many purposes)
	private void checkInputs() {
		

		BoxValidation();
		
		//Start button becomes clickable when the input is correct and the connection with finch is established 
		start.setEnabled(lengthBoxValidity&&quantityBoxValidity&&Core.connectedFinch);
		
		//changing start button to abort button
		if(Core.ready) {start.setText("Abort");}
		else {start.setText("Start");}
		
		//deciding whether program terminates after traversing a zigzag
		finish=btnCloseProgramOnce.getSelection();
		
		//finch connection test
		if(Core.connectedFinch)lblStatusConnected.setText("Connected!");
		else lblStatusConnected.setText("Connecting to Finch...");
		
		counter++;//redrawing canvas and progress bar every 100 frames so that they don't blink 
		if(counter%100==0&&lengthBoxValidity&&quantityBoxValidity&&Core.ready||(changed&&lengthBoxValidity&&quantityBoxValidity)) {
			//updating progress bar
			if(goingBack)progressBar.setSelection(progress/2);
			else progressBar.setSelection(50+(100-progress)/2);
			//redrawing canvas
			canvas.redraw();}
	}
	
	//validating the input that is currently typed into boxes
	private static void BoxValidation() {
		//detecting changes in text boxes
		if(!text.getText().trim().equals(w[0])||!text_1.getText().trim().equals(w[1]))changed=true;
		else changed=false;
		w[0]=text.getText().trim();
		w[1]=text_1.getText().trim();
		
		//validating input
		Core.validation(w);
		lengthBoxValidity=(Core.status1.isEmpty());//if an error message is empty - there's no errors
		quantityBoxValidity=(Core.status2.isEmpty());//if an error message is empty - there's no errors
		Color red = new Color (Display.getCurrent (), 255, 0, 0);
		Color green = new Color (Display.getCurrent (), 0, 255, 0);
		
		//changing colors accordingly to input validity & displaying error messages

		if(quantityBoxValidity) {text_1.setBackground(green);}
		else {
			if(lengthBoxValidity)
			lblLengthMustBe.setText("Number of sections must be even and in range 2-10");
			text_1.setBackground(red);}
		if(lengthBoxValidity) {text.setBackground(green);}
		else {
			lblLengthMustBe.setText("Length must be in range 20-80");
			text.setBackground(red);}
		//correct input but no connection with finch
		if(lengthBoxValidity&&quantityBoxValidity&&!Core.connectedFinch)lblLengthMustBe.setText("No connection with Finch!");
		else if(lengthBoxValidity&&quantityBoxValidity&&Core.connectedFinch) lblLengthMustBe.setText("");
		
		
	}
	//receiving data from Traveler class
	public static synchronized void showProgres(int prgrs,boolean dir) {
		goingBack=dir;
		progress=prgrs;
		changed=true;
	}
	
	
	public void drawFinch_red(PaintEvent e,int x,int y) {
		e.gc.setBackground(display.getSystemColor(SWT.COLOR_RED)); 
		int[] vertices= {-5+x,5+y,7+x,7+y,5+x,-5+y};
		e.gc.fillPolygon(vertices);
		
	}
	public void drawFinch_redBack(PaintEvent e,int x,int y) {
		e.gc.setBackground(display.getSystemColor(SWT.COLOR_RED)); 
		int[] vertices= {-5+x,5+y,-7+x,-7+y,5+x,-5+y};
		e.gc.fillPolygon(vertices);
		
	}
	public void drawFinch_green(PaintEvent e,int x,int y) {
		e.gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN)); 
		int[] vertices= {5+x,5+y,7+x,-7+y,-5+x,-5+y};
		e.gc.fillPolygon(vertices);
		
	}
	public void drawFinch_greenBack(PaintEvent e,int x,int y) {
		e.gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN)); 
		int[] vertices= {5+x,5+y,-7+x,7+y,-5+x,-5+y};
		e.gc.fillPolygon(vertices);
		
	}


	@Override
	public void run() {
		main();
	}
}
