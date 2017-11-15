###############################################################
## DOWNLOAD CROP CALENDARS ####################################
###############################################################

## Download Crop Calendars and Unzip plant and harvest rasters:
## File names:
source("functions.r")

files <- c("Maize.crop.calendar.tar.gz"## ,
           ## "Maize.2.crop.calendar.tar.gz",
           ## "Rice.crop.calendar.tar.gz",
           ## "Rice.2.crop.calendar.tar.gz",
           ## "Sorghum.crop.calendar.tar.gz",
           ## "Sorghum.2.crop.calendar.tar.gz",
           ## "Wheat.Winter.crop.calendar.tar.gz",
           ## "Wheat.crop.calendar.tar.gz"
           )
## ## directories <- substr(files,1,nchar(files)-7)
## ## Crop <- substr(files,1,nchar(files)-26)
## ## Crop <- tolower(Crop)
## ## Crop <- "maize"

#### Download and Unzip plant and harvest rasters:
u <- "http://www.sage.wisc.edu/download/sacks/arcgis/0.5deg/"

for(i in files){
    file <- i
    url.file <- paste(u,file,sep="")
    download.file(url.file,destfile=
                  paste("../Data/cropcalendars/",i,sep="")
                  )
}

##    Unzip plant and harvest rasters:
for(i in files){
    file <- i
    system(paste("7z x -tgzip",
                 paste("../Data/cropcalendars/",
                       file, " -o../Data/cropcalendars/ -r", sep="")))
    system(paste("7z x -ttar",
                 paste("../Data/cropcalendars/",
                       substr(file, 1, nchar(file) - 3),
                                     " plant.asc harvest.asc -o../Data/cropcalendars/ -r", sep="")))
}
file.remove(paste("../Data/cropcalendars/", substr(file, 1, nchar(file) - 3), sep = ""))
