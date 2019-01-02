# team-212-F18 project
This is a client-server messaging application.
Prattle :: Server side of the messaging application.
Chatter :: Client side of the messaging application.

Our focus is on the development of the server side of this messaging application and expand features to be able to fully support a rich messaging experience.


## TEAM MEMBERS
* Aswath Ilangovan
* Sourabh Punja
* Rakesh Krishna Radhakrishnan
* Shweta Oak

## LINKS TO SYSTEM

| Description | Link  | Port  |
|---|---|---|
| Prattle Application  | ec2-18-217-85-47.us-east-2.compute.amazonaws.com  | 4098  |
|  LDAP Server | ec2-52-14-175-200.us-east-2.compute.amazonaws.com |  10389 |
|  MySQL Server |  http://prattledb.cfmiqlmluzk4.us-east-1.rds.amazonaws.com/ | 3036  |

## LINKS TO YOUTUBE VIDEOS

| Description | Link  |
|---|---|
| System Setup  |  https://youtu.be/mwEx5kztCo4 |
| Final Presentation  |  https://youtu.be/2IWg5EvIg0E |

## Setup Files

* My SQL
    * Files for SQL Queries to generate message tables for persistence are found in 
        - Final Submission/Setup/MYSQL/

* Apache Directory
    * Link for Apache DS Command Line Installer:
        - http://directory.apache.org/apacheds/download/download-linux-bin.html
    * Apache Directory Studio Tool:
        - http://directory.apache.org/studio/
    * LDAP API Documentation:
        - http://directory.apache.org/api/
    * Files for LDAP Schema:
        - Final Submission/Setup/LDAP/Schema/
    * Files for sample LDAP Entries:
        - Final Submission/Setup/LDAP/LDIF Entried/

* Project Source Files
    * The source files are packaged with Maven. Targeted Java Version is 1.8.


## Sample Message Formats Implemented
Message formats for different messages implemented in sprint 3. 

    1. Recall Messaging format :: /recall receiver <ReceiverName> text <Text>
        - eg: /recall receiver punja text  @punja hellowww
    
    2. Search Message format :: 
        1.  /search sender <SenderName> fromTime <FromTimeStamp> toTime <ToTimeStamp>
            - eg: /search sender rak fromTime 2018-11-26,09:55:10 toTime 2018-11-26,09:55:10
        2. /search receiver <ReceiverName> fromTime <FromTimeStamp> toTime <ToTimeStamp>
            - eg: /search receiver punja FromTime 2018-12-01,22:25:16 toTime 2018-12-01,22:27:04
    
    3. Admin Messages :: To create,remove the subpoena(wiretap) between users/group there are four messages
        1. createSubpoenaUsers :: create Subpoena between two users
            - The message text is of format :: createSubpoenaUsers <FromUser> <ToUser> <SubpoenaUser>
        2. createSubpoenaGroup :: create Subpoena for group
            - The message text is of format :: createSubpoenaGroup <GroupName> <SubpoenaUser>
        3. removeSubpoenaUsers :: remove Subpoena between two users
            - The message text is of format :: removeSubpoenaUsers <FromUser> <ToUser> <SubpoenaUser>
        4. removeSubpoenaGroup :: remove Subpoena for group
            - The message text is of format :: removeSubpoenaGroup <FromUser> <ToUser> <SubpoenaUser>
        5. logger :: Switching logging on/off
            - The message text is of format :: logger <On/OFF>
        