# Known issues

- The "Daily Monitoring" feature inside /SDF/SMON is not used, as the data is delayed by at least 1 minute. Instead, this solution automatically schedules /SDF/SMON for you. However, if desired, you may [schedule /SDF/SMON manually](Schedule_SDF_SMON_manually.md) as well.
- Dashboard has to be created [manually](Sample_Dashboard.md)
- Updates are incremental, but have to be performed [manually](https://github.com/aws-samples/amazon-cloudwatch-monitor-for-sap-netweaver/tree/master#update-to-latest-version)
- Retrieving & decrypting secrets from AWS Secrets Manager is [slow](https://forums.aws.amazon.com/thread.jspa?messageID=878578)
- Instead of SAP JCo, the solution could also be developed with [PyRFC](https://github.com/SAP/PyRFC) or [node-rfc](https://github.com/SAP/node-rfc).