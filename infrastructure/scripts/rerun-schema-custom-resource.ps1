param(
  [string]$StackName = "doomscrolling-ingestion-public-v3",
  [string]$Region = "us-west-2",
  [string]$Profile = "study",
  [string]$SchemaVersion = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($SchemaVersion)) {
  $SchemaVersion = "manual-" + (Get-Date -Format "yyyyMMdd-HHmmss")
}

Write-Host "Updating stack '$StackName' with SchemaVersionParam='$SchemaVersion'..."

aws cloudformation update-stack `
  --stack-name $StackName `
  --region $Region `
  --profile $Profile `
  --use-previous-template `
  --capabilities CAPABILITY_IAM `
  --parameters `
    ParameterKey=AllowedClientIpCidrParam,UsePreviousValue=true `
    ParameterKey=AllowedClientIpCidrParam2,UsePreviousValue=true `
    ParameterKey=DbNameParam,UsePreviousValue=true `
    ParameterKey=DbUserParam,UsePreviousValue=true `
    ParameterKey=DbPasswordParam,UsePreviousValue=true `
    ParameterKey=BackupRetentionDaysParam,UsePreviousValue=true `
    ParameterKey=EnableDeletionProtectionParam,UsePreviousValue=true `
    ParameterKey=AdminTokenParam,UsePreviousValue=true `
    ParameterKey=SchemaVersionParam,ParameterValue=$SchemaVersion `
  --no-cli-pager

if ($LASTEXITCODE -ne 0) {
  throw "CloudFormation update-stack failed."
}

Write-Host "Waiting for stack update to complete..."
aws cloudformation wait stack-update-complete --stack-name $StackName --region $Region --profile $Profile
if ($LASTEXITCODE -ne 0) {
  throw "Stack update did not complete successfully."
}

Write-Host "Schema custom resource re-run completed."