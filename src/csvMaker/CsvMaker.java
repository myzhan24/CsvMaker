package csvMaker;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class CsvMaker extends JFrame implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField folderNameTitle;
	JTextField folderName;
	JTextField fileName ;
	JTextField fileNameTitle ;
	JFileChooser chooser ;
	JButton goButton ;
	JButton openButton;
		

    private Date getDate(String s) {
        try {
            // iOS
            return new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss Z (z)").parse(s.replace("GMT", ""));
        } catch (ParseException e) {
            // Something else
        }

        try {
            return new Date(Long.parseLong(s));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
    


	public void generateCSVFile(String directoryName,String fileName)
    {
    	
    	try
    	{
    		//Question,Answer,Latitude,Longitude,Altitude,Accuracy,Altitude Accuracy,Heading,Speed,Timestamp,TeamId,DeviceType,DeviceId
    	    FileWriter writer = new FileWriter(fileName);
    	    writer.append("Question,Answer,Latitude,Longitude,Altitude,Accuracy,Altitude Accuracy,Heading,Speed,Timestamp,TeamId,DeviceType,DeviceId\n");

    	    File directory = new File(directoryName);
        	//Open up each group ID
            File[] fList = directory.listFiles();
            for(File f : fList)	//goes through each group
            {
            	File groupDirectory = new File(f.getAbsolutePath());
            	File[] gList = groupDirectory.listFiles();	
            	for(File g : gList)	//goes through each phone in the group
            	{
            		int firstHyphen=g.getName().indexOf('-');					//determines the device type, android or iOS
            		String phoneType = g.getName().substring(0, firstHyphen);	
            		String phoneID=g.getName().substring(firstHyphen+1);		//determines the device ID
            		File latestAnswersFile = null;
            		
            		//Open files within the phone
            		File phoneDirectory = new File(g.getAbsolutePath());
            		File[] pList = phoneDirectory.listFiles();
            		for(File p : pList)	//goes through each of the files within a phone
            		{
            			//Determine the most recent answers file
            			if(p.getName().contains("answers"))
            			{
            				if(latestAnswersFile==null)
            					latestAnswersFile=p;
            				
            				else if(p.getName().compareTo(latestAnswersFile.getName())>0)
            					latestAnswersFile=p;
            			}       			
            		}
            		Scanner sc = null;
            		try	//append the old data, formatting all time stamps to UNIX if necessary
            		{
            			sc = new Scanner(latestAnswersFile);
            			sc.nextLine();
            			sc.useDelimiter(",");
            			while(sc.hasNextLine())
            			{
            				String[] orig = sc.nextLine().split(",");							//the timestamp is the last part of data for each line
            				orig[orig.length-1] = ""+getDate(orig[orig.length-1]).getTime();	//reformat timestamp if necessary
            				
            				for(String s: orig)
            				{
            					if(s.equals("NaN"))								//null changes to NaN in the split function, so change it back
            						writer.append("null,");
            					
            					else
            						writer.append(s+",");						//otherwise append the data like usual
            				}
            				
            				writer.append(f.getName()+",");						//append the teamID, phoneType, and device ID
            				writer.append(phoneType+",");
            				writer.append(phoneID);
            				writer.append("\n");		
            			}
            			
            		}
            		catch(Exception e)
            		{
            			
            		}
            		finally
            		{
            			if(sc!=null)
            				sc.close();
            		}
            	}
            }

    	    writer.flush();
    	    writer.close();
    	}
    	catch(IOException e)
    	{
    	     e.printStackTrace();
    	} 
    	
    }
    
    private CsvMaker()
    {
    	
    	super(".csv Maker");
    	setSize(600,110);
    	setResizable(false);
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
    	Container container = getContentPane();
    	container.setLayout(new FlowLayout());
    	fileName = new JTextField(35);
    	fileName.setText("myFileName");
    	fileNameTitle = new JTextField("File Name:", 13);
    	fileNameTitle.setEditable(false);
    	folderNameTitle = new JTextField("Data Source Directory: ",13);
    	folderNameTitle.setEditable(false);
    	chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Browse the folder to process");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addActionListener(this);
    	folderName = new JTextField(chooser.getCurrentDirectory().toString(),35);
    	
    	
    	goButton = new JButton("Create");
    	goButton.addActionListener(this);
    	
    	openButton = new JButton("Open");
    	openButton.addActionListener(this);
    	
    	
        
        
        container.add(folderNameTitle);
        container.add(folderName);
    	container.add(fileNameTitle);
    	container.add(fileName);
    	container.add(openButton);
    	container.add(goButton);
    	
    	
    }
    
	public static void main(String args[])
	{
		CsvMaker myCsv = new CsvMaker();
		myCsv.setVisible(true);
		
       // String directoryLinuxMac ="/home/myz/csvMaker/data";
       // myCsv.parsePhones(directoryLinuxMac);
       // myCsv.generateCSVFile(directoryLinuxMac,"test.csv");
        
	}

	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource()==openButton)
		{
			this.setTitle(".csv Maker");
			
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	            //System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
	            //System.out.println("getSelectedFile() : "+ chooser.getSelectedFile());
	            folderName.setText(""+chooser.getSelectedFile());
	            
	        } else {
	            System.out.println("No Selection ");
	        }
		}
		else if(e.getSource()==goButton)
		{
			try{
				generateCSVFile(folderName.getText(),fileName.getText()+".csv");
				String current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("File "+fileName.getText()+".csv created in "+current);
				this.setTitle("File "+fileName.getText()+".csv created in "+current);
			}
			catch(Exception ex)
			{
				System.out.println("File name or Folder path not valid");
			}
		}
	}
}
