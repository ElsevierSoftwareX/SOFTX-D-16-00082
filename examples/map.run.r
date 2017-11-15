sessionInfo()

#save arguments as variables
args <- commandArgs(TRUE)

source_file <- args[1]
syear<-args[2]
eyear<-args[3]
filename<-args[4]
climateName <- args[5]
gcmName <- args[6]
rcpName <- args[7]
climateAbbr <- args[8]

#year<-2010
#filename<-"temp"

# maek full file name : filename.csv
full_filename<-paste(filename, "csv", sep=".")

unit <- ""
if(climateAbbr == "pr"){
	unit <- "    mm / year"
}else{
	unit <- expression(~degree*C)
}


climateName <- gsub("_", " ", args[5])

# run map generation 
#source("map.generator.r")
source(source_file)
