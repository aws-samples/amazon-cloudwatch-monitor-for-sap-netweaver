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

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.JCoParameterList;
import java.util.HashMap;
import java.text.SimpleDateFormat;

public class DataProviderWE02 {

    public int frequency = 5;
    public String prefix = "WE02";
    public String rfc = "ZE2E_IDOC"; // Copy of /SDF/E2E/_IDOC
    
    public Config config = Config.getInstance();
    public Utils utils = new Utils();

    public void getData() throws JCoException
    {
        if(config.we02)
        {
            //METRICS
            Integer Inbound = 0;
            Integer Outbound = 0;
            
            SimpleDateFormat de_formatter = new SimpleDateFormat("yyyyMMdd");
            String today = de_formatter.format(new java.util.Date());
            
            //PART1 (INBOUND)
            
            //IMPORT
            HashMap<String, Object> importParameters = new HashMap<String, Object>();
            String myjson = "{ \"SELOPT_PARA\":[ { \"CALL_ID\":0, \"SELECTION_PARAMETER\":[ { \"PARAM\":\"DIRECTION\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\"INBOUND\", \"HIGH\":\"\" }] }, { \"PARAM\":\"STATUS\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\"RED\", \"HIGH\":\"\" }] }, { \"PARAM\":\"CLIENT\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\"TOTAL\", \"HIGH\":\"\" }] }, { \"PARAM\":\"IGNORE_BEFORE_DATE\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\""+today+"\", \"HIGH\":\"\" }] } ] } ] }";
            importParameters.put("json", myjson);
    
            //EXECUTE
            JCoParameterList exportParameters = utils.execute("PARAMS", importParameters, rfc, prefix+"_Inbound", frequency);
               
            //EXPORT
            if(exportParameters != null)
            {
                final JCoTable result = exportParameters.getTable("RESULT");
                for (int i = 0; i < result.getNumRows(); i++) {
                    result.setRow(i);
                    Inbound += result.getStructure("RESULT").getInt("AVERAGE");
                }
            }
            
            //PART2 (OUTBOUND)
            
            
            //IMPORT
            HashMap<String, Object> importParametersT = new HashMap<String, Object>();
            String myjsonT = "{ \"SELOPT_PARA\":[ { \"CALL_ID\":0, \"SELECTION_PARAMETER\":[ { \"PARAM\":\"DIRECTION\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\"OUTBOUND\", \"HIGH\":\"\" }] }, { \"PARAM\":\"STATUS\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\"RED\", \"HIGH\":\"\" }] }, { \"PARAM\":\"CLIENT\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\"TOTAL\", \"HIGH\":\"\" }] }, { \"PARAM\":\"IGNORE_BEFORE_DATE\", \"T_RANGES\":[{ \"SIGN\":\"\", \"OPTION\":\"EQ\", \"LOW\":\""+today+"\", \"HIGH\":\"\" }] } ] } ] }";
            importParametersT.put("json", myjsonT);
    
            //EXECUTE
            JCoParameterList exportParametersT = utils.execute("PARAMS", importParametersT, rfc, prefix+"_Outbound", frequency);
               
            //EXPORT
            if(exportParametersT != null)
            {
                final JCoTable resultT = exportParametersT.getTable("RESULT");
                for (int i = 0; i < resultT.getNumRows(); i++) {
                    resultT.setRow(i);
                    Outbound += resultT.getStructure("RESULT").getInt("AVERAGE");
                }
            
                System.out.println("Inbound: "+Inbound);
                System.out.println("Outbound: "+Outbound);
                
                //COLLECT & SUBMIT
                utils.collectResultEmbedded(config.destination_name, prefix+"_INBOUND", (double) Inbound);
                utils.collectResultEmbedded(config.destination_name, prefix+"_OUTBOUND", (double) Outbound);
                utils.submitResultsEmbedded(prefix);
            }
        }
        else
        {
            System.out.println("we02 collector disabled! Enable by passing we02 = 1 through Secrets Manager!");
        }
    }
}