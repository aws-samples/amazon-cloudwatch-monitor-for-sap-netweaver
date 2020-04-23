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

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.util.Properties;

public class PropertiesDestinationDataProvider implements DestinationDataProvider {

    private Properties props;

    public PropertiesDestinationDataProvider(Properties props) {
        this.props = props;
    }
    
    @Override
    public Properties getDestinationProperties(String destinationName) {
        return this.props;
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener arg0) {
        // nothing to do
    }

    @Override
    public boolean supportsEvents() {
        return false;
    }
}