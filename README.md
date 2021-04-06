## Duplicated Files Finder (sample)

---

Based on Spring Boot (which is redundant in this case). Therefore, used almost nothing related to Spring Framework.
The application lists all files in a directory and in all sub-directories, excluding empty ones (0 Byte). It compares
files by their sizes and hash-values (SHA-1 by default). Resulting list of duplicated files gets logged in 
`logs/dupfinder.log`.

###Dependencies
Does not depend on specific libraries or platform. Java 1.8 or higher. In case of "Too many open files" error,
may require to increase ulimit values.

###Build
mvn package

###Usage
`java -jar dupfinder.jar /tmp`

Define other hashing algorithm registered in JRE:
`java -jar -DhashAlg=SHA-256 dupfinder.jar /tmp`

###Suggestions
We might exclude some files, such as, .DS_Store, desktop.ini, symbolic links, etc.