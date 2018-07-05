import java.io.*;
import java.net.*;
import java.util.Scanner;
import static java.lang.System.out;

/**
 * Client is the class used by the user. The user will be able to input commands in the console.<br>
 * Valid commands are: help, add, time, lecture, quit.
 * <p>
 * 'help' shows the list of available commands.<br>
 * 'add' allows the client to input lectures.<br>
 * 'time' shows the free time the student has on each day.<br>
 * 'lecture' shows all the already input lectures.<br>
 * 'quit' closes the socket, as well as the server.<br>
 * </p>
 *
 * @author Chi Nguyen, 1206243
 * @version 20.05.2017
 */
public class Client {

    private Socket socket;
    private OutputStream outputStream;
    private OutputStreamWriter outputStreamWriter;
    private PrintWriter printWriter;

    /**
     * Sole constructor
     */
    public Client() {}

    /**
     * Main method, used to start the client
     * @param args String array of main method
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.initialize();
    }

    /**
     * This method starts the client, enabling the user to input in the console.
     * Depending which command user inputs, the method will call other methods.<br>
     * Commands are non-case sensitive.
     */
    public void initialize() {
        try {
            while (true) {
                out.println("What would you like to do? type 'help' for list of commands");
                Scanner scanner = new Scanner(System.in);
                String data = scanner.nextLine();
                if (data.equalsIgnoreCase("quit")) {
                    quit();
                    break;
                } else if (data.equalsIgnoreCase("help")) {
                    out.println("add     \t to add more lectures in your time table");
                    out.println("lecture \t to show all your lectures you've input");
                    out.println("time    \t to show all your free time in your weekdays");
                    out.println("quit    \t to quit the program");
                } else if (data.equalsIgnoreCase("add")) {
                    out.println("Input format: 'Math;monday;8' Title;Weekday;TimeStart");
                    addLecture();
                } else if (data.equalsIgnoreCase("lecture")) {
                    getLectures();
                } else if (data.equalsIgnoreCase("time")) {
                    getFreeTime();
                } else {
                    out.println("Command not recognized, type 'help' for commands");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method is called when the user inputs 'add'. User can then add new lectures with the format
     * 'Title;Weekday;TimeStart'. <br> Weekday is non-case sensitive.<br> TimeStart has to be between 8 and 17.
     *
     * @throws IOException throws exception in case socket isn't found and outputStream can't send data to server
     */
    public void addLecture() throws IOException {
        socket = new Socket("localhost",1065);
        outputStream = socket.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);
        printWriter = new PrintWriter(outputStreamWriter);
        out.println("Add lecture. Type 'stop' to stop");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] split = input.split("\\W+");
        if (!input.equalsIgnoreCase("stop")) {
            try {
                if (split[1].equalsIgnoreCase("monday") || (split[1].equalsIgnoreCase("tuesday"))
                        || (split[1].equalsIgnoreCase("wednesday")) || (split[1].equalsIgnoreCase("thursday"))
                        || (split[1].equalsIgnoreCase("friday")) ) {
                    int time = Integer.parseInt(split[2]);
                    if (time > 7 && time < 18) {
                        printWriter.println("ADD;" + input);
                        out.println(input);
                        printWriter.flush();
                        addLecture();
                    } else error();
                } else error();
            } catch (NumberFormatException  e) {
                error();
            } catch (ArrayIndexOutOfBoundsException e) {
                error();
            }
        } else {
            printWriter.println();
            printWriter.flush();
        }
    }

    /**
     * Sub-method of addLecture(), called when there's an error. It prints in the console an error message and then
     * tries to repeat the addLecture() for the user to input again.
     */
    public void error() {
        out.println("Invalid input");
        printWriter.println();
        printWriter.flush();
        try {
            addLecture();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method is called when the user inputs 'time'. The method sends a request to the server, in return
     * the server replies with the free time on each day printed in the client's console.
     *
     * @throws IOException throws exception in case socket isn't found and outputStream can't send data to server
     */
    public void getFreeTime() throws IOException {
        //send to server
        socket = new Socket("localhost",1065);
        outputStream = socket.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);
        printWriter = new PrintWriter(outputStreamWriter);
        printWriter.println("TIME");
        printWriter.flush();

        //receive from server
        InputStream inputStream = socket.getInputStream(); //get bytestream
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream); //read bytestream
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader); //convert bytestream to charstream
        String receive = bufferedReader.readLine(); //read charstream
        String[] split = receive.split("\\s+");
        for (int i = 0; i < split.length; i++) {
            out.println(split[i]);
        }
    }

    /**
     * The method is called when the user inputs 'lecture'. The method sends a request to the server, in return
     * the server replies with all lectures that were previously input by the user.
     *
     * @throws IOException throws exception in case socket isn't found and outputStream can't send data to server
     */
    public void getLectures() throws IOException {
        //send to server
        socket = new Socket("localhost",1065);
        outputStream = socket.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);
        printWriter = new PrintWriter(outputStreamWriter);
        printWriter.println("LECTURE");
        printWriter.flush();

        //receive from server
        InputStream inputStream = socket.getInputStream(); //get bytestream
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream); //read bytestream
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader); //convert bytestream to charstream
        String receive = bufferedReader.readLine(); //read charstream
        String[] split = receive.split("\\s+");
        try {
            for (int i = 0; i < split.length; i++) {
                out.print(split[i].substring(4) + "\n");
            }
        } catch (StringIndexOutOfBoundsException e ) {
            out.println("No lecture found");
        }
    }

    /**
     * The method is called when the user inputs 'quit'. The method sends a message to the server declaring
     * ending the connection and close the socket.
     *
     * @throws IOException throws exception in case socket isn't found and outputStream can't send data to server
     */
    public void quit() throws IOException {
        //sends to server 'QUIT'
        socket = new Socket("localhost",1065);
        outputStream = socket.getOutputStream();
        outputStreamWriter = new OutputStreamWriter(outputStream);
        printWriter = new PrintWriter(outputStreamWriter);
        printWriter.println("QUIT");
        printWriter.flush();
        printWriter.close();
        try {
            socket.close();
        } catch (SocketException e) {
            out.println("Server closed");
        }
    }
}
