package ua.dscorp.poessence.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AdminCheck {

    public static boolean isRunningAsAdmin() {
        try {
            Process process = Runtime.getRuntime().exec("net session");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Access is denied")) {
                    return false;
                }
            }
            process.waitFor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        boolean isAdmin = isRunningAsAdmin();

        if (isAdmin) {
            System.out.println("The application is running with administrative privileges.");
        } else {
            System.out.println("The application is not running with administrative privileges.");
        }
    }
}
