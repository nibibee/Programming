package managers;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import data.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Sabitov Danil
 * @version 1.0
 * Class with user`s commands
 */
public class ConsoleManager {

    /** Field for saving collection into csv file */
    private File collectionCsv;
    /** Field for saving date of initialization of the collection */
    private Date dateInitial;
    /** Field for saving date of modification of the collection */
    private DataChecker modificationDate;
    /** LinkedList collection for keeping a collection as java-object */
    protected LinkedList<Organization> organizations = new LinkedList<>();
    /** HashMap collection for making an instruction */
    private final HashMap<String, String> consoleInfo;

    public ConsoleManager(String pathToFile) throws IOException {
        checkFile(pathToFile);
        Integer fileId = null;
        String fileName = null;
        Long fileAnnnualTurnover = null;
        String fileCreationDate = null;
        String fileFullName = null;
        OrganizationType fileType = null;
        Location fileTown = null;
        Address fileAddress = null;
        Coordinates fileCoordinates = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm:ss");
        try (
                Reader reader = Files.newBufferedReader(Paths.get(pathToFile));
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
        ) {
            String[] nextLine;
            int goodElements = 0;
            int badElements = 0;
            while ((nextLine = csvReader.readNext()) != null) {
                boolean ok = true;
                try {
                    fileId = Integer.valueOf(nextLine[0]);
                } catch (NumberFormatException e) {
                    ok = false;
                }
                try {
                    fileName = nextLine[1];
                } catch (Exception e) {
                    ok = false;
                }
                try {
                    fileAnnnualTurnover = Long.valueOf(nextLine[2]);
                } catch (NumberFormatException e) {
                    ok = false;
                }
                try {
                    fileCreationDate =nextLine[3];
                    if (DataChecker.isValidDate(fileCreationDate)){
                        fileCreationDate = nextLine[3];
                    }
                    else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    ok = false;
                }
                try {
                    fileFullName = nextLine[4];
                } catch (Exception e) {
                    ok = false;
                }
                try {
                    fileType = OrganizationType.valueOf(nextLine[5]);
                } catch (IllegalArgumentException e) {
                    ok = false;
                }
                try {
                    String[] t1 = nextLine[6].split(" ");
                    fileTown = new Location(Integer.parseInt(t1[1]), Long.parseLong(t1[2]), t1[3]);
                    fileAddress = new Address(t1[0], fileTown);
                } catch (Exception e) {
                    ok = false;
                }
                try {
                    String[] t2 = nextLine[7].split(" ");
                    fileCoordinates = new Coordinates(Double.parseDouble(t2[0]), Float.parseFloat(t2[1]));
                } catch (NumberFormatException e) {
                    ok = false;
                }
                if(ok) {
                    Organization organizationFile = new Organization(fileId, fileName, fileAnnnualTurnover, fileCreationDate, fileFullName, fileType,
                            fileAddress, fileCoordinates);
                    organizations.addFirst(organizationFile);
                    goodElements++;
                }
                else {
                    badElements ++;
                }

            }
            System.out.println("Collection was loaded succesfully! " +"\n" +
                    "Number of correct elements: " + goodElements);
            System.out.println("Number of corrupted elements: " + badElements);
        } catch (IOException | CsvValidationException e) {
            System.out.println("Syntax error! Try again!");
        }

    }



    /**method that checks file and path to it*/
    public void checkFile(String pathToFile) throws IOException {
        String filePath = null;

        Scanner newFile = new Scanner(pathToFile);
        filePath = newFile.nextLine();

        try {
            if (filePath == null) throw new FileNotFoundException();
        } catch (FileNotFoundException exception) {
            System.out.println("Error! You did not specify the path to the file!");
            System.exit(1);
        }

        File file = new File(filePath);

        try {
            if (file.isDirectory()) throw new FileNotFoundException();
        } catch (FileNotFoundException exception) {
            System.out.println("Error! You did not specify the path to the file!");
            System.exit(1);
        }

        try {
            if (file.exists()) {
                this.collectionCsv = new File(filePath);
                System.out.println("Path to file was checked successfully!");
            } else {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException exception) {
            System.out.println("File is not found! Check path to file!");
            System.exit(1);
        }
        try {
            if (!collectionCsv.canRead() || !collectionCsv.canWrite()) throw new SecurityException();
        } catch (SecurityException securityException) {
            System.out.println("Error! This file is protected from writing or reading" + '\n' + "Check both permissions! ");
            System.exit(1);
        }
        this.collectionCsv = new File(filePath);
        this.dateInitial = new Date();
    }


    /**show, print all collection's elements in string representation*/
    public void show(){
        if (organizations.size() != 0) {
            for (Organization org : organizations) {
                System.out.println(org.toString() + "\n");
            }
        } else {
            System.out.println("Collection is empty!");
        }
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
        consoleInfo.put("save", " Save collection to the file");
        consoleInfo.put("execute_script {file_name}", " Read and execute a script from specified file");
        consoleInfo.put("exit", " End the program (without saving)");
        consoleInfo.put("remove_first", " Delete a first element from the collection");
        consoleInfo.put("add_if_min {element}", " If an element's value is less than the smallest element value, add a new element to the collection");
        consoleInfo.put("remove_greater {element}", "Delete all collection's elements which are bigger than current element");
        consoleInfo.put("count_by_full_name {fullName}", " Print the amount of elements which fullName field is equal to given");
        consoleInfo.put("filter_greater_than_annual_turnover {annualTurnover}", " Print elements which value annualTurnover field is bigger than given");
        consoleInfo.put("print_unique_official_address", " Print unique values of officialAddress fields from all collection's elements");
    }
    /** help, show available commands" */
    public void help () {
        for (Map.Entry<String, String> entry : consoleInfo.entrySet()) {
            System.out.println(entry.getKey() + entry.getValue());
        }
    }


    /**info, print collection's info to standard output*/
    public void info () {
        System.out.println("Type of collection: java.util.LinkedList");
        System.out.println("Initialization date: " +dateInitial);
        System.out.println("Amount of elements in the collection: " + organizations.size());
    }


    /**method that gets ID of collection's element
     * @return Integer id
     */
    public int makerID() {
        int maxID = 0;
        for (Organization org : organizations) {
            if (org.getId() > maxID) {
                maxID = org.getId();
            }
        }
        return maxID + 1;
    }

    /**method that gets name of collection's element
     * @return String name
     */
    public String makerName() {
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter organization's name: ");
                String name = scanner.nextLine().trim(); // trim receive name without spaces
                if (name.isEmpty()) {
                    System.out.println("Name cannot be empty or null");
                    continue;
                }
                return name;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Name value must be string!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Programm was stopped!");
                System.exit(1);
            }
        }
    }

    /**method that gets X-coordinate of organization
     * @return Double x
     */
    public double makerX() {
        while (true) {
            try {
                System.out.println("Enter X coordinate. Value cannot be empty.");
                Scanner scanner = new Scanner(System.in);
                double x = scanner.nextDouble();
                String iX = Double.toString(x);
                if (iX.isEmpty()) {
                    System.out.println("Coordinate cannot be empty or null");
                    continue;
                }
                return x;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Coordinate value must be double number!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Programm was stopped!");
                System.exit(1);
            }
        }
    }

    /**method that gets Y-coordinate of organization
     * @return Float y
     */
    public float makerY() {
        while (true) {
            try {
                System.out.println("Enter Y coordinate. Value cannot be empty.");
                Scanner scanner = new Scanner(System.in);
                float y = scanner.nextFloat();
                String iY = Float.toString(y);
                if (iY.isEmpty()) {
                    System.out.println(" Coordinate cannot be empty or null");
                    continue;
                }
                return y;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Coordinate value must be float number!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /**method that makes coordinates from method makerX and makerY*/
    public Coordinates makerCoordinates() {
        return new Coordinates(makerX(), makerY());
    }

    /**method that gets organization's annual turnover
     * @return Long annualTurnover
     */
    public long makerAnnualTurnover() {
        while (true) {
            try {
                System.out.println("Enter organization's annual turnover. Value must be greater than 0.");
                Scanner scanner = new Scanner(System.in);
                long turnover = scanner.nextLong();
                String ITurnover = String.valueOf(turnover).trim();
                if (turnover <= 0) {
                    System.out.println("Annual turnover must be greater than 0. Try again!");
                    continue;
                }
                return turnover;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Annual turnover value must be long number!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /** method that gets organization's full name
     * @return String fullName
     */
    public String makerFullName() {
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter organization's FULL name: ");
                String fullname = scanner.nextLine().trim();
                return fullname;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Name value must be string!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /** method that gets organization's type
     * @return OrganizationType type
     */
    public OrganizationType makerOrganizationType() {
        while (true) {
            try {
                System.out.println("Choose type of organization. Enter the number which respond for desired type.");
                System.out.println("Types: 'TRUST' - 1, 'PRIVATE_LIMITED_COMPANY' - 2, 'OPEN_JOINT_STOCK_COMPANY' - 3");
                Scanner scanner = new Scanner(System.in);
                int type = scanner.nextInt();
                switch (type) {
                    case 1:
                        return OrganizationType.TRUST;
                    case 2:
                        return OrganizationType.PRIVATE_LIMITED_COMPANY;
                    case 3:
                        return OrganizationType.OPEN_JOINT_STOCK_COMPANY;
                    default:
                        break;
                }
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Organziation type must be a number (1, 2, 3). Try again!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /** method that gets address of organization(street)
     * @return String street
     * */
    public String makerAddressStreet() {
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter organization's street address: ");
                String street = scanner.nextLine().trim();
                if (street.length() > 148) {
                    System.out.println("Street value cannot be greater than 148. Try again!");
                    continue;
                }
                if (street.isEmpty()) {
                    System.out.println("Name cannot be empty or null");
                    continue;
                }
                return street;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Street value must be string!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /** method that gets organization's town X-coordinate
     * @return x
     */
    public int makertownX() {
        while (true) {
            try {
                System.out.println("Enter town's X coordinate. Value cannot be empty.");
                Scanner scanner = new Scanner(System.in);
                int x = scanner.nextInt();
                String iX = Integer.toString(x);
                if (iX.isEmpty()) {
                    System.out.println("Coordinate cannot be empty or null");
                    continue;
                }
                return x;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Coordinate value must be integer number!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /** method that gets organization's town Y-coordinate
     * @return y
     */
    public long makertownY() {
        while (true) {
            try {
                System.out.println("Enter town's Y coordinate. Value cannot be empty.");
                Scanner scanner = new Scanner(System.in);
                long y = scanner.nextLong();
                String iY = Long.toString(y);
                if (iY.isEmpty()) {
                    System.out.println("Coordinate cannot be empty or null");
                    continue;
                }
                return y;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Coordinate value must be long number!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /** method that gets address of organization(name of town)
     * @return town
     */
    public static String makerTownName() {
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter organization's town: ");
                String town = scanner.nextLine().trim();
                if (town.isEmpty()) {
                    System.out.println("Town value cannot be empty or null");
                    continue;
                }
                return town;
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("Town value must be string!");
            } catch (NoSuchElementException noSuchElementException) {
                System.out.println("Program was stopped!");
                System.exit(1);
            }
        }
    }

    /** method that makes organization's town name and coordinates
     *from methods makertownX, makertownY,makerTownName
     */
    public  Location makerLocation() {
        return new Location(makertownX(), makertownY(), makerTownName());
    }

    /** method that makes official organization's address from methods makerAddressStreet and makerLocation */
    public  Address makerAddress() {
        return new Address(makerAddressStreet(), makerLocation());
    }


    /** add {element}, adding a new element to collection using all maker-methods */
    public void add () {
        Organization newOrg = new Organization(makerID(), makerName(), makerAnnualTurnover(), makerDate(),
                makerFullName(), makerOrganizationType(), makerAddress(), makerCoordinates());
        organizations.add(newOrg);

    }


    /** update id {element}, method that updates element by it's ID */
    public void update_id (String id) {
        try {
            id = id.trim().replaceAll("[ ]{2,}", " ");
            ListIterator<Organization> iterator = organizations.listIterator();
            boolean check = false;
            while (iterator.hasNext()) {
                Organization s = iterator.next();
                int intID = s.getId();
                String stringID = String.valueOf(intID);
                if (stringID.equals(id)) {
                    check = true;
                    iterator.remove();
                    Organization organizationUpdated = new Organization(intID, makerName(), makerAnnualTurnover(), makerDate(),
                            makerFullName(), makerOrganizationType(), makerAddress(), makerCoordinates());
                    iterator.add(organizationUpdated);
                    System.out.println("Element was updated!");
                }
            }
            if (!check) {
                System.out.println("Element with this ID is not found. Try again!");
            }
        } catch (NumberFormatException numberFormatException) {
            System.out.println("An argument must be a number! Try again!");
        }
    }

    /** remove_by_id, method that removes element by it's id */
    public void remove_by_id (String id) {
        try {
            id = id.trim().replaceAll("[ ]{2,}", " ");
            ListIterator<Organization> iterator = organizations.listIterator();
            boolean check = false;
            while (iterator.hasNext()) {
                Organization s = iterator.next();
                int intID = s.getId();
                String stringID = String.valueOf(intID);
                if (stringID.equals(id)) {
                    iterator.remove();
                    System.out.println("Element was removed!");
                    check = true;
                }
            }
            if (!check) {
                System.out.println("Element with this ID is not found. Try again!");
            }
        } catch (NumberFormatException numberFormatException) {
            System.out.println("An argument must be a number! Try again!");
        }
    }


    /** clear, method that removes all elements from collection */
    public void clear () {
        organizations.clear();
        System.out.println("All elements from collection were removed!");
    }

    /** save, method that saves collection to CSV file */
    public void save () {
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(String.valueOf(collectionCsv)));

                CSVWriter csvWriter = new CSVWriter(
                        writer, CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            String[] header = {"id", "name", "annualTurnover", "creationDate", "fullName",
                    "organizationType", "officialAddress", "coordinates"};
            csvWriter.writeNext(header);
            for (Organization organization : organizations) {
                csvWriter.writeNext(new String[]{String.valueOf(organization.getId()), organization.getName(),
                        organization.getAnnualTurnover().toString(), String.valueOf(organization.getDate()), organization.getFullName(),
                        organization.getType().toString(), organization.getOfficialAddress().toString(), organization.getCoordinates().toString()});
            }
            System.out.println("Collection was saved successfully!");
        }catch (IOException e) {
            System.out.println("Collection wasn't saved! Try again!");
        }
    }

    /** add_if_min {element}, Method that adds a new element to the collection
     *if it's annual turnover is less than the smallest collection's turnover
     */
    public void add_if_min (Organization org){
        long minAnnualTurnover = Long.MAX_VALUE;
        for (Organization organization : organizations) {
            minAnnualTurnover = organization.getAnnualTurnover();
        }
        if (org.getAnnualTurnover() < minAnnualTurnover) {
            organizations.add(org);
            System.out.println("The minimal element was added.");
        } else {
            System.out.println("The element's annual turnover is bigger than the collection's minimal element " +
                    "element was not added. Try another value!");
        }
    }

    /** remove_greater {element}, method that remove collection's elements
     if it's annual turnover is more than entered value
     */
    public void remove_greater (long turnover){
        int count = 0;
        boolean check = false;
        ListIterator<Organization> iterator = organizations.listIterator();
        while (iterator.hasNext()) {
            Organization s = iterator.next();
            if (s.getAnnualTurnover() > turnover) {
                count += 1;
                check = true;
                iterator.remove();
            }
        }

        System.out.println(count + " elements were removed!");
        if (!check){
            System.out.println("There are no elements which are greater entered value.");
        }

    }

    /** remove_first, method that removes first element of collection */
    public void remove_first () {
        organizations.remove();
        if (organizations == null) {
            System.out.println("The collection is empty!");
        } else {
            System.out.println("The first element was removed!");
        }
    }

    /** count_by_full_name, method that prints number of elements which full name is equal to entered */
    public void count_by_full_name (String fullName){
        int count = 0;
        for (Organization organization : organizations) {
            if (organization.getFullName().equals(fullName)) {
                count += 1;
            }
        }
        System.out.println(count + " elements equal to entered value!");
    }

    /** filter_greater_than_annual_turnover,
     * method that prints elements which are greater than entered value
     */
    public void filter_greater_than_annual_turnover ( long annualTurnover){
        int count = 0;
        System.out.println("Elements which annual turnover is greater than entered value: ");
        for (Organization organization : organizations) {
            if (organization.getAnnualTurnover() > annualTurnover) {
                System.out.println(organization);
                count += 1;
            }
        }
        System.out.println(count + " elements were printed!");
    }

    /** print_unique_official_address, method that prints all collection's unique officialAddress values */
    public void print_unique_official_address () {
        int count = 0;
        HashSet<Organization> organizationTreeSet = new HashSet(organizations);
        System.out.println("Unique values of organization's official address: ");
        for (Organization organization : organizationTreeSet) {
            System.out.println(organization);
            count += 1;
        }
        System.out.println(count + " unique elements were printed!");
    }


    /** exit, method that finishes the program */
    public void exit () {
        System.out.println("Thank you for using my program! The program will be finished now!");
        System.exit(0);
    }

    /** execute_script file_name, method that read and execute script from needed file */
    public void execute_script (String filepath){
        try{
            filepath = filepath.trim().replaceAll("[ ]{2,}", " ");
            System.out.println("Recursion warning! To avoid it your file can't contain execute_script command!");
            BufferedReader reader = new BufferedReader(new FileReader(new File(filepath)));
            String[] commandUser;
            String command;
            while ((command = reader.readLine()) != null){
                commandUser = command.trim().toLowerCase().split(" ", 2);
                switch (commandUser[0]){
                    case "":
                        break;
                    case "help":
                        help();
                        break;
                    case "info":
                        info();
                        break;
                    case "show":
                        show();
                        break;
                    case "add":
                        add();
                        break;
                    case "update id":
                        update_id(commandUser[1]);
                        break;
                    case "remove_by_id":
                        remove_by_id(commandUser[1]);
                        break;
                    case "clear":
                        clear();
                        break;
                    case "save":
                        save();
                        break;
                    case "exit":
                        exit();
                        break;
                    case "remove_first":
                        remove_first();
                        break;
                    case "add_if_min":
                        System.out.println("Enter an element, which will be compared with other elements in collection.");
                        add_if_min(new Organization(makerID(), makerName(),
                                makerAnnualTurnover(), makerDate(),
                                makerFullName(), makerOrganizationType(),
                                makerAddress(), makerCoordinates()));
                        break;
                    case "remove_greater":
                        System.out.println("Enter an element, which will be compared with other elements in collection.");
                        remove_greater(makerAnnualTurnover());
                        break;
                    case "execute_script":
                        System.out.println("Using execute_script is prohibited!");
                        break;
                    case "count_by_full_name":
                        System.out.println("Enter organization's full name, which will be compared with element`s full name.");
                        count_by_full_name(makerFullName());
                        break;
                    case  "filter_greater_than_annual_turnover":
                        System.out.println("Enter organization's annual turnover, which will be compared with element's annual turnover");
                        filter_greater_than_annual_turnover(makerAnnualTurnover());
                        break;
                    case "print_unique_official_address":
                        print_unique_official_address();
                        break;
                    default:
                        reader.readLine();
                        break;
                }
                System.out.println("The end of the command");
            }
            System.out.println("The end of the commands");
            reader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found. Try again.");
        } catch (IOException ioException) {
            System.out.println("File reading exception. Try again.");
        }
    }


    /** method that prints current date in string representation
     * @return modificationDate*/
    public String makerDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(calendar.getTime());
    }


}

