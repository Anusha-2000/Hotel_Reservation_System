import java.sql.*;
import java.util.Scanner;


public class Main {
    //Database URL
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";

    //System.out.println(url);
    //Database credentials
    private static final String username = "root";
    private static final String password = "***************";

    public static void main(String[] args) throws RuntimeException {
        Scanner scn = new Scanner((System.in));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        //Establish the connection
        try(Connection con = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connection established");
            //Statement stmt = con.createStatement();

            while(true) {
                displayMenu();
                System.out.print("Enter the corresponding number to choose an option: ");
                int choice = scn.nextInt();
                scn.nextLine();
                switch(choice) {
                    case 0 : displayExitMessage();
                             return;
                    case 1 : makeReservation(con, scn);
                             break;
                    case 2 : displayReservations(con);
                             break;
                    case 3 : getRoomNumber(con, scn);
                             break;
                    case 4 : updateReservation(con, scn);
                             break;
                    case 5 : deleteReservation(con, scn);
                             break;
                    default:
                        System.out.println("Invalid Choice. Try again!");
                }
            }
            //Perform database operations here
        } catch(SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        scn.close();

    }

    public static void deleteReservation(Connection con, Scanner scn) {
        System.out.println("Enter Reservation Id to be deleted: ");
        int reservationId = scn.nextInt();
        scn.nextLine();
        System.out.println("Enter to confirm Guest Name: ");
        String guestName = scn.nextLine();

        if(!reservationExists(con, reservationId)) {
            System.out.println("The reservation doesn't exist!! Try again!!");
            return;
        }

        String deleteQuery = "DELETE FROM reservations WHERE " +
                              "reservation_id = " + reservationId +
                              " AND guest_name = '" + guestName + "';";

        try {
            Statement stmt = con.createStatement();
            int rowsAffected = stmt.executeUpdate(deleteQuery);

            if(rowsAffected > 0) {
                System.out.println("Deletion Successful!! " + rowsAffected + " rows affected");
            } else {
                System.out.println("Deletion Failed!! Please try again after some time.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception inside deleteReservation: " + e);
        }
    }

    public static void updateReservation(Connection con, Scanner scn) {
        System.out.print("Enter Reservation Id: ");
        int reservationId = scn.nextInt();
        scn.nextLine();
        System.out.print("Enter Guest Name: ");
        String guestName = scn.nextLine();

        if(!reservationExists(con, reservationId)) {
            System.out.println("The given reservation doesn't exist. Please try again!");
            return;
        }

        System.out.println("1. Guest Name");
        System.out.println("2. Room Number");
        System.out.println("3. Contact Number");
        System.out.print("Which column do you want to update? Enter the corresponding number: ");
        int choice = scn.nextInt();
        scn.nextLine();
        System.out.print("Enter new value of the field: ");
        String newValue = scn.nextLine();

        String columnName;
        switch(choice) {
            case 1 : columnName = "guest_name";
                     break;
            case 2 : columnName = "room_number";
                     break;
            case 3 : columnName = "contact_number";
                     break;
            default :
                System.out.println("Invalid Choice. Try Again!!");
                return;
        }

        String updateQuery;
        if(choice == 2 ) {
            updateQuery = "UPDATE reservations SET " + columnName + " = " + newValue +
                    " WHERE reservation_id = " + reservationId + " and guest_name = '" + guestName + "';";
        } else {
            updateQuery = "UPDATE reservations SET " + columnName + " = '" + newValue +
                    "' WHERE reservation_id = " + reservationId + " and guest_name = '" + guestName + "';";
        }



        try {
            Statement stmt = con.createStatement();
            int rowsAffected = stmt.executeUpdate(updateQuery);

            if(rowsAffected > 0) {
                System.out.println("Update Successful!! " + rowsAffected + " rows affected");
            } else {
                System.out.println("Update Failed!! Please try again after some time.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception inside updateReservation: " + e);
        }
    }

    public static void getRoomNumber(Connection con, Scanner scn) {
        System.out.print("Enter reservation id: ");
        int reservationId = scn.nextInt();
        scn.nextLine();
        System.out.print("Enter guest name: ");
        String guestName = scn.nextLine();

        String getQueryWithIdAndName = "SELECT room_number FROM reservations WHERE reservation_id = "
                                        + reservationId + " AND guest_name = '" + guestName + "';";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(getQueryWithIdAndName);

            if(reservationExists(con, reservationId)) {
                int roomNumber = rs.getInt("room_number");
                System.out.println("Room number for reservation id " + reservationId + " and Guest " + guestName + " is " + roomNumber);
            } else {
                System.out.println("Reservation not found!! Check the values and try again.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception inside getRoomNumber: \n" + e);
        }


    }

    public static void displayReservations(Connection con) {
        try {
            Statement stmt = con.createStatement();
            String displayQuery = "SELECT * FROM reservations;";

            ResultSet rs = stmt.executeQuery(displayQuery);
            displayRecords(rs);

        } catch (SQLException e) {
            System.out.println("SQL Exception inside displayReservations(): " + e);
            System.out.println("Sorry, couldn't display reservations. Please try again after some time.");
        }

    }

    public static void displayRecords(ResultSet rs) {
        System.out.println("+-----------------------+------------------------------------------------------+--------------------+-------------------------------+-----------------------+");
        System.out.println("| Reservation ID        | Guest Name                                           | Room Number        | Contact Number                | Reservation Date      |");
        System.out.println("+-----------------------+------------------------------------------------------+--------------------+-------------------------------+-----------------------+");


        try {
            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                String guestName = rs.getString("guest_name");
                int roomNumber = rs.getInt("room_number");
                String contactNumber = rs.getString("contact_number");
                String reservationDate = String.valueOf(rs.getDate("reservation_date"));


                System.out.printf("| %-21d | %-52s | %-18d | %-29s | %-21s |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }
            System.out.println("+-----------------------+------------------------------------------------------+--------------------+-------------------------------+-----------------------+");
        } catch(SQLException e) {
            System.out.println("|                                                                  No records to display                                                                    |");
            System.out.println("+-----------------------+------------------------------------------------------+--------------------+-------------------------------+-----------------------+");
        }
    }

    public static boolean reservationExists(Connection con, int reservationId) {
        String query = "SELECT * FROM reservations WHERE reservation_id = " + reservationId;

        try(Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void makeReservation(Connection con, Scanner scn) {
        System.out.print("Enter Guest Name: ");
        String guestName = scn.nextLine();

        System.out.print("Enter Room Number: ");
        int roomNumber= scn.nextInt();

        System.out.print("Enter Contact Number: ");
        String contactNumber = scn.next();

        try{
            Statement stmt = con.createStatement();
            String insertQuery = "INSERT INTO reservations (guest_name, room_number, contact_number) VALUES ('"
                                    + guestName + "', "
                                    + roomNumber + ", '"
                                    + contactNumber + "');";

            int rowsAffected = stmt.executeUpdate(insertQuery);

            if(rowsAffected > 0) {
                System.out.println("Reservation successful!! " + rowsAffected + " rows affected");
            } else {
                System.out.println("Insert failed. Please enter correct values and try again");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception inside makeReservation(): " + e);
            System.out.println("Sorry, couldn't make a reservation. Please try again after some time.");
        }
    }

    public static void displayExitMessage() throws InterruptedException{
        System.out.println("Exiting system");

        int i = 5;
        while(i > 0) {
            System.out.print(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println("\nThank You for using Hotel Reservation System");

    }

    public static void displayMenu() {
        System.out.println("HOTEL MANAGEMENT SYSTEM");
        System.out.println("1. Reserve a room");
        System.out.println("2. View Reservations");
        System.out.println("3. Get Room Number");
        System.out.println("4. Update Reservations");
        System.out.println("5. Delete Reservations");
        System.out.println("0. Exit");
    }
}
