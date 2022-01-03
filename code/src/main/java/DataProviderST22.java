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

public class DataProviderST22 {

    public int frequency = 5; //execute only every iteration/function call
    public String prefix = "ST22";
    public String rfc = "/SDF/EWA_GET_ABAP_DUMPS";
    
    public Config config = Config.getInstance();
    public Utils utils = new Utils();

    public void getData() throws JCoException
    {
        //METRIC
        Integer DUMPS = 0;
        
        //IMPORT
        HashMap<String, Object> importParameters = new HashMap<String, Object>();
        importParameters.put("BEDATUM", config.system_date);

        //EXECUTE
        JCoParameterList exportParameters = utils.execute("TABLE", importParameters, rfc, prefix, frequency);
          
        //EXPORT
        if(exportParameters != null)
        {
            final JCoTable result = exportParameters.getTable("I_SNAP_ERROR_DAY");
            for (int i = 0; i < result.getNumRows(); i++) {
                result.setRow(i);
                DUMPS += result.getInt("DUMPS");
            }
            
            System.out.println("Dumps: "+DUMPS);
            
            //COLLECT & SUBMIT
            utils.collectResultEmbedded(config.destination_name, prefix+"_DUMPS", (double) DUMPS);
            utils.submitResultsEmbedded(prefix);
        }
    }
}