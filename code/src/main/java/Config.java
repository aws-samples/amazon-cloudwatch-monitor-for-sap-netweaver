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

//import software.amazon.awssdk.lambda.runtime.LambdaLogger;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
//import software.amazon.awssdk.simplesystemsmanagement.AWSSimpleSystemsManagement;
//import software.amazon.awssdk.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
//import software.amazon.awssdk.simplesystemsmanagement.model.GetParameterRequest;
//import software.amazon.awssdk.simplesystemsmanagement.model.GetParameterResult;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.util.Properties;
import org.json.JSONObject;

public final class Config {
 
    private static Config INSTANCE;

    //READ FROM ENV VAR
    public String secret_key = "sap-monitor-tmp";
    
    //READ FROM PARAMATER STORE
    public String destination_name; //Used as id
    
    public String sdfmon_name = "AWSCW"; //Daily Monitoring"
    public Boolean sdfmon_schedule = true;
    public Integer sdfmon_frequency = 30; //seconds
    public Boolean sdfmon_enqueue = true;

    public String namespace = "sap-monitor";
    public Integer rfc_timeout = 10; //seconds
    public String username = "";

    public String guid = null;
    public java.util.Date guid_validity = null;

    public Integer iteration = 0;

    public Boolean connected = false;
    public Boolean reset = false;
    public Boolean debug = false;
    
    public Boolean we02 = false;

    public PropertiesDestinationDataProvider pddp_old;

    //public LambdaLogger logger;
    
    public JCoDestination destination;

    JCoFunction function_config;

    java.util.Date system_date;
     
    private Config() {        
    }
     
    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }
         
        return INSTANCE;
    }

    public void getSystemTime() throws JCoException {

        final long startTime = System.currentTimeMillis();

        System.out.println("Executing RFC 'BDL_GET_CENTRAL_TIMESTAMP'...");

        JCoDestination destination = JCoDestinationManager.getDestination(this.destination_name);
        JCoFunction function_config = destination.getRepository().getFunction("BDL_GET_CENTRAL_TIMESTAMP");

        if(function_config == null) {
            throw new RuntimeException("RFC 'BDL_GET_CENTRAL_TIMESTAMP' not found! Check the SAP user authorization and required SAP release!");
        }

        try
        {
            function_config.execute(destination);
        }
        catch (AbapException e)
        {
            System.out.println(e.toString());
            throw new RuntimeException("Connection lost: " + e.toString());
        }

        System.out.println("RFC 'BDL_GET_CENTRAL_TIMESTAMP' finished!");

        java.util.Date system_day = function_config.getExportParameterList().getDate("TAG");
        java.util.Date system_time = function_config.getExportParameterList().getTime("UHRZEIT");

        system_date = Utils.copyTimeToDate(system_day, system_time);

        System.out.println("SAP System Time: " + system_date);

        final long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("PING: " + elapsedTime + " ms");
    }

    public void readSecrets()
    {
        if (System.getenv("SECRET") != null) {
            secret_key = System.getenv("SECRET");
        } else {
            throw new RuntimeException("Environment Variable 'SECRET' not maintained. Exit!");
        }

        readSecretsFromSecretManager();
    }

    public void readSecretsFromSecretManager()
    {
        if (destination_name == null) {
            System.out.println("Downloading config from AWS Secrets Manager: " + secret_key);

            final SecretsManagerClient client  = SecretsManagerClient.builder().build();
            
            String secret = null;
            
            //final software.amazon.awssdk.secretsmanager.model.GetSecretValueRequest getSecretValueRequest = 
            //new software.amazon.awssdk.secretsmanager.model.GetSecretValueRequest().withSecretId(secret_key);
            //software.amazon.awssdk.secretsmanager.model.GetSecretValueResult getSecretValueResult = null;

            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secret_key)
                    .build();

            try {
                GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);

                if (valueResponse.secretString() != null) {
                    secret = valueResponse.secretString();

                    if(debug)
                    System.out.println(secret);
                }

                final org.json.JSONObject obj = new JSONObject(secret);

                System.out.println();
                System.out.println("Found destination " + obj.getString("name"));
                System.out.println();

                final Properties connectProperties = new Properties();
                destination_name = obj.getString("name");
                connectProperties.setProperty(DestinationDataProvider.JCO_DEST, obj.getString("name"));
                connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, obj.getString("client"));
                connectProperties.setProperty(DestinationDataProvider.JCO_USER,   obj.getString("user"));
                connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, obj.getString("password"));
                connectProperties.setProperty(DestinationDataProvider.JCO_LANG,   obj.getString("language"));

                if(obj.has("mshost") && obj.has("msport") && obj.has("group"))
                { 
                    System.out.println("Connect via SAP Message Server!");
                    
                    connectProperties.setProperty(DestinationDataProvider.JCO_MSHOST, obj.getString("mshost"));
                    connectProperties.setProperty(DestinationDataProvider.JCO_MSSERV, obj.getString("msport"));
                    connectProperties.setProperty(DestinationDataProvider.JCO_GROUP, obj.getString("group"));
                }
                else
                {
                    connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, obj.getString("host"));
                    connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR,  obj.getString("sys_id"));
                }

                username = obj.getString("user");
                
                sdfmon_name = obj.getString("/SDF/SMON_DESC");
                
                if(obj.has("/SDF/SMON_SCHEDULE"))
                {
                    Integer sdfmon_schedule_tmp = obj.getInt("/SDF/SMON_SCHEDULE");
                    if (sdfmon_schedule_tmp == 0) {
                        sdfmon_schedule = false; 
                    } else {
                        sdfmon_schedule = true;
                    }
                }

                if(obj.has("/SDF/SMON_FREQUENCY"))
                {
                    Integer sdfmon_frequency_tmp = obj.getInt("/SDF/SMON_FREQUENCY");
                    if (sdfmon_frequency_tmp > 30) {
                        sdfmon_frequency = sdfmon_frequency_tmp;
                    }
                }

                if(obj.has("/SDF/SMON_ENQUEUE"))
                {
                    Integer sdfmon_enqueue_tmp = obj.getInt("/SDF/SMON_ENQUEUE");
                    if (sdfmon_enqueue_tmp == 0) {
                        sdfmon_enqueue = false; 
                    } else {
                        sdfmon_enqueue = true;
                    }
                }
                
                if(obj.has("we02"))
                {
                    Integer we02_tmp = obj.getInt("we02");
                    if (we02_tmp == 1)
                    we02 = true;
                }
                
                final PropertiesDestinationDataProvider pddp = new PropertiesDestinationDataProvider(connectProperties);

                if (reset && com.sap.conn.jco.ext.Environment.isDestinationDataProviderRegistered() && pddp_old != null) {
                    com.sap.conn.jco.ext.Environment.unregisterDestinationDataProvider(pddp_old);
                    reset = false;
                }

                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(pddp);
                pddp_old = pddp;

            } catch (final Exception e) {
                throw new RuntimeException("Configuration '" + secret_key + "' not found or invalid! Please check AWS Secret Manager! "+e);
            }
        }
    }
}