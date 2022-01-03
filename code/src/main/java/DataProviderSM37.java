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
import com.sap.conn.jco.JCoContext;
import java.util.HashMap;

public class DataProviderSM37 {

    public int frequency = 5; //execute only every iteration/function call
    public String prefix = "SM37";
    public String rfc0 = "BAPI_XMI_LOGON";
    public String rfc = "BAPI_XBP_JOB_SELECT";
    
    public Config config = Config.getInstance();
    public Utils utils = new Utils();

    public void getData() throws JCoException
    {
        try
        {
            JCoContext.begin(config.destination);
            
            //PART1 (LOGIN)
            
            //METRIC
            String SessionID = "";
            
            //IMPORT
            HashMap<String, Object> importParametersT = new HashMap<String, Object>();
            importParametersT.put("EXTCOMPANY", "AWS");
            importParametersT.put("EXTPRODUCT", "AWS");
            importParametersT.put("INTERFACE", "XBP");
            importParametersT.put("VERSION", "1.0");
    
            //EXECUTE
            JCoParameterList exportParametersT = utils.execute("PARAMS", importParametersT, rfc0, prefix+"_Login", frequency);
               
            //EXPORT
            if(exportParametersT != null)
            {
                String sessionId = exportParametersT.getString("SESSIONID");
                System.out.println("XBP SESSIONID: "+sessionId);
            }
            
            //PART2 (READ)
            
            //METRIC
            Integer cancelledJobs = 0;
            
            //IMPORT
            HashMap<String, Object> importParameters = new HashMap<String, Object>();
            importParameters.put("EXTERNAL_USER_NAME", "AWS");
            HashMap<String, Object> job_param = new HashMap<String, Object>();
                job_param.put("JOBNAME", "*");
                job_param.put("USERNAME", "*");
                job_param.put("FROM_DATE", config.system_date);
                job_param.put("TO_DATE", config.system_date);
                job_param.put("FROM_TIME", "00:00:00");
                job_param.put("TO_TIME", "23:59:59");
                job_param.put("ABORTED", "X");
            importParameters.put("JOB_SELECT_PARAM", job_param);
            importParameters.put("SELECTION", "ALL");
    
            //EXECUTE
            JCoParameterList exportParameters = utils.execute("TABLE", importParameters, rfc, prefix+"_Select", frequency);
               
            //EXPORT
            if(exportParameters != null)
            {
                cancelledJobs = exportParameters.getTable("SELECTED_JOBS").getNumRows();
                
                System.out.println("Cancelled Jobs: "+cancelledJobs);
                
                //COLLECT & SUBMIT
                utils.collectResultEmbedded(config.destination_name, prefix+"_CANCELLED_JOBS", (double) cancelledJobs);
                utils.submitResultsEmbedded(prefix);
            }
        }
        catch (AbapException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (JCoException ex)
        {
            throw new RuntimeException(ex);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            JCoContext.end(config.destination);
        }
    }
}