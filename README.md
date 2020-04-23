# Overview

Amazon CloudWatch Monitoring for SAP NetWeaver ABAP based environments powered by AWS Lambda.

Please see our blog post [SAP Monitoring: A serverless approach using Amazon CloudWatch](https://amazon.awsapps.com/workdocs/index.html#/document/151c381cb0ac16d54cc0b5b5c42a0cb78558a42fd8d32515b82223bd67aecd34) for more info on the motivation and concept!

## Prerequisites

- SAP NetWeaver ABAP 7.4 or higher (ECC, S4, BW, ...), running on AWS
  - SAP Component ST-PI Release 740 SP 08 or higher (due to usage of [/SDF/SMON](https://wiki.scn.sap.com/wiki/display/CPP/All+about+SMON))
  - SAP Statistical Records enabled (TCODE stad/st03), usually by default, if not check SAP Note [2369736](https://launchpad.support.sap.com/#/notes/0002369736).
  - SAP RFC User & PW with authorizations - see "How to -> Step 1"
- SAP S-User to download SAP JCo – see “How to -> Step 2”
- Existing AWS VPC & Security Group(s), so that AWS Lambda function can be deployed and establish network connectivity towards the SAP system to be monitored (usually port 33\<INSTANCEID\>).
-	Amazon CloudWatch Detailed Monitoring enabled (1 min granularity). Optional for non-Prod, required for Prod as per SAP Note [1656250](https://launchpad.support.sap.com/#/notes/1656250).

## Setting it up

Please follow the [step-by-step guide](docs/0_How_to_setup.md).

## Architecture

**Note:** You will have to deploy a single instance of this application per SAP System ID!

![Architecture](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/assets/arch.png?raw=true)

The resulting dashboards can look as follows  

![Dashboard1](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/assets/cw_dashboard1.png?raw=true)
![Dashboard2](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/blob/master/assets/cw_dashboard2.png?raw=true)

## Further Read

- [Cost Considerations](docs/Cost_Considerations.md)  
- [Performance & Overhead Considerations](docs/Performance_Considerations.md)  
- [Known Issues](docs/Known_Issues.md)  
- [Collected Metrics](docs/Metrics.md)  
- [Troubleshooting](docs/Troubleshooting.md)  

## License

This project is licensed under the Apache-2.0 License.

This solution requires the [SAP Java Connector](https://support.sap.com/en/product/connectors.html) from SAP SE to be added manually.
  
All rights reserved.