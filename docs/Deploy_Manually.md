# Deploy via AWS CloudFormation

- Launch an [AWS CloudShell](https://console.aws.amazon.com/cloudshell/home) instance
- Execute the following statements
```bash
wget https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/raw/master/deploy.sh
chmod +x deploy.sh
./deploy.sh
```
- Create a new AWS CloudFormation stack by selecting Amazon S3 as template source