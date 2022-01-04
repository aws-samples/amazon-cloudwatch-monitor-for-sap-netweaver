#!/bin/bash

set -e

echo "Specify your SAP SystemID (3 letters):"
read sapsid

echo 'Prepare ChangeSet...'

json=$(aws cloudformation describe-stacks --stack-name serverlessrepo-sap-monitor-$sapsid --query 'Stacks[*].Parameters')
json=$(sed 's/ParameterKey/Name/g' <<< $json)
json=$(sed 's/ParameterValue/Value/g' <<< $json)
json=$(sed 's/\[ \[/\[/g' <<< $json)
json=$(sed 's/\] \]/\]/g' <<< $json)
changeARN=$(aws serverlessrepo create-cloud-formation-change-set \
--application-id arn:aws:serverlessrepo:eu-central-1:529824580566:applications/sap-monitor \
--stack-name sap-monitor-$sapsid \
--capabilities CAPABILITY_RESOURCE_POLICY CAPABILITY_IAM \
--parameter-overrides "$json" \
--query "ChangeSetId")
changeARN=$(sed -e 's/^"//' -e 's/"$//' <<<"$changeARN")

echo 'Wait 30 seconds...'
sleep 30
echo 'Deploy...'
aws cloudformation execute-change-set --change-set-name $changeARN
echo 'All done! Check AWS CloudFormation console for any errors!'