# Overview

![badge](https://codebuild.eu-central-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiTVBuUW9pbGlwNlNxVWJ4N3VhdmkyTjZJMVRqc1VvTnk0ZXNsWXNvNnFTR1pkRnlxQkFuQVpORkRqQnp2aUVYaE5PT1ZhVW83R2l5ZkljaHI4SGR1OEdvPSIsIml2UGFyYW1ldGVyU3BlYyI6InNPaUFXamZKdkNKZUFTaTYiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)

Amazon CloudWatch Monitoring for SAP NetWeaver ABAP-based environments powered by AWS Lambda.

Please see our blog post [SAP Monitoring: A serverless approach using Amazon CloudWatch](https://aws.amazon.com/blogs/awsforsap/sap-monitoring-a-serverless-approach-using-amazon-cloudwatch/) for more info on the motivation and concept!

## Prerequisites

- SAP NetWeaver ABAP 7.4 or higher (ECC, S4, BW, ...)
  - SAP Component ST-PI Release 740 SP 08 or higher.
  - SAP statistical records enabled (transaction codes stad / st03).  
  If not active by default, please check [SAP note 2369736](https://launchpad.support.sap.com/#/notes/0002369736).
  - SAP RFC user and password - see "Setting it up -> Step 1"
- SAP S-User to download SAP Java Connector – see “Setting it up -> Step 2”
- Amazon VPC security group(s) allowing inbound/outbound traffic - see also section “Architecture”:
  - Lambda + SAP@EC2: Port **33\<instanceID\>** or alternatively [message server port](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/docs/Message_Server.md), so that the Lambda function can connect via the private subnet to the SAP system to be monitored
  - Lambda: Additionally port **443** to call AWS Secrets Manager and CloudWatch APIs. In case of a private subnet without NAT Gateway make sure to create respective [private endpoints](https://docs.aws.amazon.com/vpc/latest/userguide/vpce-interface.html)!
- For Production systems, make sure to enable CloudWatch detailed monitoring according to [SAP note 1656250](https://launchpad.support.sap.com/#/notes/1656250).

## Initial setup

Please follow the [step-by-step guide](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/docs/Setting_it_up.md).

## Update to latest version

If you have deployed an older version of this solution already via **AWS Serverless Application Repository**, you can simply update the stack to the latest version as follows:

- Launch an [AWS CloudShell](https://console.aws.amazon.com/cloudshell/home) instance
- Execute the following statements - creates ChangeSet but keeps parameters **unchanged**, except password:

```bash
wget https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/raw/master/update.sh
chmod +x update.sh
./update.sh
```

## Architecture

**Note:** You will have to deploy a single instance of this application per SAP System ID! 

![Architecture](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/assets/arch.png?raw=true)

The resulting dashboards can look as follows  

![Dashboard1](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/assets/cw_dashboard1.png?raw=true)
![Dashboard2](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/assets/cw_dashboard2.png?raw=true)

## Settings

**Optional** parameters, maintained in AWS Secrets Manager: sap-monitor-\<SID\>

| Parameter | Default | Description |
|--|--|--|
|  mshost | n/a | SAP Message Server Host |
|  msport | n/a | SAP Message Server Port |
|  group | n/a | SAP Logon Group |
|  language | en | Logon Language |
|  /SDF/SMON_DESC | AWSCW | Collector Name |
|  /SDF/SMON_SCHEDULE | 1 | Schedule collector automatically |
|  /SDF/SMON_FREQUENCY | 30 | Collector frequency in seconds. Note: Increase to 60 in case of high system load / large number of app servers. 30 seconds is minimum frequency!|
|  /SDF/SMON_ENQUEUE | 1 | Collect enqueue statistics. Note: Disable in case of high system load / large number of app servers |
|  we02 | 0 | Collect IDOC Metrics. Note: Requires to copy /SDF/E2E_IDOC to **ZE2E_IDOC** and set to remote enabled |

## Further Read

- [Cost Considerations](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/docs/Cost_Considerations.md)  
- [Performance & Overhead Considerations](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/docs/Performance_Considerations.md)  
- [Known Issues](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/docs/Known_Issues.md)  
- [Collected Metrics](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/docs/Metrics.md)  
- [Troubleshooting](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/docs/Troubleshooting.md)  

## Changelog

1.07

- New Parameter /SDF/SMON_FREQUENCY -> Allows to increase collector frequency, Default = Min = 30 seconds
- New Parameter /SDF/SMON_ENQUEUE -> Allows to disable collection enqueue statistics (Default: every 10th time)

1.063

- Bug Fix we02 setting

1.062

- Added Update.sh for simplified update to the latest version
- Added Deploy.sh for deployment via AWS CloudFormation instead of AWS Serverless Application Repository

1.06

- Updated Dependencies including latest SAP JCo 3.1.5
- Added new metric: ABAP Dumps (st22)
- Added new metric: cancelled jobs (se37)
- Added new metric: failing Inbound & Outbound IDocs (we02)
- Updated role template

1.05

- Added metric TOTAL_APP_SERVERS (RFC TH_SERVER_LIST)
- Added capability to restart /SDF/SMON in case amount of active app servers changes

1.04

- Added Connectivity via SAP Message Server

## License

This project is licensed under  [![Apache 2](https://img.shields.io/badge/license-Apache%202-blue.svg)](./LICENSE)

This solution requires the [SAP Java Connector](https://support.sap.com/en/product/connectors.html) from SAP SE to be added [manually](/docs/Create_AWS_Lambda_layer_for_SAP_Jco.md).
  
All rights reserved.
