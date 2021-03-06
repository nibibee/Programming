package helpers;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import data.*;
import sun.security.util.Password;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sabitov Danil
 * @version 1.0
 * Class for checking database's collection
 */
public class CollectionChecker {

    /**
     * Field for saving date of initialization of the collection
     */
    public static Date dateInitial;

    /**
     * LinkedList collection for keeping a collection as java-object
     */
    public static LinkedList<Organization> organizations = new LinkedList<>();

    /**
     * HashMap collection for making an instruction
     */
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    Lock readLock = readWriteLock.readLock();
    Lock writeLock = readWriteLock.writeLock();
    private static final Logger logger = Logger.getLogger(CollectionChecker.class.getName());

    public CollectionChecker() throws IOException {

        Integer id = null;
        String name = null;
        Long annnualTurnover = null;
        String creationDate = null;
        String fullName = null;
        OrganizationType type = null;
        String street = null;
        Location town = null;
        Address address = null;
        Coordinates coordinates = null;
        Integer userid = null;
        try {
            Connection c;
            Statement stmt;

            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs","s313318", "mes758");
            //c = DriverManager.getConnection("jdbc:postgresql://localhost:9800/studs","s313318", "mes758");
            c.setAutoCommit(false);
            logger.log(Level.INFO,"-- Opened database successfully");
            String sql;

            stmt = c.createStatement();

            writeLock.lock();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM ORGANIZATIONS;" );
            writeLock.unlock();
            LinkedList <String> ids = new LinkedList<>();
            int goodElements = 0;
            int badElements = 0;
            boolean ok = true;
            while ( rs.next() ) {
                try {
                    id = rs.getInt("id");
                }catch (NumberFormatException e){
                    ok = false;
                }
                try {
                    ids.add(id.toString());
                }catch (Exception e){
                    ok = false;
                }
                try {
                    name = rs.getString("name");
                }catch (Exception e) {
                    ok = false;
                }
                try {
                    annnualTurnover = rs.getLong("annualturnover");
                }catch (NumberFormatException e){
                    ok = false;
                }
                try {
                    creationDate = rs.getString("creationDate");
                    if (DataChecker.isValidDate(creationDate)) {
                        creationDate = rs.getString("creationDate");
                    }
                } catch (Exception e) {
                    ok = false;
                }
                try {
                    fullName = rs.getString("fullName");
                } catch (Exception e) {
                    ok = false;
                }
                try {
                String type1 = rs.getString("organizationtype");
                switch (type1){
                    case "TRUST":
                        type = OrganizationType.TRUST;
                        break;

                    case "PRIVATE_LIMITED_COMPANY":
                        type = OrganizationType.PRIVATE_LIMITED_COMPANY;
                        break;

                    case "OPEN_JOINT_STOCK_COMPANY":
                        type = OrganizationType.OPEN_JOINT_STOCK_COMPANY;
                        break;
                }
                } catch (IllegalArgumentException e) {
                    ok = false;
                }
                try {
                    street = rs.getString("street");
                } catch (Exception e) {
                    ok = false;
                }
                address = new Address();
                address.setStreet(street);
                town = new Location();
                try {
                    town.setX(rs.getInt("townx"));
                } catch (NumberFormatException e) {
                    ok = false;
                }
                try {
                    town.setY(rs.getLong("towny"));
                } catch (NumberFormatException e) {
                    ok = false;
                }
                town.setName(rs.getString("townname"));
                address.setTown(town);

                coordinates = new Coordinates();
                try {
                    coordinates.setX(rs.getDouble("coordx"));
                } catch (NumberFormatException e) {
                ok = false;
            }
                try {
                    coordinates.setY(rs.getFloat("coordy"));
                } catch (NumberFormatException e) {
                ok = false;
            }
                try {
                    userid = rs.getInt("userid");
                } catch (NumberFormatException e) {
                ok = false;
            }
                if (ok) {
                    Organization organizationDB = new Organization(id, name, annnualTurnover, creationDate, fullName, type,
                            address, coordinates, userid);
                    organizations.addFirst(organizationDB);
                    goodElements++;
                }else {
                    badElements++;
                }
            }
            rs.close();
            stmt.close();
            c.commit();

            logger.log(Level.INFO,"Collection was loaded succesfully! " + "\n" +
                            "Number of correct elements: " + goodElements + "\n" +
                            "Number of corrupted elements: " + badElements);
            this.dateInitial = new Date();

        } catch (ClassNotFoundException | SQLException e) {
            logger.log(Level.SEVERE,"Syntax error! Try again! " + e.getMessage());
            System.exit(1);
        }
    }

}

