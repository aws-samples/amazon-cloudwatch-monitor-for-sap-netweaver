# Cost Considerations

**Note:** This solution has to be deployed per SAP System ID (SID).

AWS Lambda functions are very (cost effective)[https://aws.amazon.com/lambda/pricing/], as you only pay for effective compute resources consumed. This solution runs on **Arm** architecture and serves metrics in less than 500ms in average at a memory consumption of just 512 MB.  As the Lambda is kept warm due to periodic execution (every minute), also [cold start tweaks](https://aws.amazon.com/de/blogs/compute/new-for-aws-lambda-predictable-start-up-times-with-provisioned-concurrency/) are **not** required.

Example:

For a single SAP System with 2 Application Servers, without considering free tier, costs are estimated to be approximately [16 USD per month per SID](https://calculator.aws/#/estimate?id=3590591b3364c8911c6755e278f3473cfd7881b5).

To limit log dat stored, make sure to also [reduce the retention period](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/Working-with-log-groups-and-streams.html#SettingLogRetention) for log group “/aws/lambda/sap-monitor-\<SID\>” to e.g. 1 week.