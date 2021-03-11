# Troubleshooting

In case of metrics are not available inside CloudWatch or Lambda Runtime Errors, make sure to check the Lambda Function and CloudWatch Logs - Log Group **/aws/lambda/sap-monitor-\<SID\>**.

You can always change your credentials or connection information (e.g. host) inside AWS Secrets Manager.

To force a config reload, simply pass the string "refresh":

![Imgur](../assets/tr1.png)

![Imgur](../assets/tr2.png)

For debugging purposes, temporarily add an environment variable - Key: DEBUG Value: true

Make sure to also check your connectiviy, as per prerequisites:

> Amazon VPC security group(s) allowing inbound/outbound traffic - see also section “Architecture”:
>  - Lambda + SAP@EC2: Port **33\<instanceID\>** or [message server port](Message_Server.md), so that the Lambda function can connect via the private subnet to the SAP system to be monitored
>  - Lambda: Additionally port **443** to call AWS Secrets Manager and CloudWatch APIs. In case of a private subnet without NAT Gateway make sure to create respective [private endpoints](https://docs.aws.amazon.com/vpc/latest/userguide/vpce-interface.html)!
