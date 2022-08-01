# Collected SAP Metrics

**Note:** This section is continously updated!

**Generic (2 metrics per SID):**  
- "PING" = SAP Monitor RFC Execution Time (/SDF/SMON)
- "TOTAL_APP_SERVERS" = Number of active SAP Application Servers

**[/SDF/SMON](https://wiki.scn.sap.com/wiki/display/CPP/All+about+SMON) (14 metrics per Instance), granularity 1 min:**  
- "ACT_WPS" = Number of Active Work Processes  
- "ACT_DIA" = Number of Active Dialog Work Processes  
- "TRFC_FREE" = Number of Available Work Processes for RFCs  
- "SESSIONS" = Number of Sessions  
- "USERS" = Number of Logins  
- "UPDQ" = Update Queue Length  
- "ENQQ" = Enqueue Queue Length  
- "DIAQ" = Dialog Queue Length  
- "PRIVWPNO" = Number of Work Processes in Priv Modes  
- "DIAAVG20" = Average Load last 20s  
- "DIAAVG60" = Average Load last 60s  
- "FREE_MEM_PERC" = Free Memory in % of RAM  
- "USR_TOTAL" = CPU Utilization (User) in %  
- "SYS_TOTAL" = CPU Utilization (System) in %  

**SWNC_GET_WORKLOAD_SNAPSHOT = [st03](https://wiki.scn.sap.com/wiki/pages/viewpage.action?pageId=471174735) Last Minute Load (12 metrics per SID), granularity 5 min**  

Currently enabled for Tasktypes \<TYPE\> = DIA & RFC:

- "ST03\_\<TYPE\>\_AVG_SNAP" = Average Response Time per Step in ms (ideally < 1s)  
- "ST03\_\<TYPE\>\_CPU_TIME_PERC_SNAP" = Average CPU Time in % as part of average response time (ideally ca. 40%)  
- "ST03\_\<TYPE\>\_DB_TIME_PERC_SNAP" = Average DB Time in % as part of average response time (ideally ca. 40%)  
- "ST03\_\<TYPE\>\_AVG_DB_DIR_SNAP" = Average Database Request Time per Direct reads in ms (ideally max. 10ms)  
- "ST03\_\<TYPE\>\_AVG_DB_SEQ_AVG_SNAP" = Average Database Request Time per Sequential reads in ms (ideally max. 10ms)  
- "ST03\_\<TYPE\>\_AVG_DB_CHG_AVG_SNAP" = Average Database Request Time per Changes in ms (ideally max. 10ms)  

**ABAP DUMPS st22 (1 metric per SID), granularity 5 min:** 

- "ST22_DUMPS" = Number of dumps (today)

**ABAP JOBS sm37 (1 metric per SID), granularity 5 min:**

- "SM37_CANCELLED_JOBS" = Number of aborted / failing jobs (today)

**(Optional) IDOC MONITOR we02 (2 metrics per SID), granularity 5 min:**

- "WE02_INBOUND" = Number of aborted / failing inbound IDocs (today). 
- "WE02_OUTBOUND" = Number of aborted / failing outbound IDocs (today). 

-> we02 collector requires to copy /SDF/E2E_IDOC to **ZE2E_IDOC** and set to remote enabled! Activate collector by passing we02 = 1 through Secrets Manager!

  
**Note:** Further Metrics such as SAP HANA Data/Log Disk Space, Free Memory could be captured via the [CloudWatch Agent](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/Install-CloudWatch-Agent.html).
