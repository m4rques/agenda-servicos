param(
    [switch]$Testar,
    [switch]$SomenteCompilar
)

$ErrorActionPreference = 'Stop'
if ($Testar -and $SomenteCompilar) {
    throw 'Escolha -Testar ou -SomenteCompilar.'
}

$compilador = $null
$comando = Get-Command javac -ErrorAction SilentlyContinue
if ($comando) {
    $compilador = $comando.Source
}
if (-not $compilador -and $env:JAVA_HOME) {
    $candidato = Join-Path $env:JAVA_HOME 'bin\javac.exe'
    if (Test-Path -LiteralPath $candidato) {
        $compilador = $candidato
    }
}
if (-not $compilador) {
    $padroes = @(
        "$env:ProgramFiles\Java\*\bin\javac.exe",
        "$env:ProgramFiles\Java\*\*\bin\javac.exe",
        "$env:ProgramFiles\Eclipse Adoptium\*\bin\javac.exe",
        "$env:USERPROFILE\.jdks\*\bin\javac.exe"
    )
    foreach ($padrao in $padroes) {
        $localizado = Resolve-Path -Path $padrao -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($localizado) {
            $compilador = $localizado.Path
            break
        }
    }
}
if (-not $compilador) {
    throw 'JDK nao encontrado. Configure JAVA_HOME ou inclua a pasta bin do JDK no PATH.'
}

$destino = Join-Path $PSScriptRoot 'out'
New-Item -ItemType Directory -Path $destino -Force | Out-Null
$fontes = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src\agenda') -Filter '*.java' |
    ForEach-Object { $_.FullName })
if ($Testar) {
    $fontes += @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'testes\agenda') -Filter '*.java' |
        ForEach-Object { $_.FullName })
}

$OutputEncoding = New-Object System.Text.UTF8Encoding($false)
[Console]::InputEncoding = $OutputEncoding
[Console]::OutputEncoding = $OutputEncoding
$infoVersao = New-Object System.Diagnostics.ProcessStartInfo
$infoVersao.FileName = $compilador
$infoVersao.Arguments = '-version'
$infoVersao.UseShellExecute = $false
$infoVersao.CreateNoWindow = $true
$infoVersao.RedirectStandardOutput = $true
$infoVersao.RedirectStandardError = $true
$processoVersao = New-Object System.Diagnostics.Process
$processoVersao.StartInfo = $infoVersao
try {
    $null = $processoVersao.Start()
    $versao = ($processoVersao.StandardOutput.ReadToEnd() + $processoVersao.StandardError.ReadToEnd()).Trim()
    $processoVersao.WaitForExit()
    if ($processoVersao.ExitCode -ne 0) {
        throw 'Nao foi possivel consultar a versao do compilador.'
    }
} finally {
    $processoVersao.Dispose()
}
$argumentos = @('-encoding', 'UTF-8', '-Xlint:all,-options', '-Werror', '-d', $destino)
if ($versao -notmatch '^javac (1\.8|8)(\.|$)') {
    $argumentos = @('--release', '8') + $argumentos
}
& $compilador @argumentos @fontes
if ($LASTEXITCODE -ne 0) {
    throw 'A compilacao falhou.'
}
Write-Host "Compilacao concluida ($versao; destino Java 8)."
if ($SomenteCompilar) {
    return
}

$javaExecutavel = Join-Path (Split-Path -Parent $compilador) 'java.exe'
if ($Testar) {
    & $javaExecutavel '-Dfile.encoding=UTF-8' -cp $destino agenda.TestesAgenda
    if ($LASTEXITCODE -ne 0) {
        throw 'Os testes das estruturas e integracao falharam.'
    }
    & $javaExecutavel '-Dfile.encoding=UTF-8' -cp $destino agenda.TestesConsole
    if ($LASTEXITCODE -ne 0) {
        throw 'Os testes do console falharam.'
    }
} else {
    & $javaExecutavel '-Dfile.encoding=UTF-8' -cp $destino agenda.Main
    if ($LASTEXITCODE -ne 0) {
        throw 'A execucao terminou com erro.'
    }
}
