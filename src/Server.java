import java.io.*;
import java.net.*;
import java.util.Scanner;
import static java.lang.System.out;

/**
 * Server is the class that manage the Server socket. The server receives data from client which will be
 * processed and saved before replying the client data.<br>
 * All commands input by the user are saved in commands.txt <br>
 * All lectures input by the user are saved in lectures.txt <br>
 *
 * @author Chi Nguyen, 1206243
 * @version 20.05.2017
 */
public class Server {

    private String data;
    private int weekday = 0;
    private String[][] dateTime = new String[5][10];
    private boolean ok = true;
    private Socket socket;

    /**
     * Sole constructor.
     */
    public Server() {}

    /**
     * Main method, for calling the server.
     *
     * @param args String array of main method.
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }

    /**
     * This method is used to start the Server Socket.
     * After starting, the server will wait for connection with the client. When the connection is established, server
     * will wait for the String command sent by the client (add/time/lecture/quit). When the command is read, the
     * server will react accordingly to each of the command.
     */
    public void runServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(1065);
            OutputStream outputStream1 = new FileOutputStream("commands.txt");
            PrintStream printStream1 = new PrintStream(outputStream1);
            OutputStream outputStream2 = new FileOutputStream("lectures.txt");
            PrintStream printStream2 = new PrintStream(outputStream2);

            while (ok) {
                socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream(); //get bytestream
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream); //read bytestream
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader); //convert bytestream to charstream
                data = bufferedReader.readLine(); //read charstream
                System.out.println(data);
                printStream1.println(data);

                if (data.length() > 7) {
                    addLecture();
                    printStream2.println(data);
                } else {
                    switch (data) {
                        case "TIME":
                            getFreeTime(); break;
                        case "LECTURE":
                            getAllLectures(); break;
                        case "QUIT":
                            ok = false;
                            serverSocket.close();
                            break;
                        default: break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method will called when the command 'add' is read from the user.
     * The lecture input by the client will be processed before adding the lecture to lectures.txt
     *
     * @throws FileNotFoundException throws exception in case file opening error
     */
    public void addLecture() throws FileNotFoundException {
        String[] split = data.split("\\W+");
        if (split[0].equals("ADD")) {
            if (split[2].equalsIgnoreCase("monday")) {
                weekday = 0;
            } else if (split[2].equalsIgnoreCase("tuesday")) {
                weekday = 1;
            } else if (split[2].equalsIgnoreCase("wednesday")) {
                weekday = 2;
            } else if (split[2].equalsIgnoreCase("thursday")) {
                weekday = 3;
            } else if (split[2].equalsIgnoreCase("friday")) {
                weekday = 4;
            }
        }
        int time = Integer.parseInt(split[3]);
        dateTime[weekday][time - 8] = split[1];
        out.print(weekday + " ");
        out.println(time);
    }

    /**
     * The method is called when the command 'time' is read from the user.
     * The method gets the data that was previously processed by addLecture(), checks how many free hours
     * the student currently has and send the information to the client.
     *
     * @throws IOException throws exception in case socket isn't found and outputStream can't send data to client
     */
    public void getFreeTime() throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        PrintWriter printWriter = new PrintWriter(outputStreamWriter);
        for (int i = 0; i < dateTime.length; i++) {
            int freeTime = 0;
            for (int j = 0; j < dateTime[i].length; j++) {
                if (dateTime[i][j] == null)
                    freeTime++;
            }
            switch (i) {
                case 0:
                    out.print("Monday   \t");
                    printWriter.print("Monday;");
                    break;
                case 1:
                    out.print("Tuesday  \t");
                    printWriter.print("Tuesday;");
                    break;
                case 2:
                    out.print("Wednesday\t");
                    printWriter.print("Wednesday;");
                    break;
                case 3:
                    out.print("Thursday \t");
                    printWriter.print("Thursday;");
                    break;
                case 4:
                    out.print("Friday   \t");
                    printWriter.print("Friday;");
                    break;
            }
            printWriter.print(freeTime + " ");
            out.println(freeTime + " free hours");
        }
        printWriter.println();
        printWriter.flush();
    }

    /**
     * The method is called when the command 'lecture' is read from the user.
     * The method reads the file lectures.txt which was filled and send the list of lectures to the client.
     * In case file was not found the server sends an error message back to the client.
     *
     * @throws IOException throws exception in case socket isn't found and outputStream can't send data to client
     */
    public void getAllLectures() throws IOException {
        for (int i = 0; i < dateTime.length; i++) {
            OutputStream outputStream3 = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter3 = new OutputStreamWriter(outputStream3);
            PrintWriter printWriter = new PrintWriter(outputStreamWriter3);
            
            File file = new File("lectures.txt");
            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    printWriter.print(scanner.nextLine() + " ");
                }
                printWriter.println();
                printWriter.flush();
            } catch (FileNotFoundException e) {
                out.println("File not found!");
                printWriter.println("No lecture found");
                printWriter.flush();
            }
        }
    }
}
