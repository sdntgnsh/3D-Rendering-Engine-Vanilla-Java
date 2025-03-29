# Compile GUI.java with Java 8 compatibility
javac -source 1.8 -target 1.8 GUI.java

# Create a manifest file with the Main-Class attribute.
# Note: The backtick (`n) is used for a newline in PowerShell.
$manifestContent = "Main-Class: GUI`n"
Set-Content -Path manifest.txt -Value $manifestContent

# Package the compiled class into a JAR file using the manifest.
jar cvfm YourJar.jar manifest.txt GUI.class

Write-Output "JAR file created: YourJar.jar"
