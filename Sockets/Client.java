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
private static void menaxhoHyrjenEPerdoruesit(PrintWriter dalja) throws IOException {
        BufferedReader lexuesiPerdoruesit = new BufferedReader(new InputStreamReader(System.in));

        String hyrjaPerdoruesit;

        while ((hyrjaPerdoruesit = lexuesiPerdoruesit.readLine()) != null) {
            if (hyrjaPerdoruesit.startsWith("/msg")) {
                String[] parts = hyrjaPerdoruesit.split(" ", 3);
                if (parts.length == 3 && !parts[1].isEmpty()) {
                    dalja.println(hyrjaPerdoruesit);
                } else {
                    System.out.println("Formati i gabuar. Për të dërguar një mesazh privat, shkruani: /msg username mesazhi");
                }
            } else {
                dalja.println(hyrjaPerdoruesit);
            }

            if (hyrjaPerdoruesit.equals("/dalje")) {
                break;
            }
        }
    }
}
