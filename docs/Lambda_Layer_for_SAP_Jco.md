# Create an AWS Lambda layer for SAP JCo

**Note:** This activity has to be done **once** only, regardless of any subsequent deployments.

Download the latest [SAP Java Connector](https://support.sap.com/en/product/connectors/jco.html) (e.g. version 3.1) to your local disk.
Make sure to select Linux for Intel, 64 bit!

![Imgur](https://i.imgur.com/bJGJHst.png)

Unzip the package:

![Imgur](https://i.imgur.com/Ai85loS.png)

Create a new folder “java” with sub-folder “lib” and add the highlighted files as follows:

![Imgur](https://i.imgur.com/0zsOG26.png)

Create a new .zip file based on this new folder structure. The .zip should look as follows inside

![Imgur](https://i.imgur.com/MrRMooz.png)

Go to the AWS console of your AWS target region. Navigate to AWS Lambda – Layers and hit “Create layer”:

![Imgur](https://i.imgur.com/PDhfqmS.png)

As Name choose “**sapjco**” (mandatory) and select your recently created .zip file. Also tick Java 8 & 11 as runtimes:

![Imgur](https://i.imgur.com/e1aSx6n.png)

Finish the wizard by pressing “Create”.