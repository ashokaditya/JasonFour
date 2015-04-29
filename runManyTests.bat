SET levels=SAD1 SAD2 MA1 MA2 MA2colors environment\masimple1 

DEL results.txt

FOR %%A IN (%levels%) DO (

echo %%A >> results.txt
java -cp classes Main -file=levels\%%A.lvl >> results.txt
echo ==================================================== >> results.txt
)
