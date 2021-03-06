package org.fullmetalfalcons.scouting.fileio;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.fullmetalfalcons.scouting.main.Main;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.util.HashMap;

/**
 * Reads and imports data from the config file and plists
 *
 * Created by Dan on 1/11/2016.
 */
public class Reader {

    /**
     *Loads the config file and assigns the lines to elements
     * @param fileName location of the config file
     */
    public static void loadConfig(String fileName){
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            Main.debug("Config file " + fileName + " loaded");
            String line;
            //While there are still lines in the file
            while((line=reader.readLine())!=null){
                line = line.trim();
                if (line.length()<2){
                    continue;
                }
                //If the line does not start with ##, which indicates a comment, or @ which indicated an equation
                if (!line.substring(0,2).equals("##") && line.charAt(0)!='@'){
                    //Attempt to add an Element to the main array
                    Main.addElement(line);
                } else if (line.charAt(0)=='@'){
                    Main.addEquation(line.replace("@EQUATION","").trim());
                }

            }

        } catch (IOException e) {
            Main.sendError("Something is very wrong with the config file. It's probably missing. Try and find it.",true,e);
        }


    }

    /**
     * Loads .plist files from the specified directory and creates Team Objects based off of them
     * @param plistPath The location of the directory which contains plists
     */
    public static void loadPlists(String plistPath){
        File plistDirectory = new File((plistPath.isEmpty()? plistPath: plistPath.charAt(plistPath.length()-1)=='/'?plistPath:plistPath+"/"));
        if (!plistDirectory.exists()||!plistDirectory.isDirectory()){
            Main.sendError("Team data location does not exist: " + plistPath,true);
        }
        //Only retrieve files that end in ".plist"
        File[] plistFiles = plistDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".plist");
            }
        });
        Main.debug(plistFiles.length + " plists discovered");
        //if (plistFiles.length==0){
            //Main.sendError("0 Teams discovered in location specified",true);
        //}

        for (File f: plistFiles){
            Main.debug("Loading plist " + f.getName());
            try {
                //Attempt to load the plist into an NSDictionary, which is basically a HashMap
                NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(f);
                Main.debug("Discovered " + rootDict.size() + " key/value pairs");

                //If it successfully creates an NSDictionary, it passes it to a Team object
                Main.addTeam(rootDict, f.getName());
            } catch (IOException | PropertyListFormatException | ParserConfigurationException | ParseException | SAXException e) {
                Main.sendError("An error has occurred with one of the plists: " + f.getName() + "\n" + e.getLocalizedMessage(),false,e);
            } catch (IllegalArgumentException | IndexOutOfBoundsException e){
                Main.sendError("Someone has edited the plists... and they did a bad job",false,e);
            }
        }
    }

    public static HashMap<Integer, String> loadTeamNames(){
        try (InputStream is = Main.class.getResourceAsStream("/org/fullmetalfalcons/scouting/resources/teams.txt");
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)){

            HashMap<Integer,String> teamMap = new HashMap<>();
            String line;
            String[] split;
            int i;

            while ((line=br.readLine())!=null){
                i = line.indexOf(",");
                try {
                    teamMap.put(Integer.parseInt(line.substring(0, i)), line.substring(i + 1));
                } catch (NumberFormatException e){
                    e.printStackTrace();
                    //Doesn't really affect the program, just don't want the error to pop up
                }
            }
            return teamMap;
        } catch (IOException e) {
            Main.sendError("Error loading team Names", true,e);
        }
        return new HashMap<>();
    }

}
