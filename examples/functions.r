## library("ncdf4")
library(dplyr)
## library(raster)
## library(maptools)

## The function read.CMIP5.nc. It takes as an argument a file name
## (character string) that identifies a unique ntcdf file, start year,
## end year and variable id for the climate variable (tas or pre),
## longitude, and latitude.  Please look into the header of the ncdf4
## file for these variables.  The function converts the selected ncdf
## file into a table with four columns: lon, lat, time (year_month),
## and the climate value. NAs are eliminated.

read.CMIP5.nc <- function(file, start_year, end_year, climate.variable, var_lon, var_lat){
    require(ncdf4, quietly=TRUE)
    suppressMessages(require(reshape, quietly=TRUE))
    require(dplyr, quietly=TRUE)
    ncfile <- nc_open(file)
    ## Get the longitudes and latitudes --- these are later used to
    ## identify the coordinate pairs for each climate observation:
    lon <- ncvar_get(ncfile, varid=var_lon)
    lat <- ncvar_get(ncfile, varid=var_lat)
    ## Read yields (an array of 720X360X(N of years x 12)):
    var <- ncvar_get(ncfile, varid = climate.variable)
    year <- c(start_year:end_year)
    month <- c("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
    time <- c(NULL)
    for (i in year)
      for (j in month)
        time <- c(time, paste(i,j,sep="_"))
    ## Assign the longitudes and latitudes to facilitate merging with
    ## the other files:
    dimnames(var) <- list(lon,lat,time)
    ## Set non-land areas to NA before further processing:
    fillvalue <- ncatt_get(ncfile, climate.variable, "_FillValue")
    var[var == fillvalue$value] <- NA
    ## Collapse the yield array so it becomes a column:
    var.longformat <- melt(var)
    names(var.longformat) <- c("lon", "lat", "time", "value")
    var.1 <- var.longformat %>%
        mutate(year = as.character(substr(as.character(time),1,4)),
               month = as.numeric(substr(as.character(time),6,7))) %>%
                   dplyr::select(lon, lat, year, month, value) %>%
                           do(filter(., complete.cases(.)))
    ## Eliminate NAs
    ## var.longformat <- var.longformat[complete.cases(var.longformat),]
}

## The function grid.agg takes three arguments data2agg, region.map,
## weight.map, and agg.function. data2agg is the data to be aggregated
## and therefore it should always be provided; it must be a file with
## four columns labeled lon, lat, time, and value. grid.agg also
## always requires specifying region.map. The file region.map must
## have three columns which must be labeled lon,lat,id. weight.map and
## agg.function are optional and the way in which they are declared
## defined the behavior of the function. If agg.function is specified
## (e.g., min, max, sd, mean, etc.), weight.map is ignored and the
## function returns a dataframe with the selected summary statistic by
## the id speciefied in region.map (i.e., country). If agg.function is
## not specified, the function will use the weights in weight.map to
## calculate a weighted average by id.  The file weight.map must have
## three columns which must be labeled lon,lat,weight. The longitudes
## and latitudes of these files must coincide with those of the data
## to be aggregated. The function checks that the different files have
## the number of columns and labels specified above, but the user is
## responsible for checking congruence of coordinates.

grid.agg <- function(data2agg=NULL,region.map=NULL,weight.map=NULL,agg.function=NULL, climate=NULL){
    require(dplyr, quietly=TRUE)
    ## Check data to be aggregated:
    if(ncol(data2agg)>4)
        stop('Data to be aggregated must have four columns labeled lon, lat, time, and value')
    if((c("lon") %in% names(data2agg) &
        c("lat") %in% names(data2agg) &
        c("time") %in% names(data2agg) &
        c("value") %in% names(data2agg))==FALSE)
        stop('Data to be aggregated must be labeled lon, lat, time, and id')
    ## Check regional mapping file:
    if(ncol(region.map)>4)
        stop('Regional mapping must have three columns labeled lon, lat, and id')
    if((c("lon") %in% names(region.map) &
        c("lat") %in% names(region.map) &
        c("id") %in% names(region.map))==FALSE)
        stop('Regional mapping must be labeled lon, lat, and id')
    ## Merge CMIP5 yields with regional mapping:
    suppressMessages(d <- left_join(data2agg, region.map, by.x=c("lon","lat"),
                                    by.y=c("lon","lat")))
    d <- d[complete.cases(d),]
    ## Aggregate:
    if(!is.null(agg.function)){
        ## If there is a user-defined summary statistic use this:
        if(climate=="pr"){
            agg <- with(d, aggregate(value * 2592000, by=list(id=id,time=time), agg.function))
        } else {
            agg <- with(d, aggregate(value - 273.15, by=list(id=id,time=time), agg.function))
        }
    }else{
        ## If there is not a user-defined summary statistic, the
        ## default behavior is to calculate a weighted average using
        ## user-provided weigths:
        ## Check weigths file:
        if(ncol(weight.map)>4)
            stop('Weights file must have three columns labeled lon, lat, and weight')
        if((c("lon") %in% names(weight.map) &
            c("lat") %in% names(weight.map) &
            c("weight") %in% names(weight.map))==FALSE)
            stop('Weights file must be labeled lon, lat, and weight')
        ## If weights file is correct, merge with yields and is data:
        suppressMessages(d <- left_join(d,weight.map , by =c("lon","lat")))
        d <- d[complete.cases(d),]
        ## Weighted Average:

        if(climate=="pr"){
            agg <- d%>%
                group_by(id,time) %>%
                summarize(pr = weighted.mean(value, weight) * 2592000)
        } else {
            agg <- d%>%
                group_by(id,time) %>%
                summarize(tasmax = weighted.mean(value, weight) - 273.15)
        }



    }
agg
}

## Function for getting pixel-specific growing-season average of a
## fiven climate variable. The growing seasons should be contained in
## a list created by growing.season() and the climate data is the
## dataframe produced by read.CMIP5.nc().

growing.season.average <- function(Growing.season, Climate.data){
    ## Get earliest planting month for those seasons that start in the
    ## preceding year:
    earliest.planting.month <- min(
    Growing.season[[2]]$plant.month[Growing.season[[2]]$planting.year == "PREVIOUS"])
    ## Calculate the pixel-specific gorwing season average climate variable:
    Growing.season.average <- inner_join( Growing.season[[1]], Climate.data,
                                     by = c("lon" = "lon", "lat" = "lat",
                                     "month.in.season"= "month")) %>%
    group_by( lon, lat ) %>%
    mutate( harvest.year = ifelse ( planting.year == "PREVIOUS" &
            month.in.season >= earliest.planting.month,
            as.numeric(year) + 1, as.numeric(year ) ) ) %>%
    group_by( lon, lat, harvest.year ) %>%
    summarise( value = mean(value) ) %>%
    dplyr::rename( time = harvest.year )
}

## Reads Sacks mean planting and harvest date and then creates a
## dtaaset witht he growing season for each pixel in Sacks data (his
## unfilled datasets with not interpolation are going to be
## shorter). The result is a list--named Growing.season--with a
## dataframe to be used in the aggregation script, a summary of all
## the possible pixel-level growing seasons, and a summary of how many
## growing seasons are and whether the planting month is in the year
## previous to the harvest month.

## crop = "maize" is used for naming the dataset.
## unfill = TRUE/FALSE identifies whether the original dataset is
## filled or not, as discussed by Sacks.
## sacks.data.dir is the directory with the rasters plant.asc and
## harvest.asc. These are mean planting and harvest day.
## No enforcement of coincidence between the values of the crop and
## fill arguments are made against the directory sacks.data.dir.

growing.season <- function(crop, fill = FALSE, sacks.data.dir){
    require(maptools)
    require(raster)
    require(dplyr)
    ## We use the mean planting and harvesting date.
    plant <- raster(readAsciiGrid(paste(sacks.data.dir, "plant.asc", sep = "")))
    harvest <- raster(readAsciiGrid(paste(sacks.data.dir, "harvest.asc", sep = "")))
    ## Planting and harvest month:
    ## To reclassify days of the year to month use
    ##                  jan feb mar apr may jun jul aug sep oct nov dec
    days.per.month <- c(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    end.month <- cumsum(days.per.month)
    day2months <- cbind( from = c(0, end.month[-length(end.month)]), to = end.month, month = c(1:12))

    ## Reclassify days to months so it is easier to merge with climate
    ## data:
    Planting.month.raster <- reclassify(plant, day2months)
    ## summary(Planting.month.raster)
    ## plot(Planting.month.raster, col = rainbow(12))

    Harvest.month.raster <- reclassify(harvest, day2months)
    ## summary(Harvest.month.raster)
    ## plot(Harvest.month.raster, col = rainbow(12))

    ## Convert to xyz format and joint toghether:
    Planting.month.table <- as.data.frame(rasterToPoints(Planting.month.raster))

    Harvest.month.table <- as.data.frame(rasterToPoints(Harvest.month.raster))

    Growing.season <- inner_join(Planting.month.table, Harvest.month.table, by = c( "x", "y") ) %>%
        dplyr::rename( lon =x, lat =y, plant.month = layer.x, harvest.month = layer.y )

    ## Summarize how many unique growing seasons there are, as well as how
    ## many of these hava a planting year previous to the harvest year:
    Count.seasons <- Growing.season %>%
        group_by(plant.month, harvest.month) %>%
        summarise(count= sum(lat/lat)) %>%
        ## PREVIOUS: the planting season occurs in the previous year, SAME in the
        ## same year.
        mutate( planting.year = ifelse( plant.month > harvest.month, "PREVIOUS", "SAME") )
    ## Total number of gorwing seasons and whether plating year is current
    ## or previous:
    summary.count.seasons <- table( Count.seasons$planting.year )

    ## Add a column with the gorwing season months of each pixel. For
    ## instance, if a pixel goes from november to april, the column has
    ## the values 11, 12, 1, ..., 4. Also identify whether the planting
    ## month occurs the previous year ("PREVIOUS") or on the same year
    ## ("SAME"). This las identification is used to group the variables
    ## during aggregation:
    Growing.season <- apply( Growing.season, 1, function(.i){
        if( .i[3] > .i[4] ){ ## Compares planting month with harvest
                             ## month. If the planting month is larger
                             ## than the harvest month it is because it
                             ## ocurred in the previous year.
            cbind(lon = .i[1], lat = .i[2],
                  ## month.in.season specifies from which to which
                  ## month the season runs. For example, 7 to 12 is
                  ## july to December, and 11 to 3 is November-March
                  month.in.season = c( c(.i[3]:12 ), c( 1:.i[4] ) ),
                  planting.year = "PREVIOUS")
    }else{
        cbind(lon = .i[1], lat = .i[2],
              month.in.season = c( .i[3] : .i[4]),
              planting.year = "SAME")}})
    Growing.season <- as.data.frame(do.call(rbind, Growing.season), stringsAsFactors = FALSE)
    Growing.season$lon <- as.numeric(Growing.season$lon)
    Growing.season$lat <- as.numeric(Growing.season$lat)
    Growing.season$month.in.season <- as.numeric(Growing.season$month.in.season)
    Growing.season <- list(Growing.season = Growing.season,
                           Count.seasons = Count.seasons,
                           summary.count.seasons = summary.count.seasons)
    fill <- if(fill == FALSE){ "unfilled" }else{ "filled" }
    save(Growing.season,
         file = paste("../Data/",crop,"growing.season.", fill, ".RData", sep = ""))
}
