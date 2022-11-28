package org.example;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    static class Fines<K, V> extends LinkedHashMap<K, V> { }

    /**
     * This method add value of attribute surname of <parson> to name attribute
     * @param inputFileName - Path to an XML file with Persons.
     * @param outputFileName - Path to an XML file to result of parsing.
     */
    public static File concatPersonNames(String inputFileName, String outputFileName) throws IOException {
        String line;
        StringBuilder contentToParse = new StringBuilder();
        Pattern contentOfNameOrSurname = Pattern.compile("\\s*=\\s*[\"'](\\s*\\w+\\s*)[\"']");
        Pattern tegName = Pattern.compile("\\b\\s*name" + contentOfNameOrSurname, Pattern.UNICODE_CHARACTER_CLASS);
        Pattern tegSurName = Pattern.compile("\\s*surname" + contentOfNameOrSurname, Pattern.UNICODE_CHARACTER_CLASS);

        File inputFile = new File(inputFileName);
        if (!inputFile.exists() && !inputFile.isFile() && !inputFile.getPath().endsWith(".xml")) {
            throw new IllegalArgumentException("The input file name should reference on exist XML file");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {

            while ((line = reader.readLine()) != null) {
                contentToParse.append(line).append("\n");

                Matcher matcherOfTegName = tegName.matcher(contentToParse);
                Matcher matcherOfTegSurname = tegSurName.matcher(contentToParse);
                if (matcherOfTegName.find() && matcherOfTegSurname.find()) {
                    contentToParse.insert(matcherOfTegName.end()-1, " " + matcherOfTegSurname.group(1));
                    contentToParse.delete(matcherOfTegSurname.start(), matcherOfTegSurname.end());
                }
                if (line.contains("/>") || line.contains("</person>")) {
                    writer.write(String.valueOf(contentToParse));
                    contentToParse = new StringBuilder();
                }
            }
            writer.write(String.valueOf(contentToParse));
        }
        return new File(outputFileName);
    }

    /**
     * This method parses a statistic by type of offenses
     * @param inputFolderName - Path to a folder with json files with info about offenses.
     * @param outputXmlFileName - Path to an XML file to result of parsing.
     */
    public static File parsingStatisticsOfOffenses(String inputFolderName, String outputXmlFileName) throws IOException {
        File folder = new File(inputFolderName);
        File[] listOfFiles;
        Map<String, Double> statistics = new Fines<>();

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("inputFolderName is not a name of folder");
        }

        listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile() && file.getPath().endsWith(".json")) {
                updateStatistics(statistics, file);
            }
        }
        statistics = getSortedMapByValue(statistics);
        return mapStatisticToXml(statistics, outputXmlFileName);
    }

    private static Map<String, Double> getSortedMapByValue(Map<String, Double> map) {
        return map.entrySet()
                .stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry :: getKey, Map.Entry :: getValue, (oldValue, newValue) -> oldValue, Fines::new)
                );
    }

    private static File mapStatisticToXml(Map<String, Double> statistics, String outputXmlFileName) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.configure( ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.writeValue(new File(outputXmlFileName), statistics);

        return new File(outputXmlFileName);
    }

    private static void updateStatistics(Map<String, Double> statistics, File file) throws IOException {
        String currenFineType = null;
        Double currentFineAmount = null;

        try(JsonParser jParser = new JsonFactory().createParser(file)) {
            while (jParser.nextToken() != JsonToken.END_ARRAY) {
                String fieldName = jParser.getCurrentName();

                if ("type".equals(fieldName)) {
                    jParser.nextToken();
                    currenFineType = jParser.getText();
                }
                if ("fine_amount".equals(fieldName)) {
                    jParser.nextToken();
                    currentFineAmount = jParser.getDoubleValue();
                }
                if (currenFineType != null && currentFineAmount != null) {
                    updateStatistics(statistics, currenFineType, currentFineAmount);
                    currenFineType = null;
                    currentFineAmount = null;
                }
            }
        }
    }

    private static void updateStatistics(Map<String, Double> statistics, String key, Double value) {
        if (statistics.containsKey(key)) {
            statistics.replace(key, statistics.get(key) + value);
        } else {
            statistics.put(key, value);
        }
    }
}
