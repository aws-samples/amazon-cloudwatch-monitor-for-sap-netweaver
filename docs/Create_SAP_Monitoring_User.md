# Create an SAP User for Monitoring

Launch SAPGUI and create a new technical user and role (TCODE su01/pfcg) in your default target client (e.g. 000) with the following authorizations.

You can also try and use our role export -> [ZSAPMONITOR.SAP](../assets/ZSAPMONITOR.SAP). Go to pfcg and select "Upload" from the menu "Role". Re-generate the profile and assign a user.

- TCODE
  - /SDF/SMON
  - st03
  - sm50
  - sm51
- S_RFC
  - RFC_TYPE: Function Module
  - ACTVT: Execute (16)
  - RFC_NAME:
    - /SDF/SMON_GET_SMON_RUNS 
    - /SDF/SMON_ANALYSIS_READ
    - /SDF/SMON_ANALYSIS_START
    - RFCPING
    - RFC_GET_FUNCTION_INTERFACE
    - DDIF_FIELDINFO_GET
    - SWNC_GET_WORKLOAD_SNAPSHOT
    - BDL_GET_CENTRAL_TIMESTAMP
    - RFC_METADATA_GET
    - /SDF/SMON_REORG
    - TH_SERVER_LIST
- S_ADMI_FCD
  - S_ADMI_FCD
    - PADM
    - ST0R
    - ST0M
- S_TOOLS_EX
  - AUTH
    - S_TOOLS_EX_A
- S_USER_GRP
  - CLASS: *
  - ACTVT: Display (03) 
- S_BTCH_ADM
  - BTCADMIN:
    - D
    - Y

The result should look as follows:

![Imgur](https://i.imgur.com/kY6V4BY.png)