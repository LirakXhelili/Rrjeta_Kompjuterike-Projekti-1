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
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //BufferedReader perdoret per te lexuar nga socket-i per hyrje
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);//PrintWriter per te shkruar ne socket per dalje.

            String line;
            //Perdor nje loop per te lexuar linjat e derguara nga klienti, ku ndahen permes hapsirave(\\s+)
            while((line = input.readLine()) != null) {
                String[] commands = line.split("\\s+");
                //Perdorimi i switch case per kryrjen e veprimeve per secilen komande te pranuar
                switch (commands[0]) {
                    case "/lexo": //'/lexo' thirret handelReadCommand per t'i trajtuar kerkesat per lexim
                        handleReadCommand(commands, output);
                        break;
                    case "/shkruaj":// Ndersa '/shkruaj' ose '/ekzekuto' thirret handelWriteExcuteCommand per t'i trajtuar kerkesat per shkrim ose ekzekutim
                    case "/ekzekuto":
                        handleWriteOrExecuteCommand(commands, output, clientSocket);
                        break;
                    case "/dalje"://Komanda /dalje e kryen funksionin per te trajtuar kerkesat per dalje nga klienti(Thirret handleExitCommand)
                        handleExitCommand(clientSocket, output);
                        break;
                    case "/lista"://Kjo paraqet listen e klienteve(Thirret handleListCommand)
                        handleListCommand(clientSocket, output);
                        break;
                    case "/password"://Per te trajtuar kerkesat per ndryshimin e password-it(Thirret handlePasswordCommand)
                        handlePasswordCommand(commands, output, clientSocket);
                        break;
                    case "/listofile"://Kjo komand paraqet listen e dosjeve ne server(thirret handleListFilesCommand)
                        handleListFilesCommand(output);
                        break;
                    case "/msg":// '/msg' perdoret per te derguar mesazha privat duke thirrur handlePrivateMessage per dergim
                        handlePrivateMessage(commands, clientSocket);
                        break;
                    default:
                        output.println("400: Komandë e pavlefshme");//Ne momentin kur dergohet nje komande e pavlefshme, pergjigja do te jete me nje kod gabimi ("400: Komandë e pavlefshme")
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
    private static void handleWriteOrExecuteCommand(String[] commands, PrintWriter output, Socket clientSocket) {
        if (!isAuthenticated(clientSocket) || !checkLengthOfCommandArray(commands)) {
            output.println(!isAuthenticated(clientSocket) ? "I papranueshëm." : "Argumente të pavlefshme ose autentikimi i dështuar.");
            return;
        }

        String emriFile = commands[1];
        try {
            if (commands[0].equals("/shkruaj")) {
                String teksti = String.join(" ", Arrays.copyOfRange(commands, 2, commands.length));
                Files.write(Paths.get(emriFile), teksti.getBytes());
                output.println("Teksti juaj u shkrua te: " + emriFile);
            } else if (commands[0].equals("/ekzekuto")) {

                if (emriFile.isEmpty()) {
                    output.println("Emri i file-it duhet të jepet.");
                    return;
                }

                Files.createFile(Paths.get(emriFile));
                output.println("File u krijua: " + emriFile);
            } else {
                if (new File(emriFile).exists()) {
                    ProcessBuilder pb = new ProcessBuilder(emriFile);
                    pb.start();
                    output.println("File u ekzekutua: " + emriFile);
                } else {
                    output.println("File nuk ekziston.");
                }
            }
        } catch (IOException e) {
            output.println("Gabim në shkrimin/ekzekutimin e file-it.");
        }
    }

     private static void handleListCommand(Socket clientSocket, PrintWriter output) {
        StringBuilder teksti = new StringBuilder();// StringBuilder perdoret per nje tekst qe permban informata mbi klientet
        int count = 1;
         
        //Perdor nje 'for' per te shqyrtuar cdo element ne clientArray
        for (Socket klienti : clientArray) {
            String username = clientUsernames.getOrDefault(klienti, "N/A");//Per cdo perdoruse merret username(emri i perdoruesit) nga clientUsernames ose perdoret N/A nese nuk ekziston ai perdorues

            if (klienti != clientSocket && !teksti.toString().contains("Klienti " + username))//Kjo pjesen ben kontrollimin nese klienti e nuk eshte ai qe e ka bere kerkesen dhe nese klienti ekziston ne listen e klienteve
            {
                teksti.append("Klienti ").append(username).append(" - ").append(klienti.getRemoteSocketAddress()).append("\n");//Shtohen informancione per klientin ne tekst, si emri dhe adresa e larget te socket-it
                count++;//Rritja e numrit te klienteve ne list per cdo klient te ri
            }
        }

        String clientUsername = clientUsernames.getOrDefault(clientSocket, "N/A");//Per klientin aktual qe ka bere kerkesen, merr emrin e perdoruesit nga clientUsernames ose perdor "N/A" nëse nuk ekziston
        teksti.append("Klienti ").append(clientUsername).append(" (vetvetiu) - ").append(clientSocket.getRemoteSocketAddress()).append("\n");//Shtohet informacioni per klientin aktual ne tekst, duke perfshire emrin e perdoruesit dhe adresen e larget te socket-it.
        teksti.append("Numri i klientëve: ").append(count).append("\n");//Numri i klienteve

        output.println(teksti.toString());//Dergon pergjigjen, e cila eshte teksti i krijuar me lart, tek klienti aktual permes objektit output të tipit PrintWriter.
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
    private static void handleListFilesCommand(PrintWriter output) {
        try {
            File currentDirectory = new File(System.getProperty("user.dir"));
            File[] files = currentDirectory.listFiles();

            if (files != null && files.length > 0) {
                StringBuilder fileList = new StringBuilder("Files në folderin e tanishëm:\n");

                for (File file : files) {
                    fileList.append(file.getName()).append("\n");
                }

                output.println(fileList.toString());
            } else {
                output.println("Files nuk u gjetën!.");
            }
        } catch (SecurityException e) {
            output.println("Nuk ka qasje: " + e.getMessage());
        }
    }


    private static boolean isAuthenticated(Socket clientSocket) {
        return authenticatedClients.contains(clientSocket);
    }

    private static boolean checkLengthOfCommandArray(String[] commandsArray) {
        return commandsArray.length > 1;
    }

    private static Socket findSocketByUsername(String username) {
        for (Map.Entry<Socket, String> entry : clientUsernames.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(username)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
