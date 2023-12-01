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

     private static void handleListCommand(Socket clientSocket, PrintWriter output) {
        StringBuilder teksti = new StringBuilder();
        int count = 1;

        for (Socket klienti : clientArray) {
            String username = clientUsernames.getOrDefault(klienti, "N/A");

            if (klienti != clientSocket && !teksti.toString().contains("Klienti " + username)) {
                teksti.append("Klienti ").append(username).append(" - ").append(klienti.getRemoteSocketAddress()).append("\n");
                count++;
            }
        }

        String clientUsername = clientUsernames.getOrDefault(clientSocket, "N/A");
        teksti.append("Klienti ").append(clientUsername).append(" (vetvetiu) - ").append(clientSocket.getRemoteSocketAddress()).append("\n");
        teksti.append("Numri i klientëve: ").append(count).append("\n");

        output.println(teksti.toString());
    }
    private static boolean hasExecutePermission(Socket clientSocket) {
        boolean isAuthenticated = isAuthenticated(clientSocket);
        System.out.println("Përdoruesi është autentikuar: " + isAuthenticated);
        return isAuthenticated;
    }

    private static void handleExitCommand(Socket clientSocket, PrintWriter output) {
        output.println("/dalje");
        try {
            // e mbyll socketin e klientit edhe e largon klientin nga listat e klienteve
            clientSocket.close();
            clientArray.remove(clientSocket);
            authenticatedClients.remove(clientSocket);
            clientUsernames.remove(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     private static void handlePasswordCommand(String[] commands, PrintWriter output, Socket clientSocket) {
        if (commands.length < 3) {
            output.println("Formati i gabuar. Për të futur fjalëkalimin, shkruani: /password fjalëkalimi username");
            return;
        }

        String fjalekalimi = commands[1];
        String username = commands[2];

        if (fjalekalimi.equals(PASSWORD) && !authenticatedClients.contains(clientSocket)) {
            authenticatedClients.add(clientSocket);
            clientUsernames.put(clientSocket, username);
            output.println("Mirësevini " + username + "!");
        } else {
            if (authenticatedClients.contains(clientSocket)) {
                output.println("Ju jeni tashmë autentikuar.");
            } else {
                output.println("Gabim: Fjalëkalimi ose përdoruesi i gabuar.");
            }
        }
    }
}
