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

}
