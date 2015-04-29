SET levelname=environment\sachoice.lvl
java -jar server.jar -l levels\%levelname% -g 500 -c "java -cp classes Main"
pause