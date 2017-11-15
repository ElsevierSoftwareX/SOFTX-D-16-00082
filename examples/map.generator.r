## Loading requried packages
require(maptools)
require(rgeos)
require(classInt)
require(methods)
require(RColorBrewer)
require(rworldmap)

## Read a inputfile 
dat<-read.csv(full_filename,header=TRUE)
## draw a simple world map in background
#data(wrld_simpl)

## Index ctry.names by position in the map.
#dat$pos<-match(dat$id,wrld_simpl$ISO3)
#dat<-dat[order(dat$time, dat$pos),]

#variable for interval in a Legend
#nclr<-9
#plotclr <- brewer.pal(nclr,"YlOrRd")

# yearRange <- c(syear:eyear)
# month <- c("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
# times <- c(NULL)
# for (i in yearRange)
#   for (j in month)
#     times <- c(times, paste(i,j,sep="_"))


climateName
gcmName
rcpName


for(year in syear:eyear) {
	mapdat<-subset(dat, time==year)

	image_name = paste(filename, year, sep="_")
	full_image_name = paste(image_name, "png", sep=".")
	png(full_image_name,width=1260,height=840)

	sPDF <- joinCountryData2Map(mapdat, joinCode = "ISO3", nameJoinColumn = "id")
	mapParams <- mapCountryData( sPDF, mapTitle='', nameColumnToPlot=climateAbbr, addLegend=FALSE )
	do.call( addMapLegend, c(mapParams, legendLabels='all', 
		legendArgs=c(mtext(paste(climateName, ", ", year, "\n", "GCM: ", gcmName, ",  RCP: ", rcpName, sep=""), side=3, adj=0.5, padj=0.4, cex=2.5), 
					mtext(unit, side=1, line =0, adj=0.13, padj=-0.9, cex=2.0),
					mtext("Units: ", side=1, line=0.1, adj=0.07, padj=-0.95, cex=2.0)
					), digits=3, labelFontSize=2.0, legendWidth=2.5, legendMar = 2))
					

	
	## Classes & Palettes: Use packages RColorBrewer for cool palettes and classInt for easy sectioning of the values to be mapped:
	### Variable to be plotted
	#plotvar <- mapdat$V1
	# plotvar <- mapdat$yield
	# if(is.null(plotvar)){ 
	# 	plotvar <- mapdat$x
	# }
	# ### Palettes and intervals:
	# class <- classIntervals(plotvar, nclr, style="pretty")
	# colcode <- findColours(class, plotclr)

	### Define margins and other graphic parameters:
	# par(mar=c(1,1,1,1), lheight = .8)
	### Plot an "empty" map with borders in grey --- aspect ratio is modified to distort image a bit:
	# plot(wrld_simpl,axes=TRUE,border="darkgrey",xlim=c(-110,150),bg="lightcyan", ylim=c(-45,60),asp=1.5,main="Crop Data Tool", usePloypath=FALSE)
	### Plot the yields:
	# plot(wrld_simpl[wrld_simpl$ISO3 %in% mapdat$id,],col=colcode,axes=TRUE,add=TRUE, usePolypath=FALSE)
	### Add country codes to the map:
	# text(wrld_simpl$LON[wrld_simpl$ISO3 %in% mapdat$id],wrld_simpl$LAT[wrld_simpl$ISO3 %in% mapdat$id],wrld_simpl$ISO3[wrld_simpl$ISO3 %in% mapdat$id],cex=.4,col="black")
	### Add a legend:
	# legend(title="yields",-45, -15, legend=names(attr(colcode, "table")), fill=attr(colcode, "palette"), cex=.6, bty="y",bg="lightgrey")
	dev.off()
	print(paste("map generated: ", full_image_name))
}
print("map.genrator.r terminated")
