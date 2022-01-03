/* 
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  
  Licensed under the Apache License, Version 2.0 (the "License").
  You may not use this file except in compliance with the License.
  A copy of the License is located at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
  or in the "license" file accompanying this file. This file is distributed 
  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
  express or implied. See the License for the specific language governing 
  permissions and limitations under the License.
*/

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.JCoParameterList;
import java.util.HashMap;

public class Utils {

    public static Config config = Config.getInstance();

    JSONObject alljson = new JSONObject();
    
    public void clear()
    {
        alljson = new JSONObject();
    }

    public static java.util.Date copyTimeToDate(java.util.Date date, java.util.Date time) {
        Calendar t = Calendar.getInstance();
        t.setTime(time);
    
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, t.get(Calendar.MINUTE));
        c.set(Calendar.SECOND, t.get(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, t.get(Calendar.MILLISECOND));
        return c.getTime();
    }

    public static Boolean startAfterEndTime(String strTime, String endTime) {

        try {

            java.text.DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            java.util.Date start = dateFormat.parse(strTime);
            java.util.Date end = dateFormat.parse(endTime);

            if (config.debug) {
                System.out.println(start);
                System.out.println(end);
            }
            
            if (start.getTime() >= end.getTime()) { 
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public void collectResultEmbedded(String dimension_name, String metric_name, Double value) {

        if (!alljson.has(dimension_name)) {
            alljson.put(dimension_name, new JSONObject().put("_aws",new JSONObject()));
            alljson.put(dimension_name + "_metrics", new JSONArray());
        }

        if (alljson.has(dimension_name)) {
            JSONObject myjson = alljson.getJSONObject(dimension_name);
            JSONArray myjsonmetrics = alljson.getJSONArray(dimension_name + "_metrics");
        
            myjsonmetrics.put(new JSONObject()
                .put("Unit", "None")
                .put("Name", metric_name));

            myjson.put("by SID", dimension_name);
            
            myjson.put(metric_name, value);
        }
    }

    public void submitResultsEmbedded(String provider) {

        java.util.Iterator<String> keys = alljson.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            if (alljson.get(key) instanceof JSONObject && !key.contains("_metrics")) {     
                
                JSONObject resultjson = alljson.getJSONObject(key);

                JSONArray dimensions = new JSONArray();
                dimensions.put(new JSONArray().put("by SID"));

                JSONObject myaws = resultjson.getJSONObject("_aws");

                myaws
                .put("Timestamp", System.currentTimeMillis())
                .put("CloudWatchMetrics", new JSONArray().put(new JSONObject()
                    .put("Metrics", alljson.get(key + "_metrics"))
                    .put("Dimensions", dimensions)
                    .put("Namespace", config.namespace)));

                System.out.println(resultjson.toString());

            }
        }

        System.out.println(provider + " Data successfully written to CloudWatch Logs!");
        
        clear();
    }
    
    public JCoParameterList execute(String exportType, HashMap<String, Object> importParameters, String rfc, String prefix, Integer frequency) throws JCoException
    {
        JCoParameterList result = null;
        JCoFunction function_read = null;
        
        System.out.println("...DataProvider"+prefix+"...");

        if (config.iteration % frequency == 0 || config.iteration == 1) { //execute every iteration/function call

            System.out.println("Executing RFC '"+rfc+"'...");

            //final JCoDestination destination = JCoDestinationManager.getDestination(config.destination_name);

            if (function_read == null) { 
                function_read = config.destination.getRepository().getFunction(rfc);
            } else {
                System.out.println("Use function definition from cache...");
            }

            if (function_read == null) {
                throw new RuntimeException("RFC '"+rfc+"' not found! Check the SAP user authorization and required SAP release!");
            }

            for (HashMap.Entry<String, Object> entry : importParameters.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if(key.equals("json"))
                {
                    function_read.getImportParameterList().fromJSON(value.toString());
                    System.out.println(value);
                }
                else
                {
                    if(value instanceof HashMap)
                    {
                        HashMap<String, Object> tmpvalue = (HashMap<String, Object>) value;
                        for (HashMap.Entry<String, Object> entry1 : tmpvalue.entrySet()) {
                            String key1 = entry1.getKey();
                            Object value1 = entry1.getValue();
                            function_read.getImportParameterList().getStructure(key).setValue(key1, value1);
                            
                            if(value1 instanceof java.util.Date)
                            {
                                SimpleDateFormat de_formatter = new SimpleDateFormat("dd.MM.yyyy");
                                value1 = de_formatter.format(value1);
                            }
                            
                            System.out.println("-> "+key+"/"+key1+": "+value1);
                        }
                    }
                    else
                    {
                        function_read.getImportParameterList().setValue(key, value);
                    
                        if(value instanceof java.util.Date)
                        {
                            SimpleDateFormat de_formatter = new SimpleDateFormat("dd.MM.yyyy");
                            value = de_formatter.format(value);
                        }
                        
                        System.out.println("-> "+key+": "+value);
                    }
                }
            }
            
            if(config.debug)
            System.out.println(function_read.getImportParameterList());

            final long startTime = System.currentTimeMillis();
            try {
                function_read.execute(config.destination);
            } catch (final AbapException e) {
                System.out.println(e.toString());
                throw new RuntimeException("Connection lost: " + e.toString());
            }

            final long stopTime = System.currentTimeMillis();
            final long elapsedTime = stopTime - startTime;
            System.out.println("PING: " + elapsedTime + " ms");

            System.out.println("RFC '"+rfc+"' finished!");
            
            if(exportType.equals("TABLE"))
                result = function_read.getTableParameterList();
            else
                result = function_read.getExportParameterList();
            
            if(config.debug)
            System.out.println(result.toXML());
            
        }
        else
        {
            System.out.println("Skipping - Run only every " + frequency + "th iteration!");
        }
        
        return result;
    }
}