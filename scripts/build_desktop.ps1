param([string]$Dotnet='dotnet',[switch]$Isolated)
$ErrorActionPreference='Stop'
$projectPath=Split-Path $PSScriptRoot -Parent
$desktopPath=Join-Path $projectPath 'desktop/GuateCompras.Desktop/GuateCompras.Desktop.csproj'
$oldEnv=@{}
try{
 if($Isolated){
  $buildPath=Join-Path $projectPath 'desktop/.build-environment'
  foreach($entry in @{APPDATA='appdata';DOTNET_CLI_HOME='home';NUGET_PACKAGES='packages';NUGET_SCRATCH='scratch'}.GetEnumerator()){
   $oldEnv[$entry.Key]=[Environment]::GetEnvironmentVariable($entry.Key,'Process')
   $location=Join-Path $buildPath $entry.Value
   New-Item -ItemType Directory -Path $location -Force | Out-Null
   [Environment]::SetEnvironmentVariable($entry.Key,$location,'Process')
  }
 }
 & $Dotnet restore $desktopPath --configfile (Join-Path $projectPath 'desktop/NuGet.Config')
 if($LASTEXITCODE -ne 0){throw 'Restore falló'}
 & $Dotnet build $desktopPath -c Release --no-restore
 if($LASTEXITCODE -ne 0){throw 'Build WPF falló'}
}finally{foreach($key in $oldEnv.Keys){[Environment]::SetEnvironmentVariable($key,$oldEnv[$key],'Process')}}
