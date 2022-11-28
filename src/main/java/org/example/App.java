package org.example;

import java.io.*;

public class App {
    public static void main( String[] args ) throws IOException {
        String persons = "persons.xml";
        String personWithFullName = "personsWithFullName.xml";
        String finesFolder = "fines";
        String statisticsOfFines = "fines\\statistics\\statistics.xml";

        printFile(new File(persons));
        printFile(Utils.concatPersonNames(persons, personWithFullName));

        printFile(new File(finesFolder + "\\fine2002.json"));
        printFile(new File(finesFolder + "\\fine2003.json"));
        printFile(new File(finesFolder + "\\fine2004.json"));
        printFile(new File(finesFolder + "\\fine2005.json"));
        printFile(Utils.parsingStatisticsOfOffenses(finesFolder, statisticsOfFines));
    }

    public static void printFile(File file) throws IOException {
        BufferedReader fin = new BufferedReader(new FileReader(file));
        String line;
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("Printed File: " + file.getName());
        System.out.println("----------------------------------------------------------------------------------------");
        while ((line = fin.readLine()) != null) System.out.println(line);
    }
}
