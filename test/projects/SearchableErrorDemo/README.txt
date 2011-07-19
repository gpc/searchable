No functional test has been created for this project, but it is here to check
for regressions. The corresponding issue is this one:

    http://jira.grails.org/browse/GPSEARCHABLE-60

To verify the issue hasn't come back, run the application, create a Parent
instance and then add a Child to it. Apparently adding the Child used to cause
an exception that you could see in the console.
