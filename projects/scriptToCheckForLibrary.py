import os
import getopt, sys
import json
from subprocess import Popen, PIPE

def getProjectObjects(jsonPath):
	jsondata = json.loads(open(jsonPath).read())
	return jsondata


def performSystemGrep(path, searchString):
	#remove the "src,main,java" from here if the check has to be done for all files
	p = Popen(["grep","-ir",searchString,os.path.join(path,"src","main","java")], stdin=PIPE, stdout=PIPE, stderr=PIPE)
	output, err = p.communicate(b"input data that is passed to subprocess' stdin")
	return False if output.decode('utf-8') == '' else True

def getRelevantPaths(basePath, jsonPath, searchString):
	relevantPaths = []
	jsondata = getProjectObjects(jsonPath)
	for jsonEntry in jsondata:
		folderName = jsonEntry['folderName']
		if(performSystemGrep(os.path.join(basePath,folderName),searchString)) : 
			relevantPaths.append(jsonEntry)

	return relevantPaths

def writeToJson(relevantPaths, outputPath):
	relevantPathsJson = json.dumps(relevantPaths, indent=4)
	jsonToWriteFile = open(outputPath,'w+')
	jsonToWriteFile.write(relevantPathsJson)
	jsonToWriteFile.write('\n')


def getArguments(argv):
	argumentList = sys.argv[1:]
	options = "h:b:j:s:o:"
	long_options = ["Help", "basepath=", "jsonpath=", "searchstring=","outputpath="]
	userArguments = {}
	try:
		# Parsing argument
		arguments, values = getopt.getopt(argumentList, options, long_options)
		 
		# checking each argument
		for currentArgument, currentValue in arguments:
	 
			if currentArgument in ("-h", "--Help"):
				print('Usage : '+ str(os.path.basename(__file__)) +' -s <search string> -b <base path> -j <json path> -o <output path>')
				sys.exit(2)

			elif currentArgument in ("-b", "--basepath"):
				userArguments['basePath'] = currentValue
				 
			elif currentArgument in ("-j", "--jsonpath"):
				userArguments['jsonPath'] = currentValue

			elif currentArgument in ("-s", "--searchstring"):
				userArguments['searchString'] = currentValue

			elif currentArgument in ("-o", "--outputpath"):
				userArguments['outputPath'] = currentValue
				 
	except getopt.GetoptError as err:
		# output error, and return with an error code
		print ("Use -h for help")
		print('Usage : '+ os.path.basename(__file__) +' -s <search string> -b <base path> -j <json path> -o <output path>')
		sys.exit(2)

	if len(userArguments) < 4 :
		print('Usage : '+ str(os.path.basename(__file__)) +' -s <search string> -b <base path> -j <json path> -o <output path>')
		sys.exit(2)
	return userArguments

if __name__ == "__main__":
	userArguments = getArguments(sys.argv)
	relevantPaths = getRelevantPaths(userArguments['basePath'], userArguments['jsonPath'], userArguments['searchString'])
	writeToJson(relevantPaths,userArguments['outputPath'])

