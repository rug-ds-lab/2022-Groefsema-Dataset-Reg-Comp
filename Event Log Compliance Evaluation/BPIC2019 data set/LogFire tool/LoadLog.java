package experiments.bpi2019;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

import java.io.File;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
returns sorted event log, converting all attribute values to strings (replacing spaces with underscores) except timestamps
which are converted to LocalDateTime values
 */
public class LoadLog {

    public static List<Map<String, Object>> load(String inputLogFileName) throws Exception{
        List<Map<String, Object>> result = new ArrayList<>();

        XesXmlParser parser = new XesXmlParser();
        XLog log = parser.parse(new File(inputLogFileName)).get(0);

        for (XTrace t : log) {
            for (XEvent e : t) {
                Map<String, Object> event = new HashMap<>();

                // get XAttribute values from event
                XAttributeMap xAttributes = e.getAttributes();

                // iterate through trace attributes, and if there are any duplicates in event attribute names, rename with "trace/...",
                // otherwise just add it to the map
                for (XAttribute traceAttribute : t.getAttributes().values()) {
                    String key = traceAttribute.getKey();
                    if (xAttributes.containsKey(key)) { // if xAttribute map already contains this attribute name
                        xAttributes.put("trace/" + key, traceAttribute);
                    } else {
                        xAttributes.put(key, traceAttribute);
                    }
                }

                for (String key: xAttributes.keySet()){
                    if (xAttributes.get(key) instanceof XAttributeTimestampImpl){
                        event.put("localdatetime", ((XAttributeTimestampImpl)xAttributes.get(key)).getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    }
                    if (xAttributes.get(key) instanceof XAttributeLiteralImpl){
                        event.put(key, xAttributes.get(key).toString().replace(" ", "_"));
                    }
                    else event.put(key, xAttributes.get(key));
                }
                result.add(event);
            }
        }
        // sort by time
        result.sort((a,b)->{
            org.deckfour.xes.model.impl.XAttributeTimestampImpl aTime = (org.deckfour.xes.model.impl.XAttributeTimestampImpl) a.get("time:timestamp");
            org.deckfour.xes.model.impl.XAttributeTimestampImpl bTime = (org.deckfour.xes.model.impl.XAttributeTimestampImpl) b.get("time:timestamp");
            return aTime.compareTo(bTime);
        });
        return result;
    }

}
