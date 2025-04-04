@echo off

REM Batch file for executing the NumeroAleatorio launcher class
REM Note to increase the JVM memory in order to execute the "e-Texto" program: 
REM If you're getting an OutOfMemory Exception, increase the value of -Xms and -Xmx.
REM -Xms and -Xmx are, respectively, the mininum and maximum memory.
REM Author: Pablo Freire Matos (pablofmatos at gmail dot com)
REM Homepage: http://libras.conquista.ifba.edu.br/
REM Version: $Revision: 2.0 Ano: 2013

REM java -Xms300m -Xmx600m -jar "e-Texto.jar"
java -jar "e-Texto.jar"