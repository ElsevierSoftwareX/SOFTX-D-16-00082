source("functions.r")

#####################################################################
## PREPARE DATA FRAMES WITH GROWING SEASONS
#####################################################################

## Prepare data frames with growing seasons to be used with
## aggregations (all the needed steps are in the growingseason()
## function in functions.r:
sacks.data.dir <- "../Data/cropcalendars/Maize.crop.calendar/"

growing.season(crop = "maize", fill = FALSE,
               sacks.data.dir = "../Data/cropcalendars/Maize.crop.calendar/")


