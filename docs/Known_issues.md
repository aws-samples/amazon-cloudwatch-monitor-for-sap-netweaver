# Known issues / Out of scope

- SAP Message Server / Logon Groups currently not supported (use IP-Address of SAP Primary Application Server)
- If additional SAP application server has been stopped or started, /SDF/SMON cannot pick it up automatically in all cases. Typically works if server previously has been monitored
- Why is the "Daily Monitoring" feature inside /SDF/SMON not used?
  - Unfortunately there is a data delay of 1 Min when using "Daily Monitoring"
- Dashboard has to be created manually
- Auto Update of SAP Monitor not supported