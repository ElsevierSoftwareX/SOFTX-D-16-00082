# source("functions.r")


###########################
args <- commandArgs(TRUE)
source_file <- args[1] #source_file <- "run.r"
climate_file <- args[2] #climate_file <- "sample.mm.nc"
worldid_file <- args[3] #worldid_file <- "WorldId.RData"
growing_season_file <- args[4] #growing season file <- maizegrowing.season.unfilled.Rdata
functions <- args[5] #functions <- "NULL"
weight_file <- args[6] #weight_file <- "weightmap"
startYear <- args[7]
endYear <- args[8]
climateName <- args[9] #climate <- "tas"
output_filename <- args[10]
lon <- args[11]
lat <- args[12]


previousYearPath <- args[13]
lastYearPath <- args[14]

## STEP 1: After the netcdf file is selected and downloaded via
## globus, the first step is to read the climate data from the netcdf
## file:

# nc.file <- '../Data/hadgem2-es/tas_bced_1960_1999_hadgem2-es_rcp2p6_2091-2099.mm.nc'
# Climate.data <- read.CMIP5.nc(file = nc.file,
#                                   start_year = '2091',
#                                   end_year = '2099',
#                                   climate.variable = 'tas',
#                                   var_lon= 'lon',
#                                   var_lat = 'lat')

source(source_file)

Climate.data <- read.CMIP5.nc(file = climate_file,
                              start_year = startYear,
                              end_year = endYear,
                              climate.variable = climateName,
                              var_lon= lon,
                              var_lat = lat)


if(previousYearPath != "null") {
  previousYearValues <- read.csv(file=previousYearPath, head=TRUE, sep=",")

  Climate.data <- rbind(previousYearValues, Climate.data)
}

if(lastYearPath != "null") {
  sel <- subset(Climate.data, year == endYear)
  write.csv(sel, lastYearPath, row.names=FALSE)
}

## STEP 2: Load info on growing seasons. The information is in an R
## list named Growing.season (We will provide these objects in a
## .Rdata format---these objects are created by the function
## growing.season() which is run only for preparingthe files and
## therefore does not need to be in the hub.) The first element of the
## list Growing.seasons is a data frame with four columns: lon, lat,
## month.in.season (for example, 5,6,7 is may june and july), and
## planting.year (PREVIOUSIOUS/SAME). The second object in the list
## summarizes all possible seasons and is used to get the planting
## month of those seasons that start the preceding year:

# load("../Data/maizegrowing.season.unfilled.RData")
if( growing_season_file != "null") {
  load(growing_season_file)
}

## STEP 3: Run Estimate growing.season.average() on the data
## downloaded via Globus and processed by read.CMIP5.nc() to get
## pixel-specific growing season averages:

if( growing_season_file != "null") {
  Growing.season.average <- growing.season.average(Growing.season = Growing.season,
                                                 Climate.data = Climate.data)
}
## STEP 4: the user selects region and weighting mas as wellas
## aggregating functions. Several examples follow:


region_map <- read.csv(worldid_file)

if( weight_file != "null") {
  weight_map <- read.csv(weight_file)
}

# group by lon, lat, year.
if( growing_season_file == 'null'){
  allyear <- aggregate(cbind(value)~lon+lat+year, data=Climate.data, mean, na.rm=TRUE)
  names(allyear) <- c("lon", "lat", "time", "value")
}

if( functions == "null" ) {
  if ( growing_season_file == "null") {
    test <- grid.agg(data2agg=  allyear,
                   region.map= region_map,
                   weight.map= weight_map,
                   agg.function = NULL,
                   climate = climateName)
  }
  else {
    test <- grid.agg(data2agg=  Growing.season.average,
                   region.map= region_map,
                   weight.map= weight_map,
                   agg.function = NULL,
                   climate = climateName)
  }
} else {
  if ( growing_season_file == "null") {
    test <- grid.agg(data2agg=  allyear,
                   region.map= region_map,
                   weight.map= weight_map,
                   agg.function = functions,
                   climate = climateName)
  }
  else {
    test <- grid.agg(data2agg=  Growing.season.average,
                   region.map= region_map,
                   weight.map= weight_map,
                   agg.function = functions,
                   climate = climateName)
  }
}

if(previousYearPath != "null") {
  test <- subset(test, time != as.numeric(startYear)-1)
}

test <- subset(test, time != as.numeric(endYear) + 1)

output_file <- paste(output_filename)
write.csv(test, file=output_file)



# print(paste("output saved : ", output_file))

# test1 <- grid.agg(data2agg=  Growing.season.average,
#                  region.map= read.csv("../Data/aggmaps/OnlyWorld.csv"),
#                  weight.map= read.csv("../Data/aggmaps/maize_tonnes_30min.csv"),
#                  agg.function = NULL)

# test2 <- grid.agg(data2agg=  Growing.season.average,
#                  region.map= read.csv("../Data/aggmaps/OnlyWorld.csv"),
#                  weight.map= read.csv("../Data/aggmaps/maize_hectares_30min.csv"),
#                  agg.function = NULL)

# test3 <- grid.agg(data2agg=  Growing.season.average,
#                  region.map= read.csv("../Data/aggmaps/LatId.csv"),
#                  weight.map= read.csv("../Data/aggmaps/maize_hectares_30min.csv"),
#                  agg.function = NULL)

# test4 <- grid.agg(data2agg=  Growing.season.average,
#                  region.map= read.csv("../Data/aggmaps/LatId.csv"),
#                  weight.map= read.csv("../Data/aggmaps/maize_hectares_30min.csv"),
#                  agg.function = "min")

# test5 <- grid.agg(data2agg=  Growing.season.average,
#                  region.map= read.csv("../Data/aggmaps/LatId.csv"),
#                  weight.map= read.csv("../Data/aggmaps/maize_hectares_30min.csv"),
#                  agg.function = "sd")

# test6 <- grid.agg(data2agg=  Growing.season.average,
#                  region.map= read.csv("../Data/aggmaps/Ctry18AEZId.csv"),
#                  weight.map= read.csv("../Data/aggmaps/maize_tonnes_30min.csv"),
#                  agg.function = "mean")
# test1
# test2
# test3
# test4
# test5
# head(test6)
