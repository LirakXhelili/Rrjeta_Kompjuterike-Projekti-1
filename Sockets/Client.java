import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Client{
  public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Shkruani emrin tuaj: ");
        String username = scanner.nextLine();
        String host = "localhost";
        int port = 2911;

        try {
            Socket soketi = new Socket(host, port);
            System.out.println("U lidh me serverin");
            BufferedReader hyrja = new BufferedReader(new InputStreamReader(soketi.getInputStream()));
            PrintWriter dalja = new PrintWriter(soketi.getOutputStream(), true);

            System.out.print("Shkruani fjalëkalimin: ");
            String password = scanner.nextLine();

            dalja.println("/password " + password + " " + username);

            Thread threadPerdoruesi = new Thread(() -> {
                try {
                    menaxhoHyrjenEPerdoruesit(dalja);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            threadPerdoruesi.start();

            String pergjigja;
            while ((pergjigja = hyrja.readLine()) != null) {
                System.out.println(pergjigja);
                if (pergjigja.equals("/dalje")) {
                    soketi.close();
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Gabim në lidhjen me serverin: " + e.getMessage());
        }
    }
}
