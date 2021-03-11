# Known issues / Out of scope

- If additional SAP application server has been stopped or started, /SDF/SMON cannot pick it up automatically in all cases. Typically works if server previously has been monitored
- Why is the "Daily Monitoring" feature inside /SDF/SMON not used?
  - Unfortunately there is a data delay of 1 Min when using "Daily Monitoring"
  - Check also guide on [how to schedule /SDF/SMON](Schedule_SDF_SMON_manually.md) manually instead
- Dashboard has to be created [manually](Sample_Dashboard.md)
- Auto Update of SAP Monitor not supported
- Retrieving & decrypting secret from AWS Secrets Manager is [slow](https://forums.aws.amazon.com/thread.jspa?messageID=878578)
- Instead of SAP JCo, the solution could also be developed with [PyRFC](https://github.com/SAP/PyRFC) or [node-rfc](https://github.com/SAP/node-rfc).