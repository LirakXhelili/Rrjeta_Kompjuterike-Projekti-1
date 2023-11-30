import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class Server {

    private static final int port = 2911;
    private static final String host = "localhost";
    private static final List<Socket> clientArray = new ArrayList<>();
    private static final List<Socket> authenticatedClients = new ArrayList<>();
    private static final Map<Socket, String> clientUsernames = new HashMap<>();
    private static final String PASSWORD = "route66";

    public static void main(String[] args) {
        try {
            
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Serveri është i lidhur në portin " + host + ":" + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Klienti u lidh: " + clientSocket.getRemoteSocketAddress());

                clientArray.add(clientSocket);

                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while((line = input.readLine()) != null) {
                String[] commands = line.split("\\s+");
                switch (commands[0]) {
                    case "/lexo":
                        handleReadCommand(commands, output);
                        break;
                    case "/shkruaj":
                    case "/ekzekuto":
                        handleWriteOrExecuteCommand(commands, output, clientSocket);
                        break;
                    case "/dalje":
                        handleExitCommand(clientSocket, output);
                        break;
                    case "/lista":
                        handleListCommand(clientSocket, output);
                        break;
                    case "/password":
                        handlePasswordCommand(commands, output, clientSocket);
                        break;
                    case "/listofile":
                        handleListFilesCommand(output);
                        break;
                    case "/msg":
                        handlePrivateMessage(commands, clientSocket);
                        break;
                    default:
                        output.println("400: Komandë e pavlefshme");
                }
            }
        } catch (IOException var8) {
            var8.printStackTrace();
        }

    }

     private static void handleReadCommand(String[] commands, PrintWriter output) {
        if (!checkLengthOfCommandArray(commands)) {
            output.println("Argumente të pavlefshme.");
            return;
        }

        String emriFile = commands[1];
        if (new File(emriFile).exists()) {
            try {
                String permbajtja = new String(Files.readAllBytes(Paths.get(emriFile)));
                output.println("File përmban: " + permbajtja);
            } catch (IOException e) {
                output.println("Gabim në leximin e file-it.");
            }
        } else {
            output.println("File nuk ekziston.");
        }
    }

}
