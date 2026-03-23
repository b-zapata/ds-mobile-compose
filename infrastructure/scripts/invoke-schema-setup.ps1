param(
  [string]$StackName = "doomscrolling-ingestion-public-v3",
  [string]$Region = "us-west-2",
  [string]$Profile = "study",
  [ValidateSet("Create", "Update", "Delete")]
  [string]$RequestType = "Update"
)

$ErrorActionPreference = "Stop"

$functionName = aws cloudformation describe-stack-resource `
  --stack-name $StackName `
  --logical-resource-id SchemaSetupFunction `
  --region $Region `
  --profile $Profile `
  --query "StackResourceDetail.PhysicalResourceId" `
  --output text `
  --no-cli-pager

if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($functionName)) {
  throw "Could not resolve SchemaSetupFunction physical name from stack '$StackName'."
}

Write-Host "Invoking $functionName with RequestType=$RequestType"

$payload = @{ RequestType = $RequestType } | ConvertTo-Json -Compress
$outFile = Join-Path $PSScriptRoot "..\invoke-schema-setup-output.json"

aws lambda invoke `
  --function-name $functionName `
  --payload $payload `
  --region $Region `
  --profile $Profile `
  --cli-binary-format raw-in-base64-out `
  --no-cli-pager `
  $outFile

if ($LASTEXITCODE -ne 0) {
  throw "Lambda invoke failed."
}

Write-Host "Lambda response written to $outFile"
Get-Content $outFile