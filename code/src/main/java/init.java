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


import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.sap.conn.jco.JCoException;

public class init {

    public static Config config;
    public static Connection connection;
    public static DataProviderSMON mysmon;
    public static DataProviderST03 myst03;
    public static DataProviderST22 myst22;
    public static DataProviderSM37 mysm37;
    public static DataProviderWE02 mywe02;

    //LAUNCH VIA 'gradle run'
    public static void main(final String[] args) throws JCoException {
        handleRequest(null, null);
    }

    // LAUNCH VIA AWS LAMBDA
    public static String handleRequest(final Object input, final Context context) throws JCoException {

        config = Config.getInstance();
        
        if(input.toString().contains("debug"))
        config.debug = true;

        config.iteration++;

        if (context != null && input != null) { 

            config.logger = context.getLogger();

            if (config.debug){
                config.logger.log("Received Command: " + input + "\n");
                config.logger.log("Debugging ON");
            }

            //REINITIALIZE
            if (input.toString().contains("refresh")) {
                config.destination_name = null;
                config.guid = null;
                config.guid_validity = null;
                config.connected = false;
                config.reset = true;
                config.iteration = 1;
                config.logger.log("Config RESET");
            }

        }

        config.readSecrets();
        
        if (connection == null) {
            connection = new Connection(); 
        }
        connection.connect();

        if (config.connected) {

            config.getSystemTime();

            //SDF/SMON
            if (mysmon == null) {
                mysmon = new DataProviderSMON();
            }
            mysmon.getData();

            //ST03
            if (myst03 == null) {
                myst03 = new DataProviderST03();
            }
            myst03.getData();
            
            //ST22
            if (myst22 == null) {
                myst22 = new DataProviderST22();
            }
            myst22.getData();
            
            //SM37
            if (mysm37 == null) {
                mysm37 = new DataProviderSM37();
            }
            mysm37.getData();
            
            //WE02
            if (mywe02 == null) {
                mywe02 = new DataProviderWE02();
            }
            mywe02.getData();
        }

        return "Monitor operational - Pass '{ \"refresh\": \"true\" } ' to force a config reset! Received Command: " + input;
    }
}