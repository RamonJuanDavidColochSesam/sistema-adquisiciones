param([Parameter(Mandatory)][string]$ReportServer,[Parameter(Mandatory)][int]$IdOrden,[int]$IdArticulo=0,[int]$IdProveedor=0,[string]$Salida='ssrs-evidencia',[datetime]$Desde=(Get-Date).AddYears(-1),[datetime]$Hasta=(Get-Date))
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
New-Item -ItemType Directory -Path $Salida -Force | Out-Null
foreach($file in Get-ChildItem -LiteralPath (Join-Path $root 'reports/ssrs') -Filter '*.rdl'){
 $uri=$ReportServer.TrimEnd('/')+'?/GuateCompras/'+$file.BaseName+'&rs:Command=Render&rs:Format=PDF'
 [xml]$rdl=Get-Content -LiteralPath $file.FullName -Raw
 $ns=New-Object Xml.XmlNamespaceManager($rdl.NameTable);$ns.AddNamespace('r',$rdl.DocumentElement.NamespaceURI)
 $params=@{orden=$IdOrden;articulo=$IdArticulo;proveedor=$IdProveedor;sucursal=0;anio=$Hasta.Year;desde=$Desde.ToString('yyyy-MM-dd');hasta=$Hasta.ToString('yyyy-MM-dd')}
 foreach($p in $rdl.SelectNodes('//r:ReportParameter',$ns)){$parameterName=$p.GetAttribute('Name');$uri+='&'+$parameterName+'='+[Uri]::EscapeDataString([string]$params[$parameterName])}
 $destination=Join-Path $Salida ($file.BaseName+'.pdf')
 Invoke-WebRequest -Uri $uri -UseDefaultCredentials -OutFile $destination -UseBasicParsing
 $bytes=[IO.File]::ReadAllBytes((Resolve-Path -LiteralPath $destination).Path)
 if($bytes.Length -lt 100 -or [Text.Encoding]::ASCII.GetString($bytes,0,5) -ne '%PDF-'){throw "No se obtuvo un PDF válido: $($file.BaseName)"}
 Write-Output "PASS SSRS PDF $($file.BaseName) $($bytes.Length) bytes"
}
