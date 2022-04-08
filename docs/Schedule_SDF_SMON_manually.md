# Schedule /SDF/SMON manually

Although the monitor is designed to start his own /SDF/SMON runs, you can override the behaviour and create your own scheduling via SM36.  
The required RFC authoriziation for '/SDF/SMON_ANALYSIS_START' is hereby relaxed.  
Switching between those settings, can be done inside Secrets Manager 'sap-monitor-<SID>'. Simply adjust parameters
- /SDF/SMON_SCHEDULE -> 0 (false) or 1 (true = auto = default)
- (optional) /SDF/SMON_DESC -> AWSCW (default description of /SDF/SMON runs)
- /SDF/SMON_FREQUENCY -> 30 seconds (min = default), increase to 60 in case of high system load / large number of app servers
- /SDF/SMON_ENQUEUE  -> 1 (true = auto = default) or 0 (false), disable in case of high system load / large number of app servers

**Note:** You have to carefully follow the steps below. The "daily monitoring" feature inside /SDF/SMON will not work, as data collection is delayed.

## 1. In SAPGUI go to TCODE '/SDF/SMON' and create a new variant

Adjust your settings, as shown below. Choose an interval between 30 - 60 seconds.

![Imgur](https://i.imgur.com/WcPrQC8.png)

![Imgur](https://i.imgur.com/h9deu3A.jpg)

## 2. In SAPGUI go to TCODE 'SM36' and schedule a new daily job

As follows:

![Imgur](https://i.imgur.com/CFfVACc.jpg)

![Imgur](https://i.imgur.com/PRfm7o4.jpg)