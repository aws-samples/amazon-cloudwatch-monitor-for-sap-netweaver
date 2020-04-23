# How to setup

### Step 1. SAPGUI - TCODE su01/pfcg - Create SAP User & Password

Please refer to the [list of required authorizations](https://code.amazon.com/packages/AWS-sap-monitor/blobs/mainline/--/SAPUser.md).

### Step 2. Create an AWS Lambda layer for SAP JCo

This activity has to be done once only, regardless of any subsequent deployments. 

Please refer to the [step-by-step guide](https://code.amazon.com/packages/AWS-sap-monitor/blobs/mainline/--/SAPJco.md).

### Step 3. Deploy Application through

Locate the "sap-monitor" app inside the AWS Serverless Application Repository.  
Provide the parameters matching your SAP system and VPC:

![Imgur](https://i.imgur.com/ksP9e5e.png)

**Note:** If you like to start /SDF/SMON yourself instead of automatically by the monitor, put 'ScheduleParameter' to 0 and follow these [steps](SAPSchedule.md). 

### Step 4. Test function
 
Payload can be random

![Imgur](https://i.imgur.com/6TU7Equ.png)

In case of issues, please refer to our [troubleshooting guide](https://code.amazon.com/packages/AWS-sap-monitor/blobs/mainline/--/troubleshooting.md).

### Step 5. Enable Monitor

Name: sap-monitor-\<SID\>  

![Imgur](https://i.imgur.com/W52zbTs.png)

### Step 6. Create Dashboard & Alarms

The custom metrics can be found in namespace "sap-monitor" -> "by SID"

![Imgur](https://i.imgur.com/lEdWT34.png)

Design your dashboard:

![Imgur](https://i.imgur.com/fjszdbH.jpg)