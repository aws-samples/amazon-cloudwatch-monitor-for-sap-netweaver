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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.sap.conn.jco.JCoParameterList;
import java.util.HashMap;

public class DataProviderST03 {

    private static DecimalFormat df = new DecimalFormat("0.00");

    public int frequency = 5; //execute only every 5th iteration/function call
    public String prefix = "ST03";
    public String rfc = "SWNC_GET_WORKLOAD_SNAPSHOT";
    
    public Config config = Config.getInstance();
    public Utils utils = new Utils();

    public void getData() throws JCoException
    {
        //IMPORT
        final java.util.Date date = config.system_date;
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String end_time = sdf.format(cal.getTime());
        cal.add(Calendar.MINUTE, -frequency); //Run only every 5th minute => Time Offset 5 min
        String start_time = sdf.format(cal.getTime());

        //MAKE SURE START TIME < END TIME
        if (Utils.startAfterEndTime(start_time,end_time)) {
            start_time = "00:00:01";
            System.out.println("Time adjusted!");
        }

        HashMap<String, Object> importParameters = new HashMap<String, Object>();
        importParameters.put("READ_START_DATE", date);
        importParameters.put("READ_END_DATE", date);
        importParameters.put("READ_START_TIME", start_time);
        importParameters.put("READ_END_TIME", end_time);

        //EXECUTE
        JCoParameterList exportParameters = utils.execute("PARAMS", importParameters, rfc, prefix, frequency);
          
        //EXPORT
        if(exportParameters != null)
        {
            final JCoTable result = exportParameters.getTable("TASKTIMES");
            for (int i = 0; i < result.getNumRows(); i++) {
                result.setRow(i);

                String TASKTYPE = result.getString("TASKTYPE");
                String TASKTYPE_LABEL = "";

                //RFC => FE & DIA => 01
                if (TASKTYPE.equals("FE") || TASKTYPE.equals("01")) {

                    if(TASKTYPE.equals("FE"))
                    TASKTYPE_LABEL = "RFC";
                    if(TASKTYPE.equals("01"))
                    TASKTYPE_LABEL = "DIA";

                    Double STEPS = Double.parseDouble(result.getString("COUNT"));
                    Double TOTAL_RESPONSE_TIME = Double.parseDouble(result.getString("RESPTI"));
                    Double TOTAL_CPU_TIME = Double.parseDouble(result.getString("CPUTI"));
                    Double TOTAL_DB_TIME_READ_SEQ = Double.parseDouble(result.getString("READSEQTI")); //TOTAL TIME SEQUENTIAL READS
                    Double TOTAL_DB_TIME_READ_CHG = Double.parseDouble(result.getString("CHNGTI")); //TOTAL TIME CHANGES
                    Double TOTAL_DB_TIME_READ_DIR = Double.parseDouble(result.getString("READDIRTI")); //TOTAL TIME DIRECT READS

                    Double NUMBER_DB_TIME_READ_SEQ = Double.parseDouble(result.getString("READSEQCNT")); //COUNT SEQUENTIAL READS
                    Double NUMBER_DB_TIME_READ_CHG = Double.parseDouble(result.getString("CHNGCNT")); //COUNT TIME CHANGES
                    Double NUMBER_DB_TIME_READ_DIR = Double.parseDouble(result.getString("READDIRCNT")); //COUNT TIME DIRECT READS

                    long TOTAL_DB_TIME = Math.round(TOTAL_DB_TIME_READ_SEQ +  TOTAL_DB_TIME_READ_CHG + TOTAL_DB_TIME_READ_DIR);

                    //As per
                    //SAP recommendation
                    //https://wiki.scn.sap.com/wiki/pages/viewpage.action?pageId=471174735

                    //Average response time per STEP - approx. 1 second (dialog), <1 second (update)
                    long AVG_RESPONSE_TIME = Math.round(TOTAL_RESPONSE_TIME / STEPS);

                    //Average db time per STEP
                    //long AVG_DB_TIME = Math.round(TOTAL_DB_TIME/ STEPS);

                    //CPU TIME % - ideally 40%
                    long TOTAL_CPU_TIME_PC = Math.round(100 * TOTAL_CPU_TIME / TOTAL_RESPONSE_TIME);

                    //DB TIME % - ideally 40%
                    long TOTAL_DB_TIME_PC = Math.round(100 * TOTAL_DB_TIME / TOTAL_RESPONSE_TIME);

                    //REST (Wait, load, ...) 20%
                    //long TOTAL_REST_PC = 100 - TOTAL_DB_TIME_PC - TOTAL_CPU_TIME_PC;

                    //DB Time per Direct Read <10ms
                    double AVG_DB_TIME_DIR = 0.0;
                    if (NUMBER_DB_TIME_READ_DIR != 0) {
                        AVG_DB_TIME_DIR = Double.parseDouble(df.format(TOTAL_DB_TIME_READ_DIR / NUMBER_DB_TIME_READ_DIR));
                    }

                    //DB Time per Seq Read <40ms
                    double AVG_DB_TIME_SEQ = 0.0;
                    if (NUMBER_DB_TIME_READ_SEQ != 0) {
                        AVG_DB_TIME_SEQ = Double.parseDouble(df.format(TOTAL_DB_TIME_READ_SEQ / NUMBER_DB_TIME_READ_SEQ));
                    }

                    //DB Time per Changes <25ms
                    double AVG_DB_TIME_CHG = 0.0;
                    if( NUMBER_DB_TIME_READ_CHG != 0) {
                        AVG_DB_TIME_CHG = Double.parseDouble(df.format(TOTAL_DB_TIME_READ_CHG / NUMBER_DB_TIME_READ_CHG));
                    }

                    utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_AVG_SNAP",(double) AVG_RESPONSE_TIME);
                    utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_CPU_TIME_PERC_SNAP",(double) TOTAL_CPU_TIME_PC);
                    utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_DB_TIME_PERC_SNAP",(double) TOTAL_DB_TIME_PC);
                    //utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_R_TIME_PERC_SNAP",(double) TOTAL_REST_PC);
                    utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_AVG_DB_DIR_SNAP",(double) AVG_DB_TIME_DIR);
                    utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_AVG_DB_SEQ_AVG_SNAP",(double) AVG_DB_TIME_SEQ);
                    utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_AVG_DB_CHG_AVG_SNAP",(double) AVG_DB_TIME_CHG);
                    //utils.collectResultEmbedded(config.destination_name, "ST03_" + TASKTYPE_LABEL + "_AVGDB_SNAP", (double) AVG_DB_TIME);
                }
            }
            
            //SUBMIT
            if(result.getNumRows() > 0)
            utils.submitResultsEmbedded(prefix);
        }
    }
}