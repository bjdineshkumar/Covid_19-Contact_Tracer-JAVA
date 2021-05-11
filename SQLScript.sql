/** Sql query to create the 2 tables used in the program */


/** Query to create the CONTACT table to store the contacts of mobile device */
CREATE TABLE CONTACT(
IndividualName varchar(255) not null,
ContactName varchar(255) not null,
DateofContact int not null,
duration int not null,
Reported varchar(255));

/** Query to create the TESTTABLE which is used to store the information of test hash */
CREATE TABLE TESTTABLE(
individualname varchar(255),
testhash varchar(255) not null unique,
dates int not null,
result varchar(255) not null);
