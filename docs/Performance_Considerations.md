## Performance & Overhead Considerations

We are currently using two techniques to query the SAP system regularly and collect insights

- **1 min** interval for /SDF/SMON data with a 30s granularity (scheduled daily on demand):

[According to SAP](https://wiki.scn.sap.com/wiki/display/CPP/All+about+SMON#AllaboutSMON-SMONOverheadandSpaceRequirements), CPU and memory overhead on the SAP system required by /SDF/SMON is negligible and can be safely run in production. 

- **5 min** interval for st03 “last minute load”

SAP Statistical Records (stad) and related aggregation (st03) are usually enabled by default (if not check [here](https://launchpad.support.sap.com/#/notes/0002369736)) and are the foundation of any SAP monitoring. They are especially used in SAP GoingLive Check, SAP EarlyWatch Alert and SAP Solution Manager.

By the way, st06 data as part of /SDF/SMON is also fed by the [AWS data provider for SAP](https://docs.aws.amazon.com/sap/latest/general/data-provider-install.html)