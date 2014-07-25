package csvMaker;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
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
    
    public static int count(final String string, final String substring)
    {
       int count = 0;
       int idx = 0;

       while ((idx = string.indexOf(substring, idx)) != -1)
       {
          idx++;
          count++;
       }

       return count;
    }

    /**
     * Count the number of instances of character within a string.
     *
     * @param string     String to look for substring in.
     * @param c          Character to look for.
     * @return           Count of substrings in string.
     */
    public static int count(final String string, final char c)
    {
       return count(string, String.valueOf(c));
    }  
    private CsvMaker()
    { 	
    	super(".csv Maker");
    	setSize(600,115);
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
    
    
    
    /**
     * generateCSVFile
     * Using a directory and output filename this function generates a .csv file by compiling multiple text files
     * the directory should contain folders of teams, which contain device folders, which contain input text files
     * @param directoryName		name of directory folder to look at
     * @param fileName			name of the output file as fileName
     */
	public void generateCSVFile(String directoryName,String fileName)
    {
    	
		Properties prop = new Properties();
		InputStream propertyInput = null;
		
    	try
    	{
    		//Question,Answer,Latitude,Longitude,Altitude,Accuracy,Altitude Accuracy,Heading,Speed,Timestamp,TeamId,DeviceType,DeviceId
    	    FileWriter writer = new FileWriter(fileName+".csv");
    	    writer.append("Question,Answer,Latitude,Longitude,Altitude,Accuracy,Altitude Accuracy,Heading,Speed,Timestamp,TeamId,DeviceType,DeviceId\n");

    	    File directory = new File(directoryName);
        	//Open up each group ID
            File[] fList = directory.listFiles();
            for(File f : fList)	//goes through each group
            {
            	if(f.getName().contains("properties"))	//skipping the properties files
            	{
            		
            	}
            	else
            	{	            	
	            	propertyInput = new FileInputStream(f.getAbsolutePath()+".properties"); //open a properties file for each group
	    			prop.load(propertyInput);												//load the properties
	            	
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
	            		try	//append the old data, formatting all time stamps to UNIX time stamp format if necessary
	            		{
	            			sc = new Scanner(latestAnswersFile);
	            			sc.nextLine();
	            			sc.useDelimiter(",");
	            			while(sc.hasNextLine())
	            			{
	            				String line = sc.nextLine();
	            				
	            				String[] orig = line.split(",");									//the time stamp is the last part of data for each line
	            				orig[orig.length-1] = ""+getDate(orig[orig.length-1]).getTime();	//reformat time stamp if necessary
	            				
								Scanner scan = new Scanner(prop.getProperty(orig[0]));				//format the question number based on the properties file
								scan.useDelimiter("[^0-9]+");										//isolate the integer
	            				orig[0]=""+scan.nextInt();
	            				scan.close();
	            				
	            				int commaCount = count(line, ",");				//determine the number of commas in the line
	            				if(commaCount == 10)							//if there are 10 commas, then the answer contains a comma, and should be swapped with a period
	            				{
	            					for(int i = 0; i < orig.length; i++)
	            					{
	            						if(i==1)
	            						{
	            							writer.append(orig[i]+".");
	            						}
	            						else
	            						{
	            							if(orig[i].equals("NaN"))
	            								writer.append("null,");
	            							
	            							else
	            								writer.append(orig[i]+",");
	            						}
	            					}
	            				}
	            				else
	            				{		
		            				for(String s: orig)
		            				{
		            					if(s.equals("NaN"))								//null changes to NaN in the split function, so change it back
		            						writer.append("null,");
		            					
		            					else
		            						writer.append(s+",");						//otherwise append the data like usual
		            				}
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
	            			{
	            				try
	            				{
	            					sc.close();
	            				}
	            				catch(Exception e)
	            				{
	            					e.printStackTrace();
	            				}
	            			}
	            			if (propertyInput != null) 
	            			{
	            				try 
	            				{
	            					propertyInput.close();
	            				} 
	            				catch (IOException e) 
	            				{
	            					e.printStackTrace();
	            				}
	            			}
	            		}
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
    
    
	public static void main(String args[])
	{
		CsvMaker myCsv = new CsvMaker();
		myCsv.setVisible(true);   
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
	            //System.out.println("No Selection ");
	        }
		}
		else if(e.getSource()==goButton)
		{
			try{
				this.setTitle(".csv Maker");
				generateCSVFile(folderName.getText(),fileName.getText());
				String current = new java.io.File( "." ).getCanonicalPath();
				System.out.println("File "+fileName.getText()+".csv created in "+current);
				this.setTitle("File "+fileName.getText()+".csv created in "+current);
			}
			catch(Exception ex)
			{
				//System.out.println("File name or Folder path not valid");
				this.setTitle("File name or Folder path not valid");
			}
		}
	}
}
