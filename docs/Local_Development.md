# Local Development

If you would like to build and run the project locally, please make sure to download the latest [SAP JCo](https://support.sap.com/en/product/connectors/jco.html) for your platform and place them into the **code/jars** directory.  
Also you need to install [aws sam](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html) and [gradle](https://gradle.org/).

Run the following command to trigger a build

```bash
$ sam build
```

### Test Locally

Adjust build.gradle = jvmArgs to point to SAP JCo directory

```bash
$ cd code
$ gradle run
```

or

```bash
sam local invoke CWLambdaFunction
```

**Note:** Due to the dependencies to other AWS services and required connectivity to SAP, local testing might not be feasible.  
Instead go and deploy it to your environment.

## Deploy

Make sure to remove the sapjco libs from the .aws-sam directory before deployment.  
Libs should be served from [Lambda Layer](Create_AWS_Lambda_layer_for_SAP_Jco.md)

A simple deploy script can be found below, make sure to **adjust**!

```bash
$ rm .aws-sam/build/CWLambdaFunction/lib/libsapjco3.dylib
$ rm .aws-sam/build/CWLambdaFunction/lib/Readme.txt
$ rm .aws-sam/build/CWLambdaFunction/lib/sapjco3.jar
$ rm .aws-sam/build/CWLambdaFunction/lib/sapjcomanifest.mf
$ rm .aws-sam/build/CWLambdaFunction/lib/licenses.txt
$ sam deploy --stack-name aws-sap-monitor-<SID> --parameter-overrides SAPSIDParameter=<SID> SubnetParameter=xxx SecurityGroupParameter=yyy SAPInstanceIDParameter=00 SAPHostParameter=0.0.0.0 SAPRFC0UserParameter=SAPMONITOR SAPRFC1PasswordParameter=tbd LayerVersionParameter=1
```