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

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Connection {

    static Config config = Config.getInstance();

    /*public void reportUptime(final Boolean healthy) {
        Utils utils = new Utils();

        if (healthy) {
            utils.collectResultEmbedded(config.destination_name, "UPTIME",100.0);
        } else {
            utils.collectResultEmbedded(config.destination_name, "UPTIME",0.0);
        }

        utils.submitResultsEmbedded("UPTIME");
    }*/

    
    public void connect() throws JCoException {
        if (!config.connected) {
            final ExecutorService executor = Executors.newCachedThreadPool();
            final Callable<Object> task = new Callable<Object>() {
                public Object call() throws JCoException {
                    try {
                        System.out.println("Trying to connect...");
                        System.out.println();
                        final JCoDestination destination = JCoDestinationManager.getDestination(config.destination_name);
                        config.destination = destination;
                        System.out.println("RFC connection attributes:");
                        System.out.println(destination.getAttributes());
                        System.out.println("Connection established!");
                        System.out.println();

                        config.connected = true;
                        //reportUptime(true);
                    }
                    catch (final JCoException e) {
                        //reportUptime(false);
                        System.out.println(e);
                        throw new RuntimeException("Connection could not be established! Please verify the host & instance config in AWS Secrets Manager and make sure your server can be reached (e.g. adjust Security Group). You can force a function config reset, by passing '{ \"refresh\": \"true\" } ' as test/event parameters!");
                    }

                    return config.connected;
                }
            };
            
            final Future<Object> future = executor.submit(task);
            
            try {
                final Object result = future.get(config.rfc_timeout, TimeUnit.SECONDS); 
            } catch (final TimeoutException ex) {
            
                System.out.println(ex);
                config.connected = false;
                //reportUptime(false);
                throw new RuntimeException("Connection could not be established! Please verify the host & instance config in AWS Secrets Manager and make sure your server can be reached (e.g. adjust Security Group). You can force a function config reset, by passing '{ \"refresh\": \"true\" } ' as test/event parameters!");

            } catch (final InterruptedException e) {
                // handle the interrupts
                //System.out.println(e);
                throw new RuntimeException(e);
            } catch (final ExecutionException e) {
                // handle other exceptions
                //System.out.println(e);
                throw new RuntimeException(e);
            } finally {
                future.cancel(true);
            }
        }
    }
} 