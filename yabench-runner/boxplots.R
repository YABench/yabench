options(echo=TRUE) # if you want see commands in output file
args <- commandArgs(trailingOnly = TRUE)
csvNames=strsplit(args[1], ",")[[1]]
outputDir=args[2]
testname=args[3]
setwd(outputDir) 
mylist <- lapply(csvNames, read.csv, header=F, sep=',', dec='.')
#print(mylist)

# Add a column to each data frame with the row index
for (i in seq_along(mylist)) {
  mylist[[i]]$rowID <- 1:nrow(mylist[[i]])
}
#print(mylist)

# Stick all the data frames into one single data frame
allData <- do.call(rbind, mylist)
labelList <- paste('Window', sep = ' ', 1:nrow(mylist[[1]]))

#PRECISION
# Split the first column based on rowID
precList <- split(allData[,1], allData$rowID)
#print(precList)
#boxplot likes a list
#png("boxplots.png")
pdf(paste("BoxplotPrecision_",testname, ".pdf", sep=''))
boxplot(precList,ylim=c(0,1),names = labelList, ylab = "Precision", main="Precision per Window",yaxs = "i")
grid()
dev.off()

#RECALL
recList <- split(allData[,2], allData$rowID)
#print(recList)
pdf(paste("BoxplotRecall_", testname, ".pdf", sep=''))
boxplot(recList,ylim=c(0,1),names = labelList, ylab = "Recall", main="Recall per Window", yaxs = "i")
grid()
dev.off()

#DELAY
delayList <- split(allData[,8], allData$rowID)
pdf(paste("BoxplotDelay_", testname, ".pdf", sep=''))
ylim <- c(min(unlist(lapply(delayList,FUN=min))), max(unlist(lapply(delayList,FUN=max))))
boxplot(delayList,ylim=ylim, names = labelList, ylab = "Delay (ms)", main="Delay per Window")
grid()
dev.off()
