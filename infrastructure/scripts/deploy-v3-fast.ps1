param(
  [string]$StackName = "doomscrolling-ingestion-public-v3",
  [string]$Region = "us-west-2",
  [string]$Profile = "study",
  [string]$DbPassword,
  [string]$SchemaVersion = "v1"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
  throw "DbPassword is required. Example: .\\scripts\\deploy-v3-fast.ps1 -DbPassword 'your-password'"
}

Push-Location (Join-Path $PSScriptRoot "..")

try {
  sam build --cached --parallel --no-beta-features
  if ($LASTEXITCODE -ne 0) { throw "sam build failed." }

  sam deploy `
    --template-file .aws-sam/build/template.yaml `
    --stack-name $StackName `
    --region $Region `
    --profile $Profile `
    --resolve-s3 `
    --s3-prefix $StackName `
    --capabilities CAPABILITY_IAM `
    --no-confirm-changeset `
    --parameter-overrides `
      AllowedClientIpCidrParam="66.219.235.149/32" `
      AllowedClientIpCidrParam2="128.187.116.2/32" `
      DbNameParam="doomscrolling" `
      DbUserParam="doom_user" `
      DbPasswordParam="$DbPassword" `
      BackupRetentionDaysParam="0" `
      EnableDeletionProtectionParam="false" `
      SchemaVersionParam="$SchemaVersion"

  if ($LASTEXITCODE -ne 0) { throw "sam deploy failed." }
}
finally {
  Pop-Location
}
