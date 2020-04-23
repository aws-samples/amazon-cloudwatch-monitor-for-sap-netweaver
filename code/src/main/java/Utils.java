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

public class Utils {

    public static Config config = Config.getInstance();

    JSONObject alljson = new JSONObject();

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
    }
}