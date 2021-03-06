package helpers;
import com.opencsv.CSVWriter;
import com.sun.org.apache.xpath.internal.operations.Or;
import data.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static data.OrganizationType.*;

/**
 * @author Sabitov Danil
 * @version 1.0
 * Class with user`s commands
 */
public class ConsoleManager {

    /** Field for saving collection into csv file */
    //File collectionCsv = CollectionChecker.collectionCsv;
    /** Field for saving date of initialization of the collection */
    Date dateInitial = CollectionChecker.dateInitial;
    /** LinkedList collection for keeping a collection as java-object */
    LinkedList<Organization> organizations = CollectionChecker.organizations;
    /** HashMap collection for making an instruction */
    private final HashMap<String, String> consoleInfo;
    private static final Logger logger = Logger.getLogger(ConsoleManager.class.getName());




    /**show, print all collection's elements in string representation
     * @return result
     * */
    public String show(){
        String result = "";
        if (organizations.size() != 0) {
            organizations.sort(Comparator.comparingInt(Organization::getId));
            for (Organization org : organizations) {
                logger.log(Level.INFO,org.toString() + "\n");
                result += org.toString() + "\n";
            }
        } else {
            logger.log(Level.INFO,"Collection is empty!");
            result = "Collection is empty!";
        }
        return result;
    }

    {
        //creating an instruction
        consoleInfo = new HashMap<>();
        consoleInfo.put("help", " Show available commands");
        consoleInfo.put("info", " Print collection's info to standard output");
        consoleInfo.put("show", " Print all collection's elements in string representation");
        consoleInfo.put("add {element}", " Add a new element to collection");
        consoleInfo.put("update_id {element}", " Update current element's value, which ID is equal to the given");
        consoleInfo.put("remove_by_id {id}", " Delete the element from the collection using its ID");
        consoleInfo.put("clear", " Purify the collection!");
        consoleInfo.put("execute_script {file_name}", " Read and execute a script from specified file");
        consoleInfo.put("exit", " End the program");
        consoleInfo.put("remove_first", " Delete a first element from the collection");
        consoleInfo.put("add_if_min {element}", " If an element's value is less than the smallest element value, add a new element to the collection");
        consoleInfo.put("remove_greater {element}", " Delete all collection's elements which are bigger than current element");
        consoleInfo.put("count_by_full_name {fullName}", " Print the amount of elements which fullName field is equal to given");
        consoleInfo.put("filter_greater_than_annual_turnover {annualTurnover}", " Print elements which value annualTurnover field is bigger than given");
        consoleInfo.put("print_unique_official_address", " Print unique values of officialAddress fields from all collection's elements");
    }
        /** help, show available commands"
         * @return result
         * */
        public String help () {
            String result = "";
            for (Map.Entry<String, String> entry : consoleInfo.entrySet()) {
                logger.log(Level.INFO,entry.getKey() + entry.getValue());
                result += (entry.getKey() + entry.getValue() +"\n") ;
            }
            return result;
        }


        /**info, print collection's info to standard output
         * @return*/
        public String info () {
            logger.log(Level.INFO,"Type of collection: java.util.LinkedList" + "\n" +
            "Initialization date: " +dateInitial + "\n" +
            "Amount of elements in the collection: " + organizations.size());
            return ("Type of collection: java.util.LinkedList" + "\n"+
                    "Initialization date: " +dateInitial + "\n"+
                    "Amount of elements in the collection: " + organizations.size());
        }


    /**method that gets ID of collection's element
     * @return Integer id
     */
    public int makerID() {
        int ID = -1;
        for (Organization org : organizations) {
            if (org.getId() < ID) {
                ID = org.getId();
            }
        }
        return ID - 1;
    }


    /** add {element}, adding a new element to collection using all maker-methods */
        public void add (Organization org) {
            int id = makerID();
            org.setId(id);
            org.setCreationDate(getDate());
            organizations.add(org);
        }

        /** update id {element}, method that updates element by it's ID */
        public String update_id (Organization org) {
            String result = "";
            //boolean check = true;
            Organization org1 = organizations.stream()
                    .filter(p -> (p.getId().equals(org.getId())))
                    .findAny()
                    .orElse(null);
            if (org1 != null) {
                organizations.remove(org1);
                org.setCreationDate(getDate());
                organizations.add(org);
                organizations.sort(Comparator.comparingInt(Organization::getId));
                logger.log(Level.INFO,"Element was updated!");
                result = "Element was updated!";
                return result;
            }
            else{
                logger.log(Level.WARNING,"Element with this ID is not found. Try again!");
                result = "Element with this ID is not found. Try again!";
            }
            return result;
        }


        /** remove_by_id, method that removes element by it's id */
        public String remove_by_id (Organization org) {
            String result = "";
            try {
                Organization org1 = organizations.stream()
                        .filter(p -> (p.getId().equals(org.getId())))
                        .findAny()
                        .orElse(null);
                if (org1 != null) {
                    organizations.remove(org1);
                    organizations.sort(Comparator.comparingInt(Organization::getId));
                    logger.log(Level.INFO,"Element was removed!");
                    result = "Element was removed successfully!";
                }else {
                    logger.log(Level.WARNING,"Element with this ID is not found. Try again!");
                    result = "Element with this ID is not found. Try again!";

                }
            } catch (NumberFormatException numberFormatException) {
                logger.log(Level.WARNING,"An argument must be a number! Try again!");
            }
            return result;
        }


        /** clear, method that removes all elements from collection */
        public String clear (Integer userid) {
            Connection c ;
            Statement stmt;
            String result = "";
            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:9800/studs","s313318", "mes758");
                c.setAutoCommit(false);
                logger.log(Level.INFO,"Opened database successfully");
                stmt = c.createStatement();
                String sql = "DELETE from ORGANIZATIONS  WHERE USERID = "+ userid +";";
                stmt.executeUpdate(sql);
                c.commit();
                result = "Elements from collection by your account were removed!";
            } catch (SQLException | ClassNotFoundException e) {
                logger.log(Level.WARNING,"Error while clearing the data!");
            }
            return result;
        }

            /** save, method that saves collection to CSV file */
        public void save (Integer userid) {
            Connection c;
            Statement stmt;
            ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
            Lock readLock = readWriteLock.readLock();
            Lock writeLock = readWriteLock.writeLock();

            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs","s313318", "mes758");
                //c = DriverManager.getConnection("jdbc:postgresql://localhost:9800/studs","s313318", "mes758");
                c.setAutoCommit(false);
                logger.log(Level.INFO,"-- Opened database successfully");
                String sql = "";
                String idS = "";
                LinkedList <String> ids = new LinkedList<>();
                for (Organization organization : organizations) {
                    if (organization.getId() > 0) {
                        ids.add(organization.getId().toString());
                    }
                }
                idS = String.join( ",", ids);
                if (ids.size() > 0) {
                    sql = "DELETE FROM ORGANIZATIONS WHERE ID NOT IN (" + idS + ");";
                }
                if(organizations.size() == 0){
                    sql += "DELETE FROM ORGANIZATIONS WHERE USERID = "+ userid +";";
                }
                stmt = c.createStatement();
                for (Organization organization : organizations) {
                    if(organization.getId() < 0) {
                        sql += "INSERT INTO ORGANIZATIONS (NAME,ANNUALTURNOVER,CREATIONDATE,FULLNAME,ORGANIZATIONTYPE,STREET," +
                                "TOWNX,TOWNY,COORDX,COORDY,TOWNNAME, USERID ) VALUES ('" + organization.getName() + "'," +
                                " " + organization.getAnnualTurnover().toString() + ", '" + organization.getCreationDate() + "'," +
                                "'" + organization.getFullName() + "', '" + organization.getType().toString() + "'," +
                                " '" + organization.getOfficialAddress().getStreet() + "', " + organization.getOfficialAddress().getTown().getX() + "," +
                                "" + organization.getOfficialAddress().getTown().getY() + ", "  + organization.getCoordinates().getX() + ", " +
                                " " + organization.getCoordinates().getY() +", '"+ organization.getOfficialAddress().getTown().getName() +"', "+ organization.getUserId() +");";
                    }else{
                        sql += "UPDATE ORGANIZATIONS SET NAME = '" + organization.getName() + "', ANNUALTURNOVER = "+ organization.getAnnualTurnover().toString() +"," +
                                "CREATIONDATE =  '"+ organization.getCreationDate() + "', FULLNAME =  '" + organization.getFullName() + "'," +
                                "ORGANIZATIONTYPE = '" + organization.getType().toString() + "', STREET = " +
                                " '" + organization.getOfficialAddress().getStreet() + "', TOWNX = " + organization.getOfficialAddress().getTown().getX() + " , " +
                                "TOWNY = "+ organization.getOfficialAddress().getTown().getY() +", COORDX = " + organization.getCoordinates().getX() +", " +
                                "COORDY = " + organization.getCoordinates().getY() +", TOWNNAME = '"+ organization.getOfficialAddress().getTown().getName() +"', " +
                                "USERID = "+ organization.getUserId() +" WHERE ID = "+ organization.getId()+";";
                    }
                }
                writeLock.lock();
                readLock.lock();
                stmt.executeUpdate(sql);
                readLock.unlock();
                writeLock.unlock();
                stmt.close();
                c.commit();
                logger.log(Level.INFO,"-- Records created successfully");

                logger.log(Level.INFO,"Organizations in save method = " + organizations.size());
                logger.log(Level.INFO,"Collection was saved successfully!");

                organizations.clear();
                CollectionChecker collectionChecker = new CollectionChecker();

            }catch (ClassNotFoundException | SQLException | IOException e) {
                logger.log(Level.WARNING,"Collection wasn't saved! Try again! "  + e.getMessage());
            }
            }

    /** add_if_min {element}, Method that adds a new element to the collection
     *if it's annual turnover is less than the smallest collection's turnover
     */
        public String add_if_min (Organization org) {
            String result = "";
            long minAnnualTurnover = Long.MAX_VALUE;
            for (Organization organization : organizations) {
                minAnnualTurnover = organization.getAnnualTurnover();
            }
            if (org.getAnnualTurnover() < minAnnualTurnover) {
                result = "The minimal element was found! Enter element's values.";
                logger.log(Level.INFO,"The minimal element was found! Enter element's values." + minAnnualTurnover);
            } else {
                logger.log(Level.WARNING,"The element's annual turnover is bigger than the collection's minimal element " +
                        "element was not added. Try another value!");
                result = "The element's annual turnover is bigger than the collection's minimal element." + "\n" +
                        "Element was not added. Try another value!";
            }
            return result;
        }

        /** remove_greater {element}, method that remove collection's elements
        if it's annual turnover is more than entered value
         */
        public String remove_greater (Organization org){
            int count = 0;
            String result = "";
            try {
                List<Organization> org1 = organizations
                        .stream()
                        .filter(p -> (p.getAnnualTurnover() > org.getAnnualTurnover() && p.getUserId() == org.getUserId()))
                        .collect(Collectors.toList());
                if (org1.size() > 0) {
                    count = org1.size();
                    for (Organization organization : org1) {
                        organizations.remove(organization);
                    }

                        organizations.sort(Comparator.comparingInt(Organization::getId));
                    logger.log(Level.INFO,count + " elements with annual turnover greater than " + org.getAnnualTurnover() + " were removed successfully!");
                        result = count + " elements with annual turnover greater than " + org.getAnnualTurnover() + " were removed successfully!";

                } else {
                    logger.log(Level.WARNING,"Elements with annual turnover greater than entered are not found. Try again!");
                    result = "Elements with annual turnover greater than entered are not found. Try again!";

                }
            }catch (NumberFormatException numberFormatException){
                logger.log(Level.WARNING,"An argument must be a number! Try again!");
            }
            return result;
        }

        /** remove_first, method that removes first element of collection */
        public String remove_first (Integer userid) {
            String result = "";
            if (organizations.size() > 0) {
                System.out.println(organizations.getFirst());
                if (organizations.getFirst().getUserId() == userid){
                    organizations.remove();
                    logger.log(Level.INFO,"The first element was removed!");
                    result = "The first element was removed!";
                }else {
                    result = "You have no access to delete this element!";
                }
            }else {
                logger.log(Level.INFO,"The collection is empty!");
                result = "The collection is empty!";
            }
            return result;
        }

        /** count_by_full_name, method that prints number of elements which full name is equal to entered */
        public String count_by_full_name (Organization org){
            String result = "";
            int count = 0;
            for (Organization organization : organizations) {
                if (organization.getFullName().equals(org.getFullName())) {
                    count += 1;
                }
            }
            result = count + " elements equal to entered value!";
            logger.log(Level.INFO,count + " elements equal to entered value!");
            return result;
        }

        /** filter_greater_than_annual_turnover,
         * method that prints elements which are greater than entered value
         */
        public String filter_greater_than_annual_turnover (Organization org) {
            int count = 0;
            String result = "";
            try {
                List<Organization> org1 = organizations
                        .stream()
                        .filter(p -> (p.getAnnualTurnover() > (org.getAnnualTurnover())))
                        .collect(Collectors.toList());
                if (org1.size() > 0) {
                    count = org1.size();
                    for (Organization organization : org1) {
                        result += "\n" +organization;
                    }
                    logger.log(Level.INFO,result);
                    organizations.sort(Comparator.comparingInt(Organization::getId));
                    result = count + " elements with annual turnover greater than " + org.getAnnualTurnover() + " were printed successfully!" +result;
                    logger.log(Level.INFO,count + " elements with annual turnover greater than " + org.getAnnualTurnover() + " were printed successfully! " + result);
                } else {
                    logger.log(Level.WARNING,"Elements with annual turnover greater than entered are not found. Try again!");
                    result = "Elements with annual turnover greater than entered are not found. Try again!";

                }
            }catch (NumberFormatException numberFormatException){
                logger.log(Level.WARNING,"An argument must be a number! Try again!");
            }
            return result;
        }

        /** print_unique_official_address,
         *  method that prints all collection's unique officialAddress values
         */
        public String print_unique_official_address() {
            int count = 0;
            String result = "";
            List <Organization> orgss = organizations.stream().filter(p -> organizations.stream().filter
                    (p1 -> p1.getOfficialAddress().getStreet().equals(p.getOfficialAddress().getStreet())).count() == 1)
                    .collect(Collectors.toList());
            for (Organization org : orgss) {
                //count++;
                result += "\n" + org.getOfficialAddress();
            }
            count = orgss.size();
            if (count > 0) {
                result = count + " unique elements were found: " + result;

            }else {
                result = "No unique elements were found!";
                logger.log(Level.INFO,"No unique elements were found!");
            }
            return result;
        }


        /** exit, method that finishes the program */
        public void exit () {
            logger.log(Level.INFO,"Thank you for using my program! The program will be finished now!");
            System.exit(0);
        }

        /** execute_script file_name, method that read and execute script from needed file */
        public String execute_script (String filepath, Integer userId, LinkedList<String> myFiles){
            String result = "";
            if (myFiles.contains(filepath)){
                result += "This script was executed already!";
                return result;
            }
            myFiles.add(filepath);
            try{
                filepath = filepath.trim().replaceAll("[ ]{2,}", " ");
                result = "Starting script file! " + "\n";
                BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
                StringBuilder cmd = new StringBuilder();
                String[] commandUser;
                String command;
                while ((command = reader.readLine()) != null){
                    commandUser = command.trim().replaceAll("[ ]{2,}", " ").toLowerCase().split(" ", 3);
                    //Sender sender = new Sender();
                    //sender.getCommand() = commandUser[1];
                    switch (commandUser[0]){
                        case "":
                            break;
                        case "help":
                           result += "\n" + help();
                            break;
                        case "info":
                            result += "\n" + info();
                            break;
                        case "show":
                            result += "\n" + show();
                            break;
                        case "add":
                            result += "\n" + "Command 'add' was read successfully!";
                            /*
                            Organization org = new Organization();
                            org.setUserId(userId);
                            Address address = new Address();
                            Location location = new Location();
                            OrganizationType organizationType = null;
                            Coordinates coordinates = new Coordinates();
                            for( int i = 0; i<10; i++) {
                                String line = reader.readLine();
                                String val = line.split(":")[1].trim();
                                switch (line.split(":")[0].trim()) {
                                    case "name":
                                        org.setName(val);
                                        break;
                                    case "turnover":
                                        org.setAnnualTurnover(Long.parseLong(val));
                                        break;
                                    case "fullname":
                                        org.setFullName(val);
                                        break;
                                    case "type":
                                        int v1 = Integer.parseInt(val);
                                        switch (v1) {
                                            case 1:
                                                organizationType = TRUST;
                                                break;
                                            case 2:
                                                organizationType = PRIVATE_LIMITED_COMPANY;
                                                break;
                                            case 3:
                                                organizationType = OPEN_JOINT_STOCK_COMPANY;
                                                break;
                                        }
                                            org.setType(organizationType);
                                            break;

                                            case "street address":
                                                address.setStreet(val);
                                                break;

                                            case "town's X coordinate":
                                                location.setX(Integer.parseInt(val));
                                                break;

                                                case "town's Y coordinate":
                                                    location.setY(Long.parseLong(val));
                                                    break;

                                    case "town":
                                        location.setName(val);
                                        break;

                                    case "X coordinate":
                                        coordinates.setX(Double.parseDouble(val));
                                        break;
                                    case "Y coordinate":
                                        coordinates.setY(Float.parseFloat(val));
                                        break;
                                }
                            }
                            org.setId(makerID());
                            org.setCreationDate(getDate());
                            org.setCoordinates(coordinates);
                            address.setTown(location);
                            org.setOfficialAddress(address);
                            //org.setUserId();
                            add(org);
                            result += "Organization was added successfully!";

                             */
                            break;
                        case "update_id":
                            result += "\n" + "Command 'update_id' was read successfully!";

                            /*
                            Organization org1 = new Organization();
                            org1.setUserId(userId);
                            Address address1 = new Address();
                            Location location1 = new Location();
                            OrganizationType organizationType1 = null;
                            Coordinates coordinates1 = new Coordinates();
                            for( int i = 0; i<10; i++) {
                                String line = reader.readLine();
                                String val = line.split(":")[1].trim();
                                switch (line.split(":")[0].trim()) {
                                    case "name":
                                        org1.setName(val);
                                        break;
                                    case "turnover":
                                        org1.setAnnualTurnover(Long.parseLong(val));
                                        break;
                                    case "fullname":
                                        org1.setFullName(val);
                                        break;
                                    case "type":
                                        int v1 = Integer.parseInt(val);
                                        switch (v1) {
                                            case 1:
                                                organizationType1 = TRUST;
                                                break;
                                            case 2:
                                                organizationType1 = PRIVATE_LIMITED_COMPANY;
                                                break;
                                            case 3:
                                                organizationType1 = OPEN_JOINT_STOCK_COMPANY;
                                                break;
                                        }
                                        org1.setType(organizationType1);
                                        break;

                                    case "street address":
                                        address1.setStreet(val);
                                        break;

                                    case "town's X coordinate":
                                        location1.setX(Integer.parseInt(val));
                                        break;

                                    case "town's Y coordinate":
                                        location1.setY(Long.parseLong(val));
                                        break;

                                    case "town":
                                        location1.setName(val);
                                        break;

                                    case "X coordinate":
                                        coordinates1.setX(Double.parseDouble(val));
                                        break;
                                    case "Y coordinate":
                                        coordinates1.setY(Float.parseFloat(val));
                                        break;
                                }
                            }
                            String line = reader.readLine();
                            String val1 = line.split(",")[3].trim();
                            org1.setId(Integer.valueOf(val1));
                            org1.setCreationDate(getDate());
                            org1.setCoordinates(coordinates1);
                            address1.setTown(location1);
                            org1.setOfficialAddress(address1);
                            add(org1);
                            result += " \n" + "Organization was updated successfully!" + "\n";
                                update_id(org1);

                             */
                            break;
                        case "remove_by_id":
                            Organization org2 = new Organization();
                            org2.setUserId(userId);
                            if(commandUser.length > 1 && commandUser[1].trim().length() >= 1){
                            try {
                                org2.setId(Integer.parseInt(commandUser[1]));
                            }catch (NumberFormatException e){
                                result += "\n" + "The value must be Integer!";
                            }
                                result += "\n" + remove_by_id(org2);
                            }
                            else {
                                result += "\n" +"No id was found for removing!";
                            }
                            break;
                        case "clear":
                            result += "\n" + clear(userId);
                            break;
                        case "exit":
                            result += "\n" + "%%%exit%%%";
                            break;
                        case "remove_first":
                            result += "\n" + remove_first(userId);
                            break;
                        case "add_if_min":
                            result += "\n" + "Command 'add_if_min' was read successfully!";

                            /*
                            Organization org3 = new Organization();
                            String line3 = reader.readLine();
                            String val3 = line3.split(",")[3].trim();
                            org3.setAnnualTurnover(Long.parseLong(val3));
                            add_if_min(org3);
                            result += " \n" + "Organization was added successfully!" + "\n";
                                                   */
                            break;
                        case "remove_greater":
                            Organization org4 = new Organization();
                            org4.setUserId(userId);
                            if(commandUser.length > 1){
                                try {
                                    org4.setAnnualTurnover(Long.parseLong(commandUser[1]));
                                    result += "\n" + remove_greater(org4);
                                }catch (NumberFormatException e){
                                    result += "\n" + "The value must be an Integer!";
                                }
                            }
                            else {
                                result += "\n" +"No value 'annual turnover' was found for removing!";
                            }
                            break;
                        case "execute_script":
                            if(commandUser.length > 1 && commandUser[1].trim().length() > 4){
                                result += "\n" + execute_script(commandUser[1], userId, myFiles);
                            }
                            else {
                                result += "\n" +"No files found for execution!";
                            }
                            break;
                        case "count_by_full_name":
                            Organization org5 = new Organization();
                            if(commandUser.length > 1){
                                org5.setFullName(commandUser[1]);
                                result += "\n" + count_by_full_name(org5);
                            }
                            else {
                                result += "\n" +"No full name was found!";
                            }
                            break;
                        case  "filter_greater_than_annual_turnover":
                            Organization org6 = new Organization();
                            if(commandUser.length > 1){
                            try {
                                org6.setAnnualTurnover(Long.parseLong(commandUser[1]));
                                result += "\n" + filter_greater_than_annual_turnover(org6);
                            }catch (NumberFormatException e){
                                result += "\n" + "The value must be an Integer!";
                            }
                            }
                            else {
                                result += "\n" +"No id was found for removing!";
                            }
                            break;
                        case "print_unique_official_address":
                           result += "\n" + print_unique_official_address();
                            break;

                        default:
                            reader.readLine();
                            result += "\n" + "Unknown command! Try again! Write 'help' for list of available commands.";
                            break;
                    }
                    System.out.println("The end of the command");
                    result += "\n" +"The end of the command";
                }
                System.out.println("The end of the commands");
                result += "\n" +"The end of the commands";
                reader.close();
            } catch (FileNotFoundException fileNotFoundException) {
                logger.log(Level.WARNING,"File not found. Try again.");
            } catch (IOException ioException) {
                logger.log(Level.WARNING,"File reading exception. Try again.");
            }
            return result;
            }



        /** method that prints current date in string representation
         * @return modificationDate
         */
        public String getDate(){
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            return formatter.format(calendar.getTime());
        }


}

