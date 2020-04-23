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

public class DataProviderST03 {

    JCoFunction function_read_swnc;

    private static DecimalFormat df = new DecimalFormat("0.00");

    public int frequency = 5; //execute only every 5th iteration/function call

    public void getData() throws JCoException
    {
        System.out.println("...DataProviderST03...");
        
        Config config = Config.getInstance();

        if (config.iteration % frequency == 0 || config.iteration == 1) { //execute only every 5th iteration/function call

            String destinationString = config.destination_name;

            System.out.println("Executing RFC 'SWNC_GET_WORKLOAD_SNAPSHOT'...");

            final JCoDestination destination = JCoDestinationManager.getDestination(destinationString);

            if (function_read_swnc == null) { 
                function_read_swnc = destination.getRepository().getFunction("SWNC_GET_WORKLOAD_SNAPSHOT");
            } else {
                System.out.println("Use function definition from cache...");
            }

            if (function_read_swnc == null) {
                throw new RuntimeException("RFC 'SWNC_GET_WORKLOAD_SNAPSHOT' not found! Check the SAP user authorization and required SAP release!");
            }

            //START IMPORT PARAMETERS
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

            SimpleDateFormat de_formatter = new SimpleDateFormat("dd.MM.YYYY");
            System.out.println("-> READ_START_DATE: " + de_formatter.format(date));
            System.out.println("-> READ_END_DATE: " + de_formatter.format(date));
            System.out.println("-> READ_START_TIME: " + start_time);
            System.out.println("-> READ_END_TIME: " + end_time);

            function_read_swnc.getImportParameterList().setValue("READ_START_DATE", date);
            function_read_swnc.getImportParameterList().setValue("READ_END_DATE", date);
            function_read_swnc.getImportParameterList().setValue("READ_START_TIME", start_time);
            function_read_swnc.getImportParameterList().setValue("READ_END_TIME", end_time);
            //END IMPORT PARAMETERS

            final long startTime = System.currentTimeMillis();
            try {
                function_read_swnc.execute(destination);
            } catch (final AbapException e) {
                System.out.println(e.toString());
                throw new RuntimeException("Connection lost: " + e.toString());
            }

            final long stopTime = System.currentTimeMillis();
            final long elapsedTime = stopTime - startTime;
            System.out.println("PING: " + elapsedTime + " ms");

            System.out.println("RFC 'SWNC_GET_WORKLOAD_SNAPSHOT' finished!");

            if(config.debug)
            System.out.println(function_read_swnc.getExportParameterList().toXML());

            final JCoTable result = function_read_swnc.getExportParameterList().getTable("TASKTIMES");

            final Integer monresult = result.getNumRows();

            System.out.println("SWNC_GET_WORKLOAD_SNAPSHOT Result Set: " + monresult);
            System.out.println();

            if(config.debug)
            System.out.println(result.toXML());

            Utils utils = new Utils();

            for (int i = 0; i < monresult; i++) {

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

                    utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_AVG_SNAP",(double) AVG_RESPONSE_TIME);
                    utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_CPU_TIME_PERC_SNAP",(double) TOTAL_CPU_TIME_PC);
                    utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_DB_TIME_PERC_SNAP",(double) TOTAL_DB_TIME_PC);
                    //utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_R_TIME_PERC_SNAP",(double) TOTAL_REST_PC);
                    utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_AVG_DB_DIR_SNAP",(double) AVG_DB_TIME_DIR);
                    utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_AVG_DB_SEQ_AVG_SNAP",(double) AVG_DB_TIME_SEQ);
                    utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_AVG_DB_CHG_AVG_SNAP",(double) AVG_DB_TIME_CHG);
                    //utils.collectResultEmbedded(destinationString, "ST03_" + TASKTYPE_LABEL + "_AVGDB_SNAP", (double) AVG_DB_TIME);
                }
            }

            if (monresult == 0) {
                throw new RuntimeException("No measurements found! Check if stad/st03 collectors are running and timezones match!");
            } else {
                utils.submitResultsEmbedded("ST03");
            }
        }
        else
        {
            System.out.println("Skipping - Run only every " + frequency + " th iteration!");
        }
    }
}