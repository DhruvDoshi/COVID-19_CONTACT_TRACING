<h2> Course Project </h2>

## Description
<p>
 - One of the tools to help us manage the COVID-19 pandemic is contact tracing. When one person is diagnosed with COVID-19, the ability to notify other individuals who have been in contact with the person who is COVID-positive allows us to limit the spread of the disease faster.<br>
 - At the same time, the ability to use contact information to detect the frequency of large gatherings also helps us understand the community’s compliance with physical distancing advisories.
 - For this project, you will replicate scaled-down functionality of the Canadian federal government’s application for contact tracing.
</p>

<!--<p> We are using this architecture for the development of the program. </p>-->
<!--![ Canadian COVID-19 tracking application overview](https://i.imgur.com/Jn6HfX0.png)-->

<!--![ : General interaction structure for MobileDevice and Government classes ](https://i.imgur.com/9OOBnkA.png)-->

## Functions Used in The Project
 - Government (String configFile) 
 - public class MobileDevice (String configFile, Government contactTracer) 
 - public void recordContact (String individual, int date, int duration)  
 - public void positiveTest (String testHash)
 - public void recordTestResult (String test_Hash, int date, boolean result)
 - Public boolean synchronizeData () 
 - Public mobileContact (String initiator, string contactinfo) 
 - public int findGatherings (int date, int min_Size, int min_Time, float density) 


## Information
This github repository includes Goverment.java and MobileDevice.java
Alongside that we also included the SQL file for the database development and a report which gives detailed information
Using findGathering method we could get the result. 

## Further Information
Check the Report here: https://git.cs.dal.ca/courses/2021-winter/csci-3901/course-project/doshi/-/blob/master/Report_Course_Project.pdf
