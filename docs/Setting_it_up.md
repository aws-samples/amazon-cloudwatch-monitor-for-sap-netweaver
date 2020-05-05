# How to setup

## Step 1: Create SAP User for Monitoring (SAPGUI)

Please refer to the [step-by-step guide](Create_SAP_Monitoring_User.md) and maintained list of required authorizations.

## Step 2: Create an AWS Lambda layer for SAP JCo (console)

This activity has to be done once only, regardless of any subsequent deployments. 

Please refer to the [step-by-step guide](Create_AWS_Lambda_layer_for_SAP_Jco.md)

## Step 3: Deploy Solution (console)

For most AWS regions you can easily deploy the solution via the AWS Serverless Application Repository, by searching for sap-monitor. Make sure to tick Show apps that create custom IAM roles. 

If not available in your region, please deploy manually by running the following AWS Cloud Formation template.

![CWAlarm](../assets/sar.png)

Please carefully fill out all the necessary details, such as SAP System ID, Client, Host/IP-Address, Instance ID, RFC User/Password as well as Security Group(s) and Subnet(s) of your target VPC. Compare your inputs also with the respective EC2 settings for your SAP system. 

**Note:** You will have to deploy a single instance of this application per SAP System ID, if multiple systems shall be monitored!

![CWAlarm](../assets/sam.png)

In case of issues, please refer to the [Troubleshooting](Troubleshooting.md) guide. 

If you like to start /SDF/SMON yourself instead of automatically through the monitor, check the documentation for [manual scheduling](Schedule_SDF_SMON_manually.md).

## Step 4: Test function (console)

Open the AWS Lambda console, select sap-monitor-<SID> and choose Test (payload can be any). The expected output is shown below:

![CWAlarm](../assets/lambda.png)

In case of issues, refer to the [Troubleshooting](Troubleshooting.md) guide.

## Step 5: Enable the Scheduler (console)

Open the Amazon CloudWatch console. In the navigation pane, choose Rules. Select the rule sap-monitor-<SID> and choose Enable as Actions, so that it runs periodically: 

![CWAlarm](../assets/scheduler.png)

## Step 6: Create dashboard (console)

Open the Amazon CloudWatch console. In the navigation pane, choose Metrics. Under Custom Namespaces, you should now find your custom metrics, arranged by SID. You can select any metric and preview its output.

![CWAlarm](../assets/cw_metrics.png)

Navigate to Dashboards and press Create dashboard to setup a new custom dashboard. Choose Add widget and select the respective custom metrics from the list. Make sure to match the granularity and period. 

The resulting dashboards can look as follows

![Dashboard1](../assets/cw_dashboard1.png)
![Dashboard2](../assets/cw_dashboard2.png)

By the way, if desired, CloudWatch even allows you to embed graphs into your webpage.

## Step 7: Create alarms

You can now create alarms and receive notifications, once desired thresholds are exceeded.

Start with a simple alarm to monitor the sap-monitor itself = Lambda metric **sap-monitor-\<SID\>** -> "Errors":
 
![CWAlarm](../assets/cw_alarm.png)

If you setup a corresponding AWS SNS topic, you can then be notified via email, once the monitoring is failing. Proceed with other alarms for metrics, that you like to closely pay attention to.