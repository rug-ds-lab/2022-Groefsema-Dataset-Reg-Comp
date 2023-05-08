package experiments.bpi2019;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/*
for checking violations from logfire are the same as violations from log compliance
manual parsing of logcompliance json violations, then asserting the maps of obligations to violations are equal
 */
public class CompareViolationsJava {

    public static void compare(String fileName, Map<String, Set<String>> logfireViolations) throws Exception{

        Map<String, Set<String>> logcomplianceViolations = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String file = reader.readLine();
        String[] lines = file.split(",\"");
        for (String line: lines) {
            String key = line.split(":")[0];
            String value = line.split(":")[1];
            Set<String> items = new HashSet<>();
            logcomplianceViolations.put(key, items);
            for (String item: value.split("],")){
                item = item.replace("[","");
                item = item.replace("]","");
                item = item.replace("}","");
                item = item.replace(" ", "");
                if (item.length()>0) items.add(item);
            }
        }
        assertEquals(logcomplianceViolations.size(), logfireViolations.size());
        for (String obligation: logfireViolations.keySet()){
            String match = logcomplianceViolations.keySet().stream().filter(k->k.contains(obligation)).findFirst().get();
            Set<String> lcItems = logcomplianceViolations.get(match);
            Set<String> lfItems = logfireViolations.get(obligation);
            Set<String> lfItemsCleaned = new HashSet<>();
            for (String item: lfItems){ // for tuples, get rid of brackets
                item = item.replace("(","");
                item = item.replace(")","");
                while (item.contains(",0")) item = item.replace(",0",","); // for integer values, get rid of initial 0s
                lfItemsCleaned.add(item);
            }
            lfItems = lfItemsCleaned;
            assertEquals(lcItems.size(), lfItems.size());
            assertTrue(lcItems.containsAll(lfItems));
        }
        reader.close();
    }
}
