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
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DataProviderSMON {

    Config config;

    JCoFunction function_get;
    JCoFunction function_read;
    JCoFunction function_start;
    JCoFunction function_delete;
    JCoFunction function_list;

    Integer appServers = 0;

    //Check glossary.csv for allowed values
    String[] whitelisted = new String[]
    {
    "ACT_WPS", 
    "ACT_DIA", 
    "TRFC_FREE", 
    "SESSIONS", 
    "USERS", 
    "UPDQ", 
    "ENQQ", 
    "DIAQ", 
    "PRIVWPNO", 
    "DIAAVG20", 
    "DIAAVG60", 
    "FREE_MEM_PERC", 
    "USR_TOTAL", 
    "SYS_TOTAL"
    };

    List<String> whitelisted_list = java.util.Arrays.asList(whitelisted);

    public int frequency = 1; //execute only every iteration/function call

    Utils utils;

    public void getData() throws JCoException
    {
        System.out.println("...DataProviderSMON...");

        config = Config.getInstance();

        utils = new Utils();

        if (config.iteration % frequency == 0) { //execute every iteration/function call
            getSMONJob();
        }
    }

    /*public void reportUptime(final Boolean healthy)
    {
        if (healthy) {
            utils.collectResultEmbedded(config.destination_name, "UPTIME",100.0);
        } else {
            utils.collectResultEmbedded(config.destination_name, "UPTIME",0.0);
        }
    }*/

    public void reportResult(final String SERVER, final String NAME, final Object VALUE)
    {
        if (whitelisted_list.contains(NAME)) {
            utils.collectResultEmbedded(config.destination_name + "_" + SERVER, NAME, Double.parseDouble(VALUE.toString()));
        }
    }

    public void reportPing(final String destinationString, final long elapsedTime)
    {
        utils.collectResultEmbedded(config.destination_name, "PING", Double.parseDouble(Long.toString(elapsedTime)));
    }

    //TH_SERVER_LIST
    public void getAppServers() throws JCoException
    {
        System.out.println("Executing RFC 'TH_SERVER_LIST'...");

        final JCoDestination destination = JCoDestinationManager.getDestination(config.destination_name);

        /*if (function_list == null) { DO NO CACHE DUE TO BUG DUPLICATES */
            function_list = destination.getRepository().getFunction("TH_SERVER_LIST");
        /*} else {
            System.out.println("Use function definition from cache...");
        }*/

        if (function_list == null) {
            throw new RuntimeException("RFC 'TH_SERVER_LIST' not found! Check the SAP user authorization and required SAP release (ST-PI >= SP08)!");
        }

        try {
            function_list.execute(destination);
        } catch (final AbapException e) {
            System.out.println(e.toString());
            throw new RuntimeException("Connection lost: " + e.toString());
        }

        if(config.debug)
            System.out.println(function_list.getTableParameterList().toXML());

        JCoTable result = function_list.getTableParameterList().getTable("LIST");

        Integer monresult = result.getNumRows();

        System.out.println("TH_SERVER_LIST Result Set: " + monresult);
        System.out.println();

        Integer tmpAppServers = 0;
        for (int i = 0; i < monresult; i++) {

            result.setRow(i);

            /*String NAME = result.getString("NAME");*/
            String STATE = result.getString("STATE");

            //01 = Active
            if (STATE.equals("01")) {
                tmpAppServers++;
            }

        }

        if(appServers > 0 && tmpAppServers > 0 && appServers != tmpAppServers)
        {
            System.out.println("Number of App Servers changed - restart /SDF/SMON collector!");
            DeleteSMONJob();
            startSMONJob();
        }

        appServers = tmpAppServers;
        utils.collectResultEmbedded(config.destination_name, "TOTAL_APP_SERVERS",(double) appServers);
        utils.submitResultsEmbedded("TOTAL_APP_SERVERS");
    }

    //SDF/SMON_REORG
    public void DeleteSMONJob() throws JCoException
    {
        System.out.println("Executing RFC '/SDF/SMON_REORG'...");

        final JCoDestination destination = JCoDestinationManager.getDestination(config.destination_name);

        if (function_delete == null) {
            function_delete = destination.getRepository().getFunction("/SDF/SMON_REORG");
        } else {
            System.out.println("Use function definition from cache...");
        }

        if (function_delete == null) {
            throw new RuntimeException("RFC '/SDF/SMON_REORG' not found! Check the SAP user authorization and required SAP release (ST-PI >= SP08)!");
        }

        //START IMPORT PARAMETERS
        function_delete.getImportParameterList().setValue("GUID", config.guid);
        //END IMPORT PARAMETERS

        try {
            function_delete.execute(destination);
        } catch (final AbapException e) {
            System.out.println(e.toString());
            return;
        }
    }

    //SDF/SMON_ANALYSIS_START
    public void startSMONJob() throws JCoException
    {
        System.out.println("Executing RFC '/SDF/SMON_ANALYSIS_START'...");

        final JCoDestination destination = JCoDestinationManager.getDestination(config.destination_name);

        if (function_start == null) {
            function_start = destination.getRepository().getFunction("/SDF/SMON_ANALYSIS_START");
        } else {
            System.out.println("Use function definition from cache...");
        }

        if (function_get == null) {
            throw new RuntimeException("RFC '/SDF/SMON_ANALYSIS_START' not found! Check the SAP user authorization and required SAP release (ST-PI >= SP08)!");
        }

        java.text.Format formatter = new SimpleDateFormat("HH:mm:ss");
        String start_time = formatter.format(config.system_date);
        String end_time = "23:59:59";

        SimpleDateFormat de_formatter = new SimpleDateFormat("dd.MM.yyyy");
        System.out.println("-> DATUM: " + de_formatter.format(config.system_date));
        System.out.println("-> ST_DAT: " + de_formatter.format(config.system_date));
        System.out.println("-> ENDDAT: " + de_formatter.format(config.system_date));
        System.out.println("-> ST_TIM: " + start_time);
        System.out.println("-> ENDTIM: " + end_time);

        //START IMPORT PARAMETERS
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("DESCRIPTION", config.sdfmon_name);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("DATUM", config.system_date);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ST_DAT", config.system_date);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ENDDAT", config.system_date);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ST_TIM", start_time);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ENDTIM", end_time);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("FREQU", config.sdfmon_frequency);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("FREQU_DB", 1);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("SM50", "X");
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("SM50_CT", 1);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ST06", "X");
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ST06_CT", 2);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ST02", "X");
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("ST02_CT", 2);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("QUEUE", "X"); //SM51
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("QUEUE_CT", 1);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("USERS", "X"); //SM04
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("USERS_CT", 2);

        if(config.sdfmon_enqueue)
        {
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("SM12", "X");
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("SM12_CT", 10);
        }

        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("TRANSPARENT", "X");
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("UNAME", config.username);

        final Calendar cal = Calendar.getInstance();
        cal.setTime(config.system_date);
        cal.add(Calendar.DAY_OF_WEEK, 7);
        function_start.getImportParameterList().getStructure("ANALYSIS").setValue("KEEP_UNTIL", cal.getTime());
        //END IMPORT PARAMETERS

        try {
            function_start.execute(destination);
        } catch (final AbapException e) {
            System.out.println(e.toString());
            return;
        }

        System.out.println("RFC '/SDF/SMON_ANALYSIS_START' finished!");

        final JCoParameterList result = function_start.getExportParameterList();

        if(config.debug)
        System.out.println(result.toXML());

        java.util.Date mytime = null;
        try {
            mytime = new SimpleDateFormat("HH:mm:ss").parse("23:59:59"); 
        } catch (Exception e) {
            System.out.println(e);
        }

        config.guid = result.getString("GUID");
        config.guid_validity = Utils.copyTimeToDate(config.system_date, mytime);

        System.out.println("/SDF/SMON scheduled with guid " + config.guid + " validity " + config.guid_validity);

        if (config.iteration == 1) {

            try {
                System.out.println("Sleeping 5s until collector has started...");
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            getSMONJob();
        }
    }

    public void getSMONJob() throws JCoException
    {
        if (config.guid_validity == null || config.guid_validity.getTime() <= System.currentTimeMillis()) { //JOB EXPIRED
            System.out.println("/SDF/SMON Job expired!");
            config.guid = null;
        }

        if (config.guid == null) {
            System.out.println("Executing RFC '/SDF/SMON_GET_SMON_RUNS'...");

            final JCoDestination destination = JCoDestinationManager.getDestination(config.destination_name);

            if (function_get == null) {
                function_get = destination.getRepository().getFunction("/SDF/SMON_GET_SMON_RUNS");
            } else {
                System.out.println("Use function definition from cache...");
            }

            if (function_get == null) {
                throw new RuntimeException("RFC '/SDF/SMON_GET_SMON_RUNS' not found! Check the SAP user authorization and required SAP release (ST-PI >= SP08)!");
            }

            SimpleDateFormat de_formatter = new SimpleDateFormat("dd.MM.yyyy");
            System.out.println("-> FROM_DATE: " + de_formatter.format(config.system_date));
            System.out.println("-> TO_DATE: " + de_formatter.format(config.system_date));

            //START IMPORT PARAMETERS
            function_get.getImportParameterList().setValue("FROM_DATE", config.system_date);
            function_get.getImportParameterList().setValue("TO_DATE", config.system_date);
            //END IMPORT PARAMETERS

            try {
                function_get.execute(destination);
            } catch (final AbapException e) {
                System.out.println(e.toString());
                return;
            }

            System.out.println("RFC '/SDF/SMON_GET_SMON_RUNS' finished!");
            final JCoTable result = function_get.getTableParameterList().getTable("SMON_RUNS");

            System.out.println("Rows: " + result.getNumRows());

            if(config.debug)
            System.out.println(result.toXML());

            for (int i = 0; i < result.getNumRows(); i++) {
                result.setRow(i);

                java.util.Date mytime = result.getTime("ENDTIM");
                java.util.Date mydate = result.getDate("ENDDAT");

                //DEBUG
                if (config.debug) {
                    System.out.println("Calc Date ENDTIM " + mytime);
                    System.out.println("Calc Date ENDDAT " + mydate);
                }

                java.util.Date tmpdate = Utils.copyTimeToDate(mydate, mytime);

                String tmpguid = result.getString("GUID");
                String description = result.getString("DESCRIPTION");

                //DEBUG
                System.out.println("Found Job: " + description + " guid: " + tmpguid + " validity: " + tmpdate + "...");

                if (tmpdate.getTime() > config.system_date.getTime() 
                && description.contains(config.sdfmon_name)) { //SELECT NOT EXPIRED AND CONTAIN VALID TITLE
                    config.guid = tmpguid;
                    config.guid_validity = tmpdate;
                    System.out.println("...valid!");
                } else {
                    System.out.println("...invalid!");
                }
            }
        } else {
            System.out.println("Use guid from cache...");
        }

        if (config.guid != null) {
            System.out.println("Job selected, valid until: " + config.guid_validity);
            System.out.println("/SDF/SMON guid: " + config.guid + " job: " + config.sdfmon_name);
            System.out.println();
            getSMONJobData();
            getAppServers();
        } else {

            if (config.sdfmon_schedule) {   
                System.out.println("Start /SDF/SMON collector...");
                startSMONJob();
            } else {
                System.out.println("Scheduling /SDF/SMON automatically has been disabled! Please do manually via sm36!");
            }

            if (config.guid == null) {
                //reportUptime(false);
                throw new RuntimeException("No active /SDF/SMON job with description " + config.sdfmon_name + " found! Please make sure a valid job is scheduled in transaction /SDF/SMON!");
            } else {
                //reportUptime(true);
            }
        }
    }

    public void getSMONJobData() throws JCoException
    {
        System.out.println("Executing RFC '/SDF/SMON_ANALYSIS_READ'...");

        final JCoDestination destination = JCoDestinationManager.getDestination(config.destination_name);

        if (function_read == null) {
            function_read = destination.getRepository().getFunction("/SDF/SMON_ANALYSIS_READ");
        } else {
            System.out.println("Use function definition from cache...");
        }

        if (function_read == null) {
            throw new RuntimeException("RFC '/SDF/SMON_ANALYSIS_READ' not found! Check the SAP user authorization and required SAP release!");
        }

        //START IMPORT PARAMETERS
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        final Calendar cal = Calendar.getInstance();
        cal.setTime(config.system_date);
        String end_time = sdf.format(cal.getTime());
        cal.add(Calendar.MINUTE, -frequency);
        String start_time = sdf.format(cal.getTime());

        //MAKE SURE START TIME < END TIME
        if (Utils.startAfterEndTime(start_time,end_time)) {
            start_time = "00:00:01";
            System.out.println("Time adjusted!");
        }

        SimpleDateFormat de_formatter = new SimpleDateFormat("dd.MM.yyyy");

        System.out.println("-> GUID: " + config.guid);
        System.out.println("-> DATUM: " + de_formatter.format(config.system_date));
        System.out.println("-> START_TIME: " + start_time);
        System.out.println("-> END_TIME: " + end_time);

        function_read.getImportParameterList().setValue("GUID", config.guid);
        function_read.getImportParameterList().setValue("DATUM", config.system_date);
        function_read.getImportParameterList().setValue("START_TIME", start_time); //min "00:00:01"
        function_read.getImportParameterList().setValue("END_TIME", end_time); //max "23:59:00"
        //END IMPORT PARAMETERS

        final long startTime = System.currentTimeMillis();
        try {
            function_read.execute(destination);
            //reportUptime(true);
        } catch (final AbapException e) {
            System.out.println(e.toString());
            //reportUptime(false);
            throw new RuntimeException("Connection lost: " + e.toString());
        }

        final long stopTime = System.currentTimeMillis();
        final long elapsedTime = stopTime - startTime;
        System.out.println("PING: " + elapsedTime + " ms");

        System.out.println("RFC '/SDF/SMON_ANALYSIS_READ' finished!");
        final JCoTable result = function_read.getTableParameterList().getTable("HEADER");

        reportPing(config.destination_name, elapsedTime);

        final Integer monresult = result.getNumRows();

        System.out.println("/SDF/SMON Result Set: " + monresult);
        System.out.println();

        if(config.debug)
        System.out.println(result.toXML());

        for (int i = 0; i < monresult; i++) {

            result.setRow(i);

            final String SERVER = result.getString("SERVER");
            
            if (SERVER != "") {
                final JCoFieldIterator iter = result.getFieldIterator();
                while (iter.hasNextField()) {
                    final JCoField f = iter.nextField();

                    final String NAME = f.getName();
                    final Object VALUE = result.getValue(f.getName());

                    reportResult(SERVER, NAME, VALUE);
                }
            }
        }

        if (monresult == 0) {
            System.out.println("Clearing guid from cache!");
            config.guid = null;
            throw new RuntimeException("No measurements found! Check if job is still running and timezones match!");
        }

        utils.submitResultsEmbedded("/SDF/SMON");
    }
}